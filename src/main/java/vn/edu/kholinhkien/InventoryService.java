package vn.edu.kholinhkien;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/** Stock is derived from posted documents, never edited directly. */
public final class InventoryService {
    public record Line(String code,int quantity,BigDecimal price) {}
    public static int post(boolean receipt,String partner,String operator,String note,List<Line> lines) throws Exception {
        if(partner==null || partner.isBlank() || operator==null || operator.isBlank())
            throw new IllegalArgumentException("Nhập đối tác và người lập phiếu.");
        if(partner.length()>(receipt?20:150) || operator.length()>100 || note==null || note.length()>250)
            throw new IllegalArgumentException("Thông tin phiếu vượt độ dài cho phép.");
        if(lines.isEmpty()) throw new IllegalArgumentException("Phiếu phải có ít nhất một linh kiện.");
        Set<String> ids=new HashSet<>();
        for(Line l:lines) {
            if(l.code()==null || l.code().isBlank() || !ids.add(l.code().toUpperCase(Locale.ROOT)))
                throw new IllegalArgumentException("Mã linh kiện trống hoặc bị lặp.");
            if(l.quantity()<=0 || l.price()==null || l.price().signum()<0 || l.price().scale()>2 || l.price().precision()>18)
                throw new IllegalArgumentException("Số lượng phải dương, đơn giá không âm và tối đa 2 chữ số thập phân.");
        }
        List<Line> ordered=new ArrayList<>(lines);
        ordered.sort(Comparator.comparing(l->l.code().toUpperCase(Locale.ROOT)));
        try(Connection c=Db.open()) {
            c.setAutoCommit(false);
            try {
                // Same deterministic lock order for every stock movement, held until commit.
                for(Line l:ordered) {
                    try(PreparedStatement s=c.prepareStatement("SELECT MaLK FROM LinhKien WITH(UPDLOCK,HOLDLOCK) WHERE MaLK=?")) {
                        s.setString(1,l.code()); try(ResultSet r=s.executeQuery()) {
                            if(!r.next()) throw new IllegalArgumentException("Không tìm thấy linh kiện: "+l.code());
                        }
                    }
                    if(!receipt) try(PreparedStatement s=c.prepareStatement("SELECT TonKho FROM v_TonKho WHERE MaLK=?")) {
                        s.setString(1,l.code()); try(ResultSet r=s.executeQuery()) { r.next();
                            if(r.getLong(1)<l.quantity()) throw new IllegalArgumentException("Không đủ tồn kho cho "+l.code()+" (còn "+r.getLong(1)+")."); }
                    }
                }
                String header=receipt?"INSERT PhieuNhap(MaNCC,NguoiLap,GhiChu) VALUES(?,?,?)":"INSERT PhieuXuat(NguoiNhan,NguoiLap,GhiChu) VALUES(?,?,?)";
                int id;
                try(PreparedStatement s=c.prepareStatement(header,Statement.RETURN_GENERATED_KEYS)) {
                    Db.bind(s,partner.trim(),operator.trim(),note.trim()); s.executeUpdate();
                    try(ResultSet r=s.getGeneratedKeys()) { r.next(); id=r.getInt(1); }
                }
                String detail=receipt?"INSERT ChiTietNhap(MaPN,MaLK,SoLuong,DonGia) VALUES(?,?,?,?)":"INSERT ChiTietXuat(MaPX,MaLK,SoLuong,DonGia) VALUES(?,?,?,?)";
                try(PreparedStatement s=c.prepareStatement(detail)) {
                    for(Line l:ordered) { Db.bind(s,id,l.code(),l.quantity(),l.price()); s.addBatch(); }
                    s.executeBatch();
                }
                c.commit(); return id;
            } catch(Exception e) { c.rollback(); throw e; }
        }
    }
}
