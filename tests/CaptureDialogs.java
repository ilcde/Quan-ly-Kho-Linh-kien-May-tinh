package vn.edu.kholinhkien;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
public class CaptureDialogs {
 static Main app;
 static void capture(String file,Runnable show)throws Exception {
  SwingUtilities.invokeAndWait(()->{
   javax.swing.Timer timer=new javax.swing.Timer(500,e->{
    for(Window w:app.getOwnedWindows())if(w instanceof JDialog d && d.isVisible()){
     try{Container c=d.getContentPane();var im=new BufferedImage(c.getWidth(),c.getHeight(),BufferedImage.TYPE_INT_RGB);var g=im.createGraphics();c.printAll(g);g.dispose();ImageIO.write(im,"png",new File("docs/images/"+file+".png"));}catch(Exception ex){throw new RuntimeException(ex);}finally{d.dispose();}
    }
   });timer.setRepeats(false);timer.start();show.run();
  });
  if(!new File("docs/images/"+file+".png").exists())throw new AssertionError(file);
  System.out.println("PASS dialog "+file);
 }
 public static void main(String[] args)throws Exception {
  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());Font font=new Font("Segoe UI",Font.PLAIN,14);for(Object key:Collections.list(UIManager.getDefaults().keys()))if(UIManager.get(key) instanceof javax.swing.plaf.FontUIResource)UIManager.put(key,font);
  SwingUtilities.invokeAndWait(()->{app=new Main();app.addNotify();app.validate();});
  try{
   Field f=Main.class.getDeclaredField("ENTITIES");f.setAccessible(true);Map<?,?> entities=(Map<?,?>)f.get(null);
   Method edit=Arrays.stream(Main.class.getDeclaredMethods()).filter(m->m.getName().equals("editDialog")).findFirst().orElseThrow();edit.setAccessible(true);
   var types=Db.query("SELECT MaLoai,TenLoai FROM LoaiLinhKien");int i=0;
   for(Object entity:entities.values())capture("form-"+(i++),()->{try{edit.invoke(app,entity,null,types);}catch(Exception e){throw new RuntimeException(e);}});
   Method voucher=Main.class.getDeclaredMethod("voucher",boolean.class,List.class,List.class);voucher.setAccessible(true);
   var products=Db.query("SELECT MaLK,TenLK,GiaBan,TonKho FROM v_TonKho");var suppliers=Db.query("SELECT MaNCC,TenNCC FROM NhaCungCap");
   for(boolean receipt:new boolean[]{true,false})capture(receipt?"form-nhap":"form-xuat",()->{try{voucher.invoke(app,receipt,products,suppliers);}catch(Exception e){throw new RuntimeException(e);}});
  }finally{SwingUtilities.invokeAndWait(()->app.dispose());}
 }
}
