param([string]$Name)
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$path=Join-Path $root "docs/$Name.docx"
$pdf=Join-Path $root "docs/qa/$Name.pdf"
$word=New-Object -ComObject Word.Application
$word.Visible=$false
$word.DisplayAlerts=0
try {
 $doc=$word.Documents.Open($path)
 [void]$doc.Fields.Update()
 $doc.Repaginate()
 foreach($toc in $doc.TablesOfContents){$toc.Update()}
 $doc.Save()
 $doc.ExportAsFixedFormat($pdf,17)
 Write-Output "$Name pages: $($doc.ComputeStatistics(2))"
 $doc.Close(0)
} finally {$word.Quit()}
