#!/bin/sh
# rpd-selftest on Linux / macOS (or any shell with a JDK 11+ on PATH): same options as run.ps1.
# Example: ./run.sh -Worktree ../Radish_Pixel_Dungeon -Scenario selftest
exec java "$(dirname "$0")/tools/RunSelftest.java" "$@"
