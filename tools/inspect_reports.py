from pathlib import Path
import pypdfium2 as pdf
from PIL import Image, ImageDraw
from pypdf import PdfReader
root=Path(__file__).resolve().parent.parent/'docs/qa'
for name in ['Bao_cao_Java_Nguyen_Duc_Tue','Thiet_ke_CSDL_Kho_linh_kien']:
    print(name)
    for i,p in enumerate(PdfReader(root/(name+'.pdf')).pages):
        text=p.extract_text();print(i+1,len(text),text[:95].replace('\n',' '))
    out=root/name;out.mkdir(exist_ok=True);p=pdf.PdfDocument(root/(name+'.pdf'))
    for i,page in enumerate(p):page.render(scale=1.3).to_pil().save(out/('page-%d.png'%(i+1)))
    for start in range(0,len(p),6):
        im=Image.new('RGB',(1530,1100),'#cccccc');d=ImageDraw.Draw(im)
        for j in range(start,min(start+6,len(p))):
            xy=((j-start)%3*510,(j-start)//3*550)
            im.paste(Image.open(out/('page-%d.png'%(j+1))).resize((400,518)),(xy[0]+50,xy[1]+24))
            d.text((xy[0]+50,xy[1]+5),str(j+1),fill='black')
        im.save(out/('contact-%d.png'%(start//6)))
