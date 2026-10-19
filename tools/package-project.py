from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
root=Path(__file__).resolve().parent.parent
archive=root/'Kho_Linh_Kien_Java_Nguyen_Duc_Tue.zip'
files=[]
for name in ['README.md','.gitignore','config.example.properties','Run.bat','run.ps1','build.ps1','test.ps1']:
    files.append(root/name)
for folder in ['src','sql','tests','lib']:
    files.extend(p for p in (root/folder).rglob('*') if p.is_file())
files.extend((root/'docs').glob('*.docx'))
files.extend((root/'docs/images').glob('*.png'))
files.extend((root/'tools').glob('*.ps1'))
files.append(root/'dist/kho-linh-kien.jar')
with ZipFile(archive,'w',ZIP_DEFLATED) as z:
    for p in files:z.write(p,'KhoLinhKienJava/'+p.relative_to(root).as_posix())
with ZipFile(archive) as z:
    assert z.testzip() is None
    assert not any(n.endswith('/config.properties') or '/qa/' in n for n in z.namelist())
print(archive.name,archive.stat().st_size,'bytes',len(files),'files')
