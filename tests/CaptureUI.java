package vn.edu.kholinhkien;
import javax.swing.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.lang.reflect.*;
public class CaptureUI {
 public static void main(String[] args)throws Exception {
  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
  java.awt.Font font=new java.awt.Font("Segoe UI",java.awt.Font.PLAIN,14);for(Object key:java.util.Collections.list(UIManager.getDefaults().keys()))if(UIManager.get(key) instanceof javax.swing.plaf.FontUIResource)UIManager.put(key,font);
  Main[] app=new Main[1];SwingUtilities.invokeAndWait(()->{app[0]=new Main();app[0].addNotify();app[0].validate();});
  Method show=Main.class.getDeclaredMethod("showPage",String.class);show.setAccessible(true);
  Field status=Main.class.getDeclaredField("status");status.setAccessible(true);
  String[] names={"Tồn kho","Linh kiện","Loại linh kiện","Nhà cung cấp","Phiếu nhập","Phiếu xuất"};
  new File("docs/images").mkdirs();
  try{for(int i=0;i<names.length;i++){
   String name=names[i];SwingUtilities.invokeAndWait(()->{try{show.invoke(app[0],name);}catch(Exception e){throw new RuntimeException(e);}});
   boolean ready=false;for(int tick=0;tick<100;tick++){Thread.sleep(100);String[] s=new String[1];SwingUtilities.invokeAndWait(()->{try{s[0]=((JLabel)status.get(app[0])).getText();}catch(Exception e){throw new RuntimeException(e);}});if(s[0].equals("Hoàn tất")){ready=true;break;}}
   if(!ready)throw new IllegalStateException("UI did not load "+name);
   int index=i;SwingUtilities.invokeAndWait(()->{try{app[0].validate();var cp=app[0].getContentPane();BufferedImage image=new BufferedImage(cp.getWidth(),cp.getHeight(),BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();cp.printAll(g);g.dispose();ImageIO.write(image,"png",new File("docs/images/screen-"+index+".png"));}catch(Exception e){throw new RuntimeException(e);}});
   System.out.println("Rendered "+name);
  }}finally{SwingUtilities.invokeAndWait(()->app[0].dispose());}
 }
}
