package vn.edu.kholinhkien;

import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.io.*;

/** JDBC only: each operation owns and closes its connection. */
public final class Db {
    private Db() {}
    public static Connection open() throws Exception {
        Properties p = new Properties();
        Path config = Path.of("config.properties");
        if (!Files.exists(config)) throw new IOException("Chưa có config.properties. Sao chép config.example.properties và cấu hình SQL Server.");
        try (Reader r = Files.newBufferedReader(config)) { p.load(r); }
        String url = p.getProperty("db.url");
        if (url == null || !url.startsWith("jdbc:sqlserver:")) throw new IOException("db.url phải là địa chỉ JDBC SQL Server.");
        Properties auth = new Properties();
        if (System.getenv("KHO_DB_USER") != null) auth.setProperty("user",System.getenv("KHO_DB_USER"));
        if (System.getenv("KHO_DB_PASSWORD") != null) auth.setProperty("password",System.getenv("KHO_DB_PASSWORD"));
        return DriverManager.getConnection(url,auth);
    }
    public static List<Object[]> query(String sql,Object... args) throws Exception {
        try(Connection c=open(); PreparedStatement s=c.prepareStatement(sql)) {
            bind(s,args);
            try(ResultSet r=s.executeQuery()) {
                List<Object[]> rows=new ArrayList<>();
                while(r.next()) { Object[] row=new Object[r.getMetaData().getColumnCount()];
                    for(int i=0;i<row.length;i++) row[i]=r.getObject(i+1); rows.add(row); }
                return rows;
            }
        }
    }
    public static void execute(String sql,Object... args) throws Exception {
        try(Connection c=open(); PreparedStatement s=c.prepareStatement(sql)) {
            bind(s,args); if(s.executeUpdate()!=1) throw new SQLException("Bản ghi đã thay đổi hoặc không còn tồn tại. Hãy tải lại.");
        }
    }
    static void bind(PreparedStatement s,Object... args) throws SQLException {
        for(int i=0;i<args.length;i++) s.setObject(i+1,args[i]);
    }
}
