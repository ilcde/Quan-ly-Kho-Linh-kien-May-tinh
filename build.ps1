param([switch]$CompileOnly)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
New-Item -ItemType Directory -Force build/classes | Out-Null
$sources = @(Get-ChildItem src/main/java -Recurse -Filter *.java | ForEach-Object FullName)
& javac -encoding UTF-8 -d build/classes @sources
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed' }
if ($CompileOnly) { return }
$jarCommand = Get-Command jar -ErrorAction SilentlyContinue
$jarExe = if ($jarCommand) { $jarCommand.Source } elseif ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME/bin/jar.exe")) { "$env:JAVA_HOME/bin/jar.exe" } else { 'C:/Program Files/Java/jdk-21/bin/jar.exe' }
& $jarExe --create --file build/kho-linh-kien.jar --main-class vn.edu.kholinhkien.Main -C build/classes .
if ($LASTEXITCODE -ne 0) { throw 'JAR packaging failed' }
