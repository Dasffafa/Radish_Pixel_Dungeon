/*
 * rpd-selftest: cross-platform runner (Java port of run.ps1 + tools/build-target.ps1).
 *
 * Needs only a JDK (11 or newer); runs straight from source, nothing to compile first:
 *
 *   java <rpd-selftest>/tools/RunSelftest.java -Worktree <path> -Scenario <Name | Name1,Name2 | Pattern* | all | extra | selftest | list> [options]
 *
 * Options (same names and defaults as run.ps1; case-insensitive, "-Name value" or "--name=value"):
 *   -SkipBuild                 reuse out/<worktree>/classpath.txt from the last build (shared with run.ps1)
 *   -TimeoutSeconds 240        per scenario process (in-process watchdog; killed 30 s later from outside)
 *   -TotalTimeoutMinutes 90    whole invocation
 *   -TimeScale 8  -Fps 120  -MaxParallel 2 (1..4)  -Repeat 1 (1..50)  -HeapMb 1024  -SkipDraw
 *   -Width 480  -Height 800    hidden window size
 *   -ScenarioArg "a=1;b=2"     becomes -Drpd.arg.a=1 -Drpd.arg.b=2 for every scenario process
 *   -ExportSave <dir>          after each scenario, copy its sandbox save dir to <dir>/<label>
 *   -ImportSave <dir>          before each scenario, copy <dir>/<name> (or <dir>) into its sandbox save dir
 *   -Overlay <dir>             game .java sources compiled and put IN FRONT of the worktree's classes
 *   -Jdk <dir>                 JDK used for javac and the game processes (default: JAVA_HOME, then this JVM)
 *   -Proxy host:port|none      HTTP(S) proxy for Gradle (default: RPD_SELFTEST_PROXY / RPD_PROXY, else 127.0.0.1:7890 if it answers)
 *
 * Exit code: 0 = every scenario as expected; 1 = FAIL / CRASH / STUCK present; 2 = tool problem.
 *
 * Differences from run.ps1:
 *   - machine-wide limits (4 game processes, 2 Gradle builds) use lock files in <java.io.tmpdir>/rpd-selftest-locks
 *     instead of Windows named semaphores; they are NOT shared with run.ps1 / rpd-tools build.ps1 runs;
 *   - the outside "visible window" guard (UNSAFE) only exists on Windows; the in-process guard of the harness
 *     (HarnessGame / Launcher) works everywhere.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RunSelftest {

	static final boolean WINDOWS = System.getProperty( "os.name" ).toLowerCase( Locale.ROOT ).startsWith( "windows" );
	static final boolean MAC = System.getProperty( "os.name" ).toLowerCase( Locale.ROOT ).startsWith( "mac" );
	static final String SEP = File.pathSeparator;
	static final List<String> VERDICT_ORDER = Arrays.asList(
			"PASS", "FAIL", "CRASH", "STUCK", "ERROR", "UNSAFE", "DIED", "KILLED", "NOCOMPILE", "NOTRUN" );

	public static void main( String[] args ) {
		// piped / read by another program: UTF-8 like run.ps1; a real console keeps its own code page
		if (System.console() == null) {
			System.setOut( new java.io.PrintStream( new java.io.FileOutputStream( java.io.FileDescriptor.out ), true, StandardCharsets.UTF_8 ) );
		}
		int code;
		try {
			code = new RunSelftest().run( args );
		} catch (Usage u) {
			System.out.println( "ERROR: " + u.getMessage() );
			code = 2;
		} catch (Throwable t) {
			System.out.println( "ERROR: runner failed: " + t );
			t.printStackTrace( System.out );
			code = 2;
		}
		System.out.flush();
		System.exit( code );
	}

	static class Usage extends RuntimeException {
		Usage( String m ) { super( m ); }
	}

	// ------------------------------------------------------------------ options

	String worktreeArg, scenarioArg;
	boolean skipBuild, skipDraw;
	int timeoutSeconds = 240, totalTimeoutMinutes = 90, fps = 120, maxParallel = 2, repeat = 1, heapMb = 1024;
	int width = 480, height = 800;
	double timeScale = 8;
	String scenarioArgs = "", exportSave = "", importSave = "", overlay = "", jdkArg = "", proxyArg = "";

	void parse( String[] args ) {
		for (int i = 0; i < args.length; i++) {
			String a = args[i];
			if (!a.startsWith( "-" )) throw new Usage( "unexpected argument '" + a + "' (options look like -Name value)" );
			String name = a.replaceFirst( "^-+", "" );
			String value = null;
			int eq = name.indexOf( '=' );
			if (eq >= 0) { value = name.substring( eq + 1 ); name = name.substring( 0, eq ); }
			name = name.toLowerCase( Locale.ROOT );
			if (name.equals( "skipbuild" )) { skipBuild = value == null || Boolean.parseBoolean( value ); continue; }
			if (name.equals( "skipdraw" )) { skipDraw = value == null || Boolean.parseBoolean( value ); continue; }
			if (value == null) {
				if (i + 1 >= args.length) throw new Usage( "option -" + name + " needs a value" );
				value = args[++i];
			}
			switch (name) {
				case "worktree": worktreeArg = value; break;
				case "scenario": scenarioArg = value; break;
				case "timeoutseconds": timeoutSeconds = intArg( name, value, 1, Integer.MAX_VALUE ); break;
				case "totaltimeoutminutes": totalTimeoutMinutes = intArg( name, value, 1, Integer.MAX_VALUE ); break;
				case "timescale": timeScale = Double.parseDouble( value ); break;
				case "fps": fps = intArg( name, value, 1, 1000 ); break;
				case "maxparallel": maxParallel = intArg( name, value, 1, 4 ); break;
				case "repeat": repeat = intArg( name, value, 1, 50 ); break;
				case "heapmb": heapMb = intArg( name, value, 64, Integer.MAX_VALUE ); break;
				case "width": width = intArg( name, value, 240, 4096 ); break;
				case "height": height = intArg( name, value, 240, 4096 ); break;
				case "scenarioarg": scenarioArgs = value; break;
				case "exportsave": exportSave = value; break;
				case "importsave": importSave = value; break;
				case "overlay": overlay = value; break;
				case "jdk": jdkArg = value; break;
				case "proxy": proxyArg = value; break;
				default: throw new Usage( "unknown option -" + name );
			}
		}
		if (worktreeArg == null) throw new Usage( "-Worktree is required" );
		if (scenarioArg == null) throw new Usage( "-Scenario is required" );
	}

	static int intArg( String name, String v, int min, int max ) {
		int n;
		try { n = Integer.parseInt( v.trim() ); } catch (NumberFormatException e) { throw new Usage( "-" + name + " must be a number, got '" + v + "'" ); }
		if (n < min || n > max) throw new Usage( "-" + name + " must be between " + min + " and " + max + ", got " + n );
		return n;
	}

	// ------------------------------------------------------------------ state

	Path root, srcDir, scnRoot, scnDir, wt, outDir, runRoot, cacheTmp, classes;
	Path jdk, javaExe, javacExe;
	final long started = System.currentTimeMillis();
	final long pid = ProcessHandle.current().pid();
	final List<String> report = new ArrayList<>();

	final List<String> allScenarios = new ArrayList<>();
	final Map<String, String> expected = new HashMap<>(), expectedReason = new HashMap<>();
	final Map<String, Integer> ownTimeout = new HashMap<>();
	final Map<String, String> compileFailed = new HashMap<>();
	final List<String[]> scnArgs = new ArrayList<>();
	List<String> selected = new ArrayList<>();
	String gameCp, appName, versionName, versionCode;
	Path overlayDir, importSaveDir, exportSaveDir;
	List<Path> overlaySources = new ArrayList<>();

	void out( String s ) {
		report.add( s );
		System.out.println( s );
	}

	int run( String[] args ) throws Exception {
		parse( args );
		root = locateRoot();
		srcDir = root.resolve( "src" );
		scnRoot = root.resolve( "scenarios" );
		scnDir = scnRoot.resolve( "rpdtest" ).resolve( "scenarios" );

		// ---------------------------------------------------------- validate
		Path w = Paths.get( worktreeArg );
		if (!Files.isDirectory( w )) { System.out.println( "ERROR: worktree not found: " + worktreeArg ); return 2; }
		wt = w.toRealPath();
		if (!Files.exists( gradlew( wt ) )) { System.out.println( "ERROR: no " + gradlew( wt ).getFileName() + " in " + wt ); return 2; }

		if (!overlay.isEmpty()) {
			if (!Files.isDirectory( Paths.get( overlay ) )) { System.out.println( "ERROR: overlay directory not found: " + overlay ); return 2; }
			overlayDir = Paths.get( overlay ).toRealPath();
			overlaySources = javaFiles( overlayDir, true );
			if (overlaySources.isEmpty()) { System.out.println( "ERROR: no .java file under the overlay directory " + overlayDir ); return 2; }
		}
		if (!importSave.isEmpty()) {
			if (!Files.isDirectory( Paths.get( importSave ) )) { System.out.println( "ERROR: -ImportSave directory not found: " + importSave ); return 2; }
			importSaveDir = Paths.get( importSave ).toRealPath();
		}
		if (!exportSave.isEmpty()) {
			Files.createDirectories( Paths.get( exportSave ) );
			exportSaveDir = Paths.get( exportSave ).toRealPath();
		}
		for (String kv : scenarioArgs.split( ";" )) {
			kv = kv.trim();
			if (kv.isEmpty()) continue;
			int eq = kv.indexOf( '=' );
			if (eq < 1 || !kv.substring( 0, eq ).trim().matches( "[A-Za-z0-9_.\\-]+" )) {
				System.out.println( "ERROR: -ScenarioArg entry '" + kv + "' is not of the form name=value (name: letters, digits, '_', '.', '-')" );
				return 2;
			}
			scnArgs.add( new String[]{ kv.substring( 0, eq ).trim(), kv.substring( eq + 1 ).trim() } );
		}

		jdk = findJdk();
		if (jdk == null) { System.out.println( "ERROR: no JDK found (-Jdk / JAVA_HOME / the JDK running this file)" ); return 2; }
		javaExe = jdk.resolve( "bin" ).resolve( WINDOWS ? "java.exe" : "java" );
		javacExe = jdk.resolve( "bin" ).resolve( WINDOWS ? "javac.exe" : "javac" );

		// ---------------------------------------------------------- scenarios
		Pattern verdictMark = Pattern.compile( "EXPECTED-VERDICT:\\s*([A-Z]+)" );
		Pattern reasonMark = Pattern.compile( "EXPECTED-REASON:[ \\t]*([^\\r\\n]+)" );
		Pattern timeoutMark = Pattern.compile( "TIMEOUT-SECONDS:\\s*(\\d+)" );
		Pattern implementsScenario = Pattern.compile( "implements\\s+Scenario\\b" );
		for (Path f : javaFiles( scnDir, false )) {
			String text = new String( Files.readAllBytes( f ), StandardCharsets.UTF_8 );
			if (!implementsScenario.matcher( text ).find()) continue;
			String name = baseName( f );
			allScenarios.add( name );
			Matcher m;
			if ((m = verdictMark.matcher( text )).find()) expected.put( name, m.group( 1 ) );
			if ((m = reasonMark.matcher( text )).find()) expectedReason.put( name, m.group( 1 ).trim() );
			if ((m = timeoutMark.matcher( text )).find()) ownTimeout.put( name, Integer.parseInt( m.group( 1 ) ) );
		}
		List<String> gameScenarios = allScenarios.stream()
				.filter( s -> !like( s, "SelfTest_*" ) && !like( s, "Extra_*" ) ).collect( Collectors.toList() );
		List<String> extraScenarios = allScenarios.stream().filter( s -> like( s, "Extra_*" ) ).collect( Collectors.toList() );
		List<String> toolScenarios = allScenarios.stream().filter( s -> like( s, "SelfTest_*" ) ).collect( Collectors.toList() );

		if (scenarioArg.equalsIgnoreCase( "list" )) {
			System.out.println( "game scenarios for the reported bugs (-Scenario all):" );
			gameScenarios.forEach( s -> System.out.println( "  " + s ) );
			System.out.println( "game scenarios for incidental findings, not part of 'all' (-Scenario extra):" );
			extraScenarios.forEach( s -> System.out.println( "  " + s ) );
			System.out.println( "tool self-tests (-Scenario selftest):" );
			toolScenarios.forEach( s -> System.out.println( "  " + s + "   (expected verdict: " + expected.getOrDefault( s, "PASS" ) + ")" ) );
			return 0;
		}
		if (scenarioArg.equalsIgnoreCase( "all" )) selected = gameScenarios;
		else if (scenarioArg.equalsIgnoreCase( "extra" )) selected = extraScenarios;
		else if (scenarioArg.equalsIgnoreCase( "selftest" )) selected = toolScenarios;
		else {
			Set<String> sel = new LinkedHashSet<>();
			for (String pat : scenarioArg.split( "," )) {
				pat = pat.trim();
				if (pat.isEmpty()) continue;
				final String p = pat;
				List<String> m = allScenarios.stream().filter( s -> like( s, p ) ).collect( Collectors.toList() );
				if (m.isEmpty()) {
					System.out.println( "ERROR: no scenario matches '" + pat + "'. Available: " + String.join( ", ", allScenarios ) );
					return 2;
				}
				sel.addAll( m );
			}
			selected = new ArrayList<>( sel );
		}
		if (selected.isEmpty()) { System.out.println( "ERROR: no scenario selected" ); return 2; }

		// ---------------------------------------------------------- directories
		String leaf = wt.getFileName().toString();
		String outName = leaf;
		outDir = root.resolve( "out" ).resolve( outName );
		Path wtMark = outDir.resolve( "worktree.txt" );
		if (Files.exists( wtMark ) && !firstLine( wtMark ).equals( wt.toString() )) {
			// same folder name, different worktree: keep them apart
			byte[] d = MessageDigest.getInstance( "MD5" ).digest( wt.toString().toLowerCase( Locale.ROOT ).getBytes( StandardCharsets.UTF_8 ) );
			StringBuilder hex = new StringBuilder();
			for (byte b : d) hex.append( String.format( "%02X", b ) );
			outName = leaf + "-" + hex.substring( 0, 8 );
			outDir = root.resolve( "out" ).resolve( outName );
			wtMark = outDir.resolve( "worktree.txt" );
		}
		Files.createDirectories( outDir );
		write( wtMark, wt.toString() );

		String runTag = overlayDir != null ? outName + "-OVERLAY" : outName;
		runRoot = root.resolve( "runs" ).resolve(
				LocalDateTime.now().format( DateTimeFormatter.ofPattern( "yyyyMMdd_HHmmss" ) ) + "_" + runTag + "_" + pid );
		Files.createDirectories( runRoot );
		cacheTmp = root.resolve( "cache" ).resolve( "tmp" );
		Files.createDirectories( cacheTmp );

		out( "rpd-selftest: worktree=" + wt );
		if (overlayDir != null) {
			out( "rpd-selftest: OVERLAY=" + overlayDir + " (" + overlaySources.size() + " source file(s)) - these results are NOT the results of the worktree alone" );
		}
		out( "rpd-selftest: scenarios=" + String.join( ",", selected ) + (repeat > 1 ? " x" + repeat : "") );
		if (!scnArgs.isEmpty()) {
			out( "rpd-selftest: scenario args=" + scnArgs.stream().map( a -> a[0] + "=" + a[1] ).collect( Collectors.joining( ";" ) ) );
		}
		out( "rpd-selftest: run dir=" + runRoot );

		// ---------------------------------------------------------- 1. build
		Path cpFile = outDir.resolve( "classpath.txt" );
		if (skipBuild) {
			if (!Files.exists( cpFile )) {
				out( "ERROR: -SkipBuild given but there is no exported classpath for this worktree yet (" + cpFile + "). Run once without -SkipBuild." );
				return 2;
			}
			out( "build: skipped (-SkipBuild), using classpath exported at "
					+ LocalDateTime.ofInstant( Files.getLastModifiedTime( cpFile ).toInstant(), java.time.ZoneId.systemDefault() )
					.format( DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" ) ) );
		} else {
			out( "build: gradle :desktop classes of the target worktree (waits for the machine-wide gradle slot if needed) ..." );
			if (!buildTarget( cpFile )) {
				out( "RESULT: BUILD FAILED - the target worktree does not build; no scenario was run." );
				return 2;
			}
		}

		Map<String, String> cpInfo = readKeyValues( cpFile );
		List<String> cpEntries = new ArrayList<>();
		for (String l : Files.readAllLines( cpFile, StandardCharsets.UTF_8 )) if (l.startsWith( "cp=" )) cpEntries.add( l.substring( 3 ) );
		List<String> missing = cpEntries.stream().filter( e -> !Files.exists( Paths.get( e ) ) ).collect( Collectors.toList() );
		if (cpEntries.isEmpty() || !missing.isEmpty()) {
			out( String.format( "ERROR: exported classpath is unusable (%d entries, %d missing, e.g. %s). Run again without -SkipBuild.",
					cpEntries.size(), missing.size(), missing.isEmpty() ? "" : missing.get( 0 ) ) );
			return 2;
		}
		appName = cpInfo.get( "appName" );
		versionName = cpInfo.get( "appVersionName" );
		versionCode = cpInfo.get( "appVersionCode" );
		Files.copy( cpFile, runRoot.resolve( "classpath.txt" ), StandardCopyOption.REPLACE_EXISTING );

		// ---------------------------------------------------------- 2. compile
		classes = runRoot.resolve( "classes" );
		Files.createDirectories( classes );
		gameCp = String.join( SEP, cpEntries );

		if (overlayDir != null) {
			Path overlayClasses = runRoot.resolve( "overlay-classes" );
			Files.createDirectories( overlayClasses );
			Tool r = compile( "overlay", overlaySources, null, overlayClasses );
			if (r.exit != 0) {
				out( "RESULT: OVERLAY DOES NOT COMPILE against this worktree; no scenario was run. javac output:" );
				for (String l : r.text.split( "\\r?\\n" )) out( "  " + l );
				return 2;
			}
			gameCp = overlayClasses + SEP + gameCp;
			long n;
			try (Stream<Path> s = Files.walk( overlayClasses )) { n = s.filter( p -> p.toString().endsWith( ".class" ) ).count(); }
			out( "overlay: compiled " + overlaySources.size() + " source file(s) into " + n + " class file(s), placed in front of the worktree's classes:" );
			for (Path s : overlaySources) out( "overlay:   " + overlayDir.relativize( s ) );
		}

		Tool core = compile( "core", javaFiles( srcDir, true ), null, classes );
		if (core.exit != 0) {
			out( "RESULT: HARNESS DOES NOT COMPILE against this worktree (the game API the harness relies on changed?). javac output:" );
			for (String l : core.text.split( "\\r?\\n" )) out( "  " + l );
			return 2;
		}
		Tool all = compile( "scenarios", javaFiles( scnDir, false ), null, classes );
		if (all.exit != 0) {
			// a scenario written for another branch does not compile here: retry with only the selected ones, then one by one
			List<Path> sel = selected.stream().map( n -> scnDir.resolve( n + ".java" ) ).collect( Collectors.toList() );
			Tool r2 = compile( "selected", sel, scnRoot, classes );
			if (r2.exit != 0) {
				for (String name : selected) {
					Tool one = compile( "scenario-" + name, Arrays.asList( scnDir.resolve( name + ".java" ) ), scnRoot, classes );
					if (one.exit != 0) compileFailed.put( name, one.text );
				}
			}
		}
		out( String.format( "compile: harness ok, scenarios ok=%d failed=%d", selected.size() - compileFailed.size(), compileFailed.size() ) );

		// ---------------------------------------------------------- 3. run
		String realSaveBefore = realSaveState();
		List<Result> results = runAll();

		// ---------------------------------------------------------- 4. report
		String realSaveAfter = realSaveState();
		Map<String, Integer> order = new HashMap<>();
		int idx = 0;
		for (String name : selected) for (int i = 1; i <= repeat; i++) order.put( label( name, i ), idx++ );
		results.sort( ( a, b ) -> Integer.compare( order.getOrDefault( a.label, 0 ), order.getOrDefault( b.label, 0 ) ) );

		out( "" );
		out( "==================== rpd-selftest results ====================" );
		out( String.format( Locale.ROOT, "worktree: %s   (version %s, %d scenario run(s), %d s total)",
				wt, versionName, results.size(), (System.currentTimeMillis() - started) / 1000 ) );
		if (overlayDir != null) {
			out( "OVERLAY : " + overlayDir + "   (" + overlaySources.size() + " game source file(s) replaced for this run - NOT the worktree's own result)" );
		}
		for (Result r : results) out( r.line() );

		for (Result r : results) detail( r );

		Map<String, Integer> count = new HashMap<>();
		for (Result r : results) count.merge( r.verdict, 1, Integer::sum );
		List<String> parts = new ArrayList<>();
		for (String k : VERDICT_ORDER) if (count.containsKey( k )) parts.add( k + "=" + count.get( k ) );
		for (String k : count.keySet()) if (!VERDICT_ORDER.contains( k )) parts.add( k + "=" + count.get( k ) );
		out( "" );
		out( "TOTAL: " + results.size() + "   " + String.join( "  ", parts ) );

		if (realSaveBefore.equals( realSaveAfter )) {
			out( "REAL_SAVE_CHECK: ok - the real save directory (" + realSaveBase() + ") was not touched during the run" );
		} else {
			out( "REAL_SAVE_CHECK: WARNING - the real save directory (" + realSaveBase() + ") changed during the run (may be the user playing another game; the harness itself only writes inside its run dir). Before/after fingerprints are in the run dir." );
			write( runRoot.resolve( "real-save-before.txt" ), realSaveBefore );
			write( runRoot.resolve( "real-save-after.txt" ), realSaveAfter );
		}
		out( "RUN_DIR: " + runRoot );

		int exitCode = 0, selfTestOk = 0;
		for (Result r : results) {
			if (r.asExpected) { if (!r.expected.equals( "PASS" )) selfTestOk++; continue; }
			if (!r.expected.equals( "PASS" ) || !r.expectedReason.isEmpty()) { exitCode = 2; continue; }
			if (Arrays.asList( "FAIL", "CRASH", "STUCK" ).contains( r.verdict )) { if (exitCode < 1) exitCode = 1; }
			else exitCode = 2;
		}
		if (selfTestOk > 0) {
			out( "SELF_TESTS: " + selfTestOk + " tool self-test(s) ended with the non-PASS verdict they are designed to produce (counted as OK)" );
		}
		out( "EXIT_CODE: " + exitCode + "  (0 = all as expected, 1 = FAIL/CRASH/STUCK present, 2 = tool problem present)" );
		Files.write( runRoot.resolve( "report.txt" ), report, StandardCharsets.UTF_8 );
		return exitCode;
	}

	/** The rpd-selftest folder: the parent of tools/ when started as "java .../tools/RunSelftest.java". */
	Path locateRoot() {
		List<Path> cands = new ArrayList<>();
		// source-file mode: "jdk.compiler/com.sun.tools.javac.launcher.Main <path>/RunSelftest.java <args>"
		String cmd = System.getProperty( "sun.java.command", "" );
		Matcher m = Pattern.compile( "^\\S*launcher\\.Main\\s+(.*?RunSelftest\\.java)" ).matcher( cmd );
		if (m.find()) {
			Path tools = Paths.get( m.group( 1 ) ).toAbsolutePath().getParent();
			if (tools != null && tools.getParent() != null) cands.add( tools.getParent() );
		}
		String env = System.getenv( "RPD_SELFTEST_HOME" );
		if (env != null) cands.add( 0, Paths.get( env ) );
		cands.add( Paths.get( "" ).toAbsolutePath() );
		cands.add( Paths.get( "" ).toAbsolutePath().getParent() );
		for (Path c : cands) {
			if (c != null && Files.isDirectory( c.resolve( "src" ).resolve( "rpdtest" ) ) && Files.isDirectory( c.resolve( "scenarios" ) )) {
				return c.normalize();
			}
		}
		throw new Usage( "cannot find the rpd-selftest folder (with src/rpdtest and scenarios/); run this file from its tools/ folder or set RPD_SELFTEST_HOME" );
	}

	Path findJdk() {
		List<String> cands = new ArrayList<>();
		if (!jdkArg.isEmpty()) cands.add( jdkArg );
		String jh = System.getenv( "JAVA_HOME" );
		if (jh != null && !jh.isEmpty()) cands.add( jh );
		cands.add( System.getProperty( "java.home" ) );
		for (String c : cands) {
			Path p = Paths.get( c.replaceAll( "[\\\\/]+$", "" ) );
			if (Files.exists( p.resolve( "bin" ).resolve( WINDOWS ? "javac.exe" : "javac" ) )) return p;
		}
		return null;
	}

	static Path gradlew( Path wt ) {
		return wt.resolve( WINDOWS ? "gradlew.bat" : "gradlew" );
	}

	// ------------------------------------------------------------------ build (tools/build-target.ps1)

	boolean buildTarget( Path cpFile ) throws Exception {
		Path initScript = root.resolve( "gradle" ).resolve( "selftest.init.gradle" );
		if (!Files.exists( initScript )) { out( "build: BUILD_RESULT: ERROR (missing init script " + initScript + ")" ); return false; }
		Files.createDirectories( outDir );
		Path logDir = root.resolve( "logs" );
		Files.createDirectories( logDir );
		Path log = logDir.resolve( "gradle_" + wt.getFileName() + "_"
				+ LocalDateTime.now().format( DateTimeFormatter.ofPattern( "yyyyMMdd_HHmmss_SSS" ) ) + ".log" );

		List<String> cmd = new ArrayList<>();
		Path gw = gradlew( wt );
		if (WINDOWS) { cmd.add( "cmd.exe" ); cmd.add( "/c" ); cmd.add( gw.toString() ); }
		else if (Files.isExecutable( gw )) cmd.add( gw.toString() );
		else { cmd.add( "sh" ); cmd.add( gw.toString() ); }
		cmd.addAll( Arrays.asList( "-I", initScript.toString(), ":desktop:rpdSelftestClasspath",
				"-PrpdSelftestOut=" + cpFile, "--console=plain", "--no-daemon" ) );
		String proxy = proxy();
		if (proxy != null) {
			int c = proxy.lastIndexOf( ':' );
			String host = proxy.substring( 0, c ), port = proxy.substring( c + 1 );
			cmd.addAll( Arrays.asList( "-Dhttp.proxyHost=" + host, "-Dhttp.proxyPort=" + port,
					"-Dhttps.proxyHost=" + host, "-Dhttps.proxyPort=" + port ) );
		}

		ProcessBuilder pb = new ProcessBuilder( cmd ).directory( wt.toFile() ).redirectErrorStream( true );
		Map<String, String> env = pb.environment();
		String sdk = androidSdk();
		if (sdk != null) { env.put( "ANDROID_HOME", sdk ); env.put( "ANDROID_SDK_ROOT", sdk ); }
		// javac diagnostics follow the JVM language; force English
		env.put( "JAVA_TOOL_OPTIONS", "-Duser.language=en -Duser.country=US" );
		if (!env.containsKey( "JAVA_HOME" )) env.put( "JAVA_HOME", jdk.toString() );

		long waitStart = System.currentTimeMillis();
		Slot slot = Slot.acquire( "gradle", 2, TimeUnit.MINUTES.toMillis( 30 ) );
		long waited = (System.currentTimeMillis() - waitStart) / 1000;
		if (slot == null) {
			out( "build: BUILD_RESULT: ERROR (waited " + waited + "s for a gradle slot, gave up)" );
			return false;
		}
		long buildStart = System.currentTimeMillis();
		int code;
		List<String> lines = new ArrayList<>();
		try {
			Process p = pb.start();
			Charset cs = nativeCharset();
			try (BufferedReader r = new BufferedReader( new InputStreamReader( p.getInputStream(), cs ) )) {
				String l;
				while ((l = r.readLine()) != null) lines.add( l );
			}
			if (!p.waitFor( 60, TimeUnit.MINUTES )) { killTree( p ); code = -999; }
			else code = p.exitValue();
		} finally {
			slot.close();
		}
		Files.write( log, lines, StandardCharsets.UTF_8 );
		long elapsed = (System.currentTimeMillis() - buildStart) / 1000;
		boolean fresh = Files.exists( cpFile ) && Files.getLastModifiedTime( cpFile ).toMillis() >= buildStart - 2000;
		boolean ok = code == 0 && fresh && lines.stream().anyMatch( l -> l.contains( "BUILD SUCCESSFUL" ) );

		out( "build: GRADLE_LOG: " + log );
		out( "build: GRADLE_QUEUE_WAIT_SECONDS: " + waited + "  GRADLE_BUILD_SECONDS: " + elapsed + "  GRADLE_EXIT: " + code );
		if (ok) {
			out( "build: CLASSPATH_FILE: " + cpFile );
			out( "build: BUILD_RESULT: BUILD SUCCESSFUL" );
			return true;
		}
		out( "build: BUILD_RESULT: BUILD FAILED" );
		out( "build: ---- error lines ----" );
		Pattern err = Pattern.compile( "(^|\\s)error:|: error |FAILED|Exception|What went wrong" );
		lines.stream().filter( l -> err.matcher( l ).find() ).limit( 60 ).forEach( l -> out( "build: " + l ) );
		out( "build: ---- last 40 lines ----" );
		lines.subList( Math.max( 0, lines.size() - 40 ), lines.size() ).forEach( l -> out( "build: " + l ) );
		return false;
	}

	/** host:port, or null for no proxy. */
	String proxy() {
		String p = !proxyArg.isEmpty() ? proxyArg : System.getenv( "RPD_SELFTEST_PROXY" );
		if (p == null || p.isEmpty()) p = System.getenv( "RPD_PROXY" );
		if (p != null && !p.isEmpty()) {
			if (p.equalsIgnoreCase( "none" )) return null;
			p = p.replaceFirst( "^[a-zA-Z]+://", "" ).replaceAll( "/+$", "" );
			if (!p.matches( ".+:\\d+" )) throw new Usage( "-Proxy must be host:port or none, got '" + p + "'" );
			return p;
		}
		try (Socket s = new Socket()) {
			s.connect( new InetSocketAddress( "127.0.0.1", 7890 ), 300 );
			return "127.0.0.1:7890";
		} catch (IOException e) {
			return null;
		}
	}

	static String androidSdk() {
		for (String v : new String[]{ "ANDROID_HOME", "ANDROID_SDK_ROOT" }) {
			String e = System.getenv( v );
			if (e != null && !e.isEmpty() && Files.isDirectory( Paths.get( e ) )) return e;
		}
		String home = System.getProperty( "user.home" );
		List<Path> cands = new ArrayList<>();
		cands.add( Paths.get( home, "Android", "Sdk" ) );
		cands.add( Paths.get( home, "Library", "Android", "sdk" ) );
		String lad = System.getenv( "LOCALAPPDATA" );
		if (lad != null) cands.add( Paths.get( lad, "Android", "Sdk" ) );
		for (Path c : cands) if (Files.isDirectory( c )) return c.toString();
		return null;
	}

	static Charset nativeCharset() {
		String n = System.getProperty( "native.encoding" );
		if (n == null) n = System.getProperty( "sun.jnu.encoding" );
		try { return n == null ? Charset.defaultCharset() : Charset.forName( n ); }
		catch (Exception e) { return Charset.defaultCharset(); }
	}

	// ------------------------------------------------------------------ javac

	static class Tool {
		int exit;
		String text;
	}

	Tool compile( String tag, List<Path> sources, Path extraSourcePath, Path dest ) throws Exception {
		List<String> lines = new ArrayList<>( Arrays.asList( "-encoding", "UTF-8", "-proc:none", "-nowarn", "-Xlint:none" ) );
		lines.add( "-d" ); lines.add( argQuote( dest.toString() ) );
		lines.add( "-cp" ); lines.add( argQuote( dest + SEP + gameCp ) );
		if (extraSourcePath != null) { lines.add( "-sourcepath" ); lines.add( argQuote( extraSourcePath.toString() ) ); }
		for (Path s : sources) lines.add( argQuote( s.toString() ) );
		Path argFile = runRoot.resolve( "javac-" + tag + ".args" );
		Files.write( argFile, lines, StandardCharsets.UTF_8 );
		// -J options are not allowed inside @argfiles; English diagnostics survive any code page
		ProcessBuilder pb = new ProcessBuilder( javacExe.toString(), "-J-Duser.language=en", "-J-Duser.country=US",
				"-J-Dfile.encoding=UTF-8", "-J-Dsun.stdout.encoding=UTF-8", "-J-Dsun.stderr.encoding=UTF-8", "@" + argFile )
				.directory( runRoot.toFile() ).redirectErrorStream( true );
		clearJavaEnv( pb.environment() );
		Process p = pb.start();
		byte[] bytes = p.getInputStream().readAllBytes();
		Tool t = new Tool();
		if (!p.waitFor( 300, TimeUnit.SECONDS )) { killTree( p ); t.exit = -999; }
		else t.exit = p.exitValue();
		t.text = new String( bytes, StandardCharsets.UTF_8 ).trim();
		write( runRoot.resolve( "javac-" + tag + ".log" ), t.text );
		return t;
	}

	/** One entry of a java/javac @argfile: always quoted; inside quotes backslash and quote are escaped. */
	static String argQuote( String s ) {
		return '"' + s.replace( "\\", "\\\\" ).replace( "\"", "\\\"" ) + '"';
	}

	static void clearJavaEnv( Map<String, String> env ) {
		env.remove( "JAVA_TOOL_OPTIONS" );
		env.remove( "_JAVA_OPTIONS" );
		env.remove( "JDK_JAVA_OPTIONS" );
	}

	// ------------------------------------------------------------------ results

	class Result {
		final String label, verdict, reason, expected, expectedReason;
		final double seconds;
		final Path dir;
		final boolean asExpected;

		Result( String label, String verdict, String reason, double seconds, Path dir ) {
			this.label = label;
			this.verdict = verdict == null ? "ERROR" : verdict;
			this.reason = reason == null ? "" : reason;
			this.seconds = seconds;
			this.dir = dir;
			String name = label.replaceFirst( "_r\\d+$", "" );
			expected = RunSelftest.this.expected.getOrDefault( name, "PASS" );
			expectedReason = RunSelftest.this.expectedReason.getOrDefault( name, "" );
			asExpected = this.verdict.equals( expected ) && (expectedReason.isEmpty() || this.reason.contains( expectedReason ));
		}

		String line() {
			String tag = "";
			if (!expected.equals( "PASS" ) || !expectedReason.isEmpty()) {
				if (asExpected) {
					if (!expected.equals( "PASS" )) tag = "(self-test OK: this is the verdict it is designed to produce)  ";
				} else {
					tag = "(SELF-TEST BROKEN: expected " + expected;
					if (!expectedReason.isEmpty()) tag += " with reason containing '" + expectedReason + "'";
					tag += ")  ";
				}
			}
			return String.format( Locale.ROOT, "[%-6s] %-34s %6.1fs  %s%s", verdict, label, seconds, tag, reason );
		}
	}

	String label( String name, int i ) {
		return repeat > 1 ? name + "_r" + i : name;
	}

	int scenarioTimeout( String name ) {
		return ownTimeout.getOrDefault( name, timeoutSeconds );
	}

	void detail( Result r ) throws IOException {
		// a self-test that produced the non-PASS verdict it is supposed to produce needs no detail block
		if (!r.expected.equals( "PASS" ) && r.asExpected) return;
		Path sumFile = r.dir.resolve( "summary.txt" );
		Path logFile = r.dir.resolve( "game.log" );
		if (r.verdict.equals( "PASS" )) {
			if (!Files.exists( sumFile )) return;
			Map<String, String> kv = readKeyValues( r.dir.resolve( "result.txt" ) );
			List<String> extra = readLines( sumFile ).stream().filter( l -> l.matches( "^\\s+note:.*" ) ).collect( Collectors.toList() );
			String warn = "";
			if (toInt( kv.get( "reported_exceptions" ) ) > 0 || toInt( kv.get( "stderr_exception_lines" ) ) > 0) {
				warn = "PASS, but the game caught/swallowed exceptions during the run: reported=" + kv.get( "reported_exceptions" )
						+ " stderr=" + kv.get( "stderr_exception_lines" ) + " (see " + sumFile + ")";
			}
			if (!extra.isEmpty() || !warn.isEmpty()) {
				out( "" );
				out( "---- " + r.label + ": PASS ----" );
				if (!warn.isEmpty()) out( "  " + warn );
				extra.forEach( this::out );
			}
			return;
		}
		out( "" );
		out( "---- " + r.label + ": " + r.verdict + " ----" );
		if (r.verdict.equals( "NOCOMPILE" )) {
			String name = r.label.replaceFirst( "_r\\d+$", "" );
			Arrays.stream( compileFailed.getOrDefault( name, "" ).split( "\\r?\\n" ) ).limit( 40 ).forEach( l -> out( "  " + l ) );
			return;
		}
		if (r.verdict.equals( "NOTRUN" )) { out( "  " + r.reason ); return; }
		if (Files.exists( sumFile )) {
			List<String> lines = readLines( sumFile );
			int shown = Math.min( lines.size(), 140 );
			for (int i = 0; i < shown; i++) out( "  " + lines.get( i ) );
			if (lines.size() > shown) out( "  ... (" + (lines.size() - shown) + " more lines in summary.txt)" );
			out( "  summary : " + sumFile );
		} else {
			out( "  " + r.reason );
			Path se = r.dir.resolve( "jvm-stderr.txt" );
			if (Files.exists( se ) && Files.size( se ) > 0) {
				out( "  -- JVM stderr (last 30 lines) --" );
				tail( readLines( se ), 30 ).forEach( l -> out( "  " + l ) );
			}
			if (Files.exists( logFile )) {
				out( "  -- game.log (last 40 lines) --" );
				tail( readLines( logFile ), 40 ).forEach( l -> out( "  " + l ) );
			}
		}
		if (Files.exists( logFile )) out( "  full log: " + logFile );
	}

	// ------------------------------------------------------------------ scheduling

	class Run {
		String name, label;
		Path dir;
		Process proc;
		long start;
		Slot slot;
	}

	List<Result> runAll() throws Exception {
		List<Result> results = new ArrayList<>();
		Path nativesMarker = root.resolve( "cache" ).resolve( "natives-ready.txt" );
		boolean warmup = !Files.exists( nativesMarker );

		Deque<String[]> queue = new ArrayDeque<>();
		for (String name : selected) for (int i = 1; i <= repeat; i++) queue.add( new String[]{ name, label( name, i ) } );

		for (String name : selected) {
			if (!compileFailed.containsKey( name )) continue;
			String first = Arrays.stream( compileFailed.get( name ).split( "\\r?\\n" ) ).filter( l -> l.contains( "error:" ) ).findFirst().orElse( "" );
			for (int i = 1; i <= repeat; i++) {
				results.add( new Result( label( name, i ), "NOCOMPILE", "scenario does not compile against this worktree: " + first, 0, runRoot ) );
			}
		}

		List<Run> running = new ArrayList<>();
		// never leave a game process (and its hidden window) behind, even on Ctrl+C
		Thread hook = new Thread( () -> {
			synchronized (running) { for (Run r : running) killTree( r.proc ); }
		} );
		Runtime.getRuntime().addShutdownHook( hook );

		long deadline = started + TimeUnit.MINUTES.toMillis( totalTimeoutMinutes );
		long lowMemSince = -1;
		long lastWindowCheck = 0;
		try {
			while (!queue.isEmpty() || !running.isEmpty()) {
				// start as many as allowed
				while (!queue.isEmpty() && running.size() < maxParallel) {
					String[] next = queue.peek();
					if (compileFailed.containsKey( next[0] )) { queue.poll(); continue; }
					// first ever run extracts the native libraries into cache/tmp: do that with a single process
					if (warmup && !running.isEmpty()) break;
					// be gentle with the machine: wait (up to 3 min) for some free memory before adding a game process
					long free = freeMb();
					if (free >= 0 && free < 700) {
						if (lowMemSince < 0) { lowMemSince = System.currentTimeMillis(); out( "run: only " + free + " MB free memory, waiting before starting the next game process ..." ); }
						if (System.currentTimeMillis() - lowMemSince < 180_000) break;
						out( "run: still only " + free + " MB free after 3 min, starting anyway" );
					}
					lowMemSince = -1;
					Slot slot = Slot.acquire( "game", 4, 0 );
					if (slot == null) break;
					Run run;
					try {
						run = startScenario( next[0], next[1] );
					} catch (Exception e) {
						slot.close();
						throw e;
					}
					run.slot = slot;
					queue.poll();
					synchronized (running) { running.add( run ); }
				}

				Thread.sleep( 200 );

				Set<Long> windowed = Set.of();
				if (WINDOWS && !running.isEmpty() && System.currentTimeMillis() - lastWindowCheck > 3000) {
					lastWindowCheck = System.currentTimeMillis();
					windowed = visibleWindowPids( running );
				}
				for (Run run : new ArrayList<>( running )) {
					boolean done = false, killed = false, windowSeen = false;
					if (!run.proc.isAlive()) {
						done = true;
					} else if (System.currentTimeMillis() - run.start > (scenarioTimeout( run.name ) + 30) * 1000L) {
						killTree( run.proc );
						done = true; killed = true;
					} else if (windowed.contains( run.proc.pid() )) {
						killTree( run.proc );
						done = true; killed = true; windowSeen = true;
					}
					if (!done) continue;
					Result res = complete( run, killed );
					if (windowSeen) {
						res = new Result( run.label, "UNSAFE", "the runner saw a VISIBLE top-level window owned by the game process and killed it immediately", res.seconds, run.dir );
					}
					run.slot.close();
					synchronized (running) { running.remove( run ); }
					results.add( res );
					out( "run: " + res.line() );
					if (warmup) {
						Path gl = run.dir.resolve( "game.log" );
						if (Files.exists( gl ) && readLines( gl ).stream().anyMatch( l -> l.contains( "boot ok" ) )) {
							write( nativesMarker, "natives extracted and loaded ok at " + LocalDateTime.now().format( DateTimeFormatter.ISO_LOCAL_DATE_TIME ) );
							warmup = false;
						}
					}
				}

				if (System.currentTimeMillis() > deadline) {
					out( "run: TOTAL TIMEOUT (" + totalTimeoutMinutes + " min) - killing running scenarios, skipping the rest" );
					for (Run run : new ArrayList<>( running )) {
						killTree( run.proc );
						results.add( complete( run, true ) );
						run.slot.close();
						synchronized (running) { running.remove( run ); }
					}
					while (!queue.isEmpty()) {
						String[] it = queue.poll();
						results.add( new Result( it[1], "NOTRUN", "total timeout reached before this scenario could start", 0, runRoot ) );
					}
				}
			}
		} finally {
			synchronized (running) {
				for (Run run : running) { killTree( run.proc ); if (run.slot != null) run.slot.close(); }
			}
			try { Runtime.getRuntime().removeShutdownHook( hook ); } catch (IllegalStateException ignored) { }
		}
		return results;
	}

	Run startScenario( String name, String label ) throws Exception {
		Run run = new Run();
		run.name = name;
		run.label = label;
		run.dir = runRoot.resolve( label );
		Path sandbox = run.dir.resolve( "sandbox" );
		Path home = sandbox.resolve( "home" );
		Files.createDirectories( home );

		String imported = "";
		if (importSaveDir != null) {
			Path src = Files.isDirectory( importSaveDir.resolve( name ) ) ? importSaveDir.resolve( name ) : importSaveDir;
			copyTree( src, sandbox.resolve( "save" ) );
			imported = src.toString();
		}

		List<String> lines = new ArrayList<>();
		lines.add( "-Xmx" + heapMb + "m" );
		lines.add( "-Xms64m" );
		lines.add( "-XX:+UseSerialGC" );
		if (MAC) lines.add( "-XstartOnFirstThread" );
		lines.add( argQuote( "-XX:ErrorFile=" + run.dir.resolve( "hs_err_pid%p.log" ) ) );
		lines.add( "-Dfile.encoding=UTF-8" );
		lines.add( "-Djava.awt.headless=true" );
		lines.add( argQuote( "-Duser.home=" + home ) );
		lines.add( argQuote( "-Djava.io.tmpdir=" + cacheTmp ) );
		lines.add( argQuote( "-Drpd.scenario=" + name ) );
		lines.add( argQuote( "-Drpd.rundir=" + run.dir ) );
		lines.add( argQuote( "-Drpd.versionName=" + versionName ) );
		lines.add( "-Drpd.versionCode=" + versionCode );
		lines.add( "-Drpd.timescale=" + String.format( Locale.ROOT, "%s", timeScale ) );
		lines.add( "-Drpd.fps=" + fps );
		lines.add( "-Drpd.width=" + width );
		lines.add( "-Drpd.height=" + height );
		lines.add( "-Drpd.timeout=" + scenarioTimeout( name ) );
		lines.add( "-Drpd.parentPid=" + pid );
		if (skipDraw) lines.add( "-Drpd.skipdraw=true" );
		if (!imported.isEmpty()) lines.add( argQuote( "-Drpd.importedSave=" + imported ) );
		for (String[] a : scnArgs) lines.add( argQuote( "-Drpd.arg." + a[0] + "=" + a[1] ) );
		lines.add( "-cp" );
		lines.add( argQuote( classes + SEP + gameCp ) );
		lines.add( "rpdtest.Launcher" );
		Path argFile = run.dir.resolve( "java.args" );
		Files.write( argFile, lines, StandardCharsets.UTF_8 );

		ProcessBuilder pb = new ProcessBuilder( javaExe.toString(), "@" + argFile )
				.directory( run.dir.toFile() )
				.redirectOutput( run.dir.resolve( "jvm-stdout.txt" ).toFile() )
				.redirectError( run.dir.resolve( "jvm-stderr.txt" ).toFile() )
				.redirectInput( ProcessBuilder.Redirect.from( new File( WINDOWS ? "NUL" : "/dev/null" ) ) );
		clearJavaEnv( pb.environment() );
		run.proc = pb.start();
		run.start = System.currentTimeMillis();
		return run;
	}

	Result complete( Run run, boolean killed ) throws Exception {
		double secs = Math.round( (System.currentTimeMillis() - run.start) / 100.0 ) / 10.0;
		int exit = -1;
		try { if (run.proc.waitFor( 5, TimeUnit.SECONDS )) exit = run.proc.exitValue(); } catch (Exception ignored) { }
		if (exportSaveDir != null) exportSave( run );
		Path resFile = run.dir.resolve( "result.txt" );
		if (Files.exists( resFile )) {
			Map<String, String> kv = readKeyValues( resFile );
			return new Result( run.label, kv.get( "verdict" ), kv.get( "reason" ), secs, run.dir );
		}
		if (killed) {
			return new Result( run.label, "KILLED", "process was still running " + (scenarioTimeout( run.name ) + 30)
					+ " s after start and was killed by the runner (no verdict written)", secs, run.dir );
		}
		String why = "JVM exited with code " + exit + " without writing a verdict";
		try (Stream<Path> s = Files.list( run.dir )) {
			Path hs = s.filter( p -> p.getFileName().toString().startsWith( "hs_err_pid" ) ).findFirst().orElse( null );
			if (hs != null) why += " (native JVM crash, see " + hs + ")";
		}
		Path se = run.dir.resolve( "jvm-stderr.txt" );
		if (Files.exists( se )) {
			List<String> l = readLines( se ).stream().filter( x -> !x.trim().isEmpty() ).limit( 3 ).collect( Collectors.toList() );
			if (!l.isEmpty()) why += ": " + String.join( " | ", l );
		}
		return new Result( run.label, "DIED", why, secs, run.dir );
	}

	void exportSave( Run run ) {
		try {
			Path save = run.dir.resolve( "sandbox" ).resolve( "save" );
			if (!Files.isDirectory( save )) return;
			Path dest = exportSaveDir.resolve( run.label );
			if (Files.exists( dest )) {
				// never delete an earlier export
				Files.move( dest, exportSaveDir.resolve( run.label + ".old-"
						+ LocalDateTime.now().format( DateTimeFormatter.ofPattern( "yyyyMMdd_HHmmss" ) ) ) );
			}
			copyTree( save, dest );
		} catch (IOException e) {
			out( "run: could not export the save of " + run.label + ": " + e );
		}
	}

	static void killTree( Process p ) {
		if (p == null || !p.isAlive()) return;
		p.descendants().forEach( ProcessHandle::destroyForcibly );
		p.destroyForcibly();
		try { p.waitFor( 5, TimeUnit.SECONDS ); } catch (InterruptedException ignored) { }
	}

	/** Windows only: pids (among the running game processes) that own a VISIBLE top-level window. */
	static Set<Long> visibleWindowPids( List<Run> running ) {
		try {
			String ids = running.stream().map( r -> Long.toString( r.proc.pid() ) ).collect( Collectors.joining( "," ) );
			Process p = new ProcessBuilder( "powershell.exe", "-NoProfile", "-NonInteractive", "-Command",
					"Get-Process -Id " + ids + " -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowHandle -ne 0 } | ForEach-Object { $_.Id }" )
					.redirectErrorStream( true ).start();
			String o = new String( p.getInputStream().readAllBytes(), StandardCharsets.US_ASCII );
			if (!p.waitFor( 10, TimeUnit.SECONDS )) { p.destroyForcibly(); return Set.of(); }
			Set<Long> s = new java.util.HashSet<>();
			for (String l : o.split( "\\s+" )) if (l.matches( "\\d+" )) s.add( Long.parseLong( l ) );
			return s;
		} catch (Exception e) {
			return Set.of();
		}
	}

	/** Free physical memory in MB (MemAvailable on Linux), -1 if unknown. */
	static long freeMb() {
		try {
			Path mi = Paths.get( "/proc/meminfo" );
			if (Files.exists( mi )) {
				for (String l : Files.readAllLines( mi )) {
					if (l.startsWith( "MemAvailable:" )) return Long.parseLong( l.replaceAll( "\\D+", "" ) ) / 1024;
				}
			}
			java.lang.management.OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
			if (os instanceof com.sun.management.OperatingSystemMXBean) {
				@SuppressWarnings("deprecation")
				long free = ((com.sun.management.OperatingSystemMXBean) os).getFreePhysicalMemorySize();
				return free / (1024 * 1024);
			}
		} catch (Exception ignored) { }
		return -1;
	}

	// ------------------------------------------------------------------ machine-wide slots (named semaphores in run.ps1)

	static final class Slot implements AutoCloseable {
		private final RandomAccessFile file;
		private final FileLock lock;

		private Slot( RandomAccessFile file, FileLock lock ) {
			this.file = file;
			this.lock = lock;
		}

		/** One of {@code capacity} slots named {@code kind}; null if none was free within {@code waitMs}. Released by the OS if this process dies. */
		static Slot acquire( String kind, int capacity, long waitMs ) throws IOException, InterruptedException {
			Path dir = Paths.get( System.getProperty( "java.io.tmpdir" ), "rpd-selftest-locks" );
			Files.createDirectories( dir );
			long until = System.currentTimeMillis() + waitMs;
			while (true) {
				for (int i = 0; i < capacity; i++) {
					RandomAccessFile f = new RandomAccessFile( dir.resolve( kind + "-" + i + ".lock" ).toFile(), "rw" );
					FileLock l = null;
					try {
						FileChannel ch = f.getChannel();
						l = ch.tryLock();
					} catch (java.nio.channels.OverlappingFileLockException ignored) {
						// held by this very process
					}
					if (l != null) return new Slot( f, l );
					f.close();
				}
				if (System.currentTimeMillis() >= until) return null;
				Thread.sleep( 500 );
			}
		}

		@Override
		public void close() {
			try { lock.release(); } catch (IOException ignored) { }
			try { file.close(); } catch (IOException ignored) { }
		}
	}

	// ------------------------------------------------------------------ real save fingerprint

	/** Where the real game keeps its saves (see DesktopLauncher). */
	Path realSaveBase() {
		String home = System.getProperty( "user.home" );
		if (WINDOWS) {
			String ad = System.getenv( "APPDATA" );
			return ad != null ? Paths.get( ad, ".shatteredpixel" ) : Paths.get( home, "AppData", "Roaming", ".shatteredpixel" );
		}
		if (MAC) return Paths.get( home, "Library", "Application Support" );
		String xdg = System.getenv( "XDG_DATA_HOME" );
		return Paths.get( xdg != null ? xdg : home + "/.local/share", ".shatteredpixel" );
	}

	String realSaveState() throws IOException {
		Path base = realSaveBase();
		if (!Files.isDirectory( base )) return "no " + base + " directory";
		List<String> lines = new ArrayList<>();
		if (!MAC) {
			try (Stream<Path> s = Files.list( base )) {
				for (Path d : s.sorted().collect( Collectors.toList() )) {
					lines.add( d.getFileName() + "|" + Files.getLastModifiedTime( d ).toMillis() );
				}
			}
		}
		Path game = appName == null ? null : base.resolve( appName );
		if (game != null && Files.isDirectory( game )) {
			try (Stream<Path> s = Files.walk( game )) {
				for (Path f : s.sorted().collect( Collectors.toList() )) {
					BasicFileAttributes a = Files.readAttributes( f, BasicFileAttributes.class );
					lines.add( f + "|" + (a.isDirectory() ? 0 : a.size()) + "|" + a.lastModifiedTime().toMillis() );
				}
			}
		} else {
			lines.add( "(no '" + appName + "' directory)" );
		}
		return String.join( "\n", lines );
	}

	// ------------------------------------------------------------------ small helpers

	/** PowerShell -like: case-insensitive, * and ? wildcards. */
	static boolean like( String s, String pattern ) {
		StringBuilder re = new StringBuilder();
		for (char c : pattern.toCharArray()) {
			if (c == '*') re.append( ".*" );
			else if (c == '?') re.append( '.' );
			else re.append( Pattern.quote( String.valueOf( c ) ) );
		}
		return Pattern.compile( re.toString(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL ).matcher( s ).matches();
	}

	static List<Path> javaFiles( Path dir, boolean recursive ) throws IOException {
		if (!Files.isDirectory( dir )) return new ArrayList<>();
		try (Stream<Path> s = recursive ? Files.walk( dir ) : Files.list( dir )) {
			return s.filter( p -> p.toString().endsWith( ".java" ) && Files.isRegularFile( p ) ).sorted().collect( Collectors.toList() );
		}
	}

	static String baseName( Path f ) {
		String n = f.getFileName().toString();
		return n.substring( 0, n.length() - ".java".length() );
	}

	static String firstLine( Path p ) throws IOException {
		List<String> l = readLines( p );
		return l.isEmpty() ? "" : l.get( 0 ).trim();
	}

	static List<String> readLines( Path p ) throws IOException {
		String t = new String( Files.readAllBytes( p ), StandardCharsets.UTF_8 );
		if (t.startsWith( "\uFEFF" )) t = t.substring( 1 );
		return t.isEmpty() ? new ArrayList<>() : new ArrayList<>( Arrays.asList( t.split( "\\r?\\n", -1 ) ) );
	}

	static Map<String, String> readKeyValues( Path p ) throws IOException {
		Map<String, String> m = new LinkedHashMap<>();
		if (!Files.exists( p )) return m;
		for (String l : readLines( p )) {
			int i = l.indexOf( '=' );
			if (i > 0) m.put( l.substring( 0, i ), l.substring( i + 1 ) );
		}
		return m;
	}

	static int toInt( String s ) {
		try { return s == null ? 0 : Integer.parseInt( s.trim() ); } catch (NumberFormatException e) { return 0; }
	}

	static List<String> tail( List<String> l, int n ) {
		while (!l.isEmpty() && l.get( l.size() - 1 ).isEmpty()) l.remove( l.size() - 1 );
		return l.subList( Math.max( 0, l.size() - n ), l.size() );
	}

	static void write( Path p, String s ) throws IOException {
		Files.write( p, s.getBytes( StandardCharsets.UTF_8 ) );
	}

	static void copyTree( Path from, Path to ) throws IOException {
		try (Stream<Path> s = Files.walk( from )) {
			for (Path src : s.collect( Collectors.toList() )) {
				Path dest = to.resolve( from.relativize( src ).toString() );
				if (Files.isDirectory( src )) Files.createDirectories( dest );
				else {
					Files.createDirectories( dest.getParent() );
					Files.copy( src, dest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES );
				}
			}
		}
	}
}
