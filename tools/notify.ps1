param(
    [string]$Message = "完成",
    [string]$Title = "萝卜地牢"
)

# 提示音（两声）
[console]::beep(880, 180)
Start-Sleep -Milliseconds 100
[console]::beep(1320, 220)

# 非阻塞弹窗：另起一个进程显示 MessageBox，不卡住当前的构建/命令
$escaped = $Message -replace "'", "''"
$arg = "Add-Type -AssemblyName System.Windows.Forms; [System.Windows.Forms.MessageBox]::Show('$escaped', '$Title', 'OK', 'Information') | Out-Null"
Start-Process -FilePath "powershell.exe" -ArgumentList @('-NoProfile', '-Command', $arg) -WindowStyle Normal
