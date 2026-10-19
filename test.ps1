$ErrorActionPreference='Stop'
Set-Location $PSScriptRoot
./build.ps1 -CompileOnly
& javac -encoding UTF-8 -cp build/classes -d build/classes tests/IntegrationTest.java
if($LASTEXITCODE -ne 0){throw 'Test compilation failed'}
& java '-Djava.net.preferIPv4Stack=true' '-Djava.library.path=lib' -cp 'build/classes;lib/*' vn.edu.kholinhkien.IntegrationTest
if($LASTEXITCODE -ne 0){throw 'Integration tests failed'}
