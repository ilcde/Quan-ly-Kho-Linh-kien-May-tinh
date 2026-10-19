$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
& "$PSScriptRoot/build.ps1"
& java '-Djava.net.preferIPv4Stack=true' '-Djava.library.path=lib' -cp 'build/kho-linh-kien.jar;lib/*' vn.edu.kholinhkien.Main @args
if ($LASTEXITCODE -ne 0) { throw 'Application exited with an error' }
