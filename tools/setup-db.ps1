$ErrorActionPreference='Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
$c=New-Object System.Data.SqlClient.SqlConnection 'Server=.;Database=master;Integrated Security=True;Encrypt=False;Connect Timeout=10'
try {
 $c.Open()
 $check=$c.CreateCommand();$check.CommandText="SELECT DB_ID(N'KhoLinhKienJava')"
 if ($check.ExecuteScalar() -isnot [DBNull]) { throw 'Database already exists. Setup stopped to protect data.' }
 foreach($file in @('sql/01_schema.sql','sql/02_seed.sql')) {
  $text=Get-Content -Raw -Encoding UTF8 $file
  foreach($batch in [regex]::Split($text,'(?im)^GO\s*$')) {
   if(-not [string]::IsNullOrWhiteSpace($batch)){$cmd=$c.CreateCommand();$cmd.CommandText=$batch;$cmd.CommandTimeout=60;[void]$cmd.ExecuteNonQuery()}
  }
 }
 Write-Output 'KhoLinhKienJava schema and demo data created.'
} finally {$c.Dispose()}
