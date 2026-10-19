$ErrorActionPreference='Stop'
# User-authorized local SQL Server configuration. Run as Windows Administrator.
$tcp='HKLM:\SOFTWARE\Microsoft\Microsoft SQL Server\MSSQL17.MSSQLSERVER\MSSQLServer\SuperSocketNetLib\Tcp'
# Listen only on IPv4 loopback; do not expose SQL Server to the LAN.
$addresses=@(Get-ChildItem -LiteralPath $tcp | Where-Object PSChildName -ne 'IPAll')
if(-not ($addresses | Get-ItemProperty | Where-Object IpAddress -eq '127.0.0.1')) { throw 'Loopback entry missing; no changes applied.' }
Set-ItemProperty -LiteralPath $tcp -Name ListenOnAllIPs -Value 0
foreach($entry in $addresses) {
 $ip=Get-ItemProperty -LiteralPath $entry.PSPath
 Set-ItemProperty -LiteralPath $entry.PSPath -Name Enabled -Value ([int]($ip.IpAddress -eq '127.0.0.1'))
 if($ip.IpAddress -eq '127.0.0.1') {
  Set-ItemProperty -LiteralPath $entry.PSPath -Name TcpDynamicPorts -Value ''
  Set-ItemProperty -LiteralPath $entry.PSPath -Name TcpPort -Value '1433'
 }
}
Set-ItemProperty -LiteralPath $tcp -Name Enabled -Value 1
Restart-Service -Name MSSQLSERVER -Force
Get-Service -Name MSSQLSERVER
