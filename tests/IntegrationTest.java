package vn.edu.kholinhkien;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

public class IntegrationTest {
    private static int passed;
    private static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);System.out.println("PASS "+name);passed++;}
    private static void rejects(Throwing f,String name)throws Exception{try{f.run();}catch(Exception expected){check(true,name);return;}throw new AssertionError(name);}
    interface Throwing{void run()throws Exception;}
    static long stock(String id)throws Exception{return ((Number)Db.query("SELECT TonKho FROM v_TonKho WHERE MaLK=?",id).get(0)[0]).longValue();}
    public static void main(String[] args)throws Exception{
        String id="T"+System.currentTimeMillis();
        try{
            Db.execute("INSERT LoaiLinhKien VALUES(?,?)",id,"Kiểm thử "+id);
            Db.execute("INSERT NhaCungCap VALUES(?,?,?,?)",id,"Kiểm thử "+id,"","Địa chỉ thử nghiệm");
            Db.execute("INSERT LinhKien VALUES(?,?,?,?,?,?,?)",id,"RAM kiểm thử",id,"Test","Thanh",100,2);
            check(stock(id)==0,"initial stock zero");
            Db.execute("UPDATE LinhKien SET TenLK=? WHERE MaLK=?","RAM đã sửa",id);
            check(Db.query("SELECT TenLK FROM LinhKien WHERE MaLK=?",id).get(0)[0].equals("RAM đã sửa"),"CRUD update Unicode");
            rejects(()->Db.execute("INSERT LoaiLinhKien VALUES(?,?)",id,"Trùng"),"duplicate primary key rejected");
            rejects(()->Db.execute("DELETE LoaiLinhKien WHERE MaLoai=?",id),"referenced category deletion rejected");
            check(Db.query("SELECT MaLK FROM LinhKien WHERE MaLK=?","' OR 1=1 --").isEmpty(),"parameterized query prevents injection");
            var ten=List.of(new InventoryService.Line(id,10,new BigDecimal("100.00")));
            InventoryService.post(true,id,id,"Kiểm thử",ten);check(stock(id)==10,"receipt adds stock");
            rejects(()->InventoryService.post(false,id,id,"",List.of(new InventoryService.Line(id,11,new BigDecimal("100.00")))),"oversell rejected");
            check(stock(id)==10 && Db.query("SELECT MaPX FROM PhieuXuat WHERE NguoiLap=?",id).isEmpty(),"failed issue rolls back header and stock");
            rejects(()->InventoryService.post(true,id,id,"",List.of(new InventoryService.Line(id,0,BigDecimal.ONE))),"zero quantity rejected");
            rejects(()->InventoryService.post(true,id,id,"",List.of(new InventoryService.Line(id,1,new BigDecimal("-1")))),"negative price rejected");
            rejects(()->InventoryService.post(true,id,id,"",List.of(ten.get(0),ten.get(0))),"duplicate line rejected");
            rejects(()->InventoryService.post(true,"missing-"+id,id,"",ten),"invalid supplier rejected");check(stock(id)==10,"invalid supplier no stock mutation");
            ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);
            Callable<Boolean> issue=()->{start.await();try{InventoryService.post(false,id,id,"Concurrent",List.of(new InventoryService.Line(id,7,new BigDecimal("150.00"))));return true;}catch(IllegalArgumentException e){return false;}};
            Future<Boolean> a=pool.submit(issue),b=pool.submit(issue);start.countDown();try{check(a.get(30,TimeUnit.SECONDS)^b.get(30,TimeUnit.SECONDS),"only one concurrent overselling issue succeeds");}finally{pool.shutdownNow();}
            check(stock(id)==3,"stock remains nonnegative after concurrent issues");
            rejects(()->Db.execute("DELETE LinhKien WHERE MaLK=?",id),"product with history cannot be deleted");
            rejects(()->Main.money("1.999"),"excess currency precision rejected");
            System.out.println("PASSED: "+passed);
        }finally{
            try(var c=Db.open()){
                c.setAutoCommit(false);
                try{
                    for(String sql:List.of("DELETE ChiTietXuat WHERE MaPX IN(SELECT MaPX FROM PhieuXuat WHERE NguoiLap=?)","DELETE PhieuXuat WHERE NguoiLap=?","DELETE ChiTietNhap WHERE MaPN IN(SELECT MaPN FROM PhieuNhap WHERE NguoiLap=?)","DELETE PhieuNhap WHERE NguoiLap=?","DELETE LinhKien WHERE MaLK=?","DELETE NhaCungCap WHERE MaNCC=?","DELETE LoaiLinhKien WHERE MaLoai=?"))try(var s=c.prepareStatement(sql)){s.setString(1,id);s.executeUpdate();}
                    c.commit();
                }catch(Exception e){c.rollback();throw e;}
            }
        }
    }
}
