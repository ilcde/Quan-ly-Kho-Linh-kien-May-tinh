-- SQL Server 2019+; run once on a NEW database. Never drops existing data.
IF DB_ID(N'KhoLinhKienJava') IS NULL CREATE DATABASE KhoLinhKienJava;
GO
USE KhoLinhKienJava;
GO
CREATE TABLE LoaiLinhKien (
 MaLoai nvarchar(20) NOT NULL PRIMARY KEY,
 TenLoai nvarchar(100) NOT NULL UNIQUE,
 CHECK(LEN(LTRIM(RTRIM(MaLoai)))>0), CHECK(LEN(LTRIM(RTRIM(TenLoai)))>0)
);
CREATE TABLE NhaCungCap (
 MaNCC nvarchar(20) NOT NULL PRIMARY KEY,
 TenNCC nvarchar(150) NOT NULL,
 DienThoai nvarchar(20) NOT NULL DEFAULT '',
 DiaChi nvarchar(250) NOT NULL DEFAULT '',
 CHECK(LEN(LTRIM(RTRIM(MaNCC)))>0), CHECK(LEN(LTRIM(RTRIM(TenNCC)))>0)
);
CREATE TABLE LinhKien (
 MaLK nvarchar(20) NOT NULL PRIMARY KEY,
 TenLK nvarchar(150) NOT NULL,
 MaLoai nvarchar(20) NOT NULL REFERENCES LoaiLinhKien(MaLoai),
 Hang nvarchar(80) NOT NULL,
 DonVi nvarchar(20) NOT NULL,
 GiaBan decimal(18,2) NOT NULL CHECK(GiaBan>=0),
 TonToiThieu int NOT NULL DEFAULT 5 CHECK(TonToiThieu>=0),
 CHECK(LEN(LTRIM(RTRIM(MaLK)))>0), CHECK(LEN(LTRIM(RTRIM(TenLK)))>0)
);
CREATE TABLE PhieuNhap (
 MaPN int IDENTITY PRIMARY KEY,
 NgayTao datetime2 NOT NULL DEFAULT SYSDATETIME(),
 MaNCC nvarchar(20) NOT NULL REFERENCES NhaCungCap(MaNCC),
 NguoiLap nvarchar(100) NOT NULL CHECK(LEN(LTRIM(RTRIM(NguoiLap)))>0),
 GhiChu nvarchar(250) NOT NULL DEFAULT ''
);
CREATE TABLE ChiTietNhap (
 MaPN int NOT NULL REFERENCES PhieuNhap(MaPN),
 MaLK nvarchar(20) NOT NULL REFERENCES LinhKien(MaLK),
 SoLuong int NOT NULL CHECK(SoLuong>0),
 DonGia decimal(18,2) NOT NULL CHECK(DonGia>=0),
 PRIMARY KEY(MaPN,MaLK)
);
CREATE TABLE PhieuXuat (
 MaPX int IDENTITY PRIMARY KEY,
 NgayTao datetime2 NOT NULL DEFAULT SYSDATETIME(),
 NguoiNhan nvarchar(150) NOT NULL CHECK(LEN(LTRIM(RTRIM(NguoiNhan)))>0),
 NguoiLap nvarchar(100) NOT NULL CHECK(LEN(LTRIM(RTRIM(NguoiLap)))>0),
 GhiChu nvarchar(250) NOT NULL DEFAULT ''
);
CREATE TABLE ChiTietXuat (
 MaPX int NOT NULL REFERENCES PhieuXuat(MaPX),
 MaLK nvarchar(20) NOT NULL REFERENCES LinhKien(MaLK),
 SoLuong int NOT NULL CHECK(SoLuong>0),
 DonGia decimal(18,2) NOT NULL CHECK(DonGia>=0),
 PRIMARY KEY(MaPX,MaLK)
);
CREATE INDEX IX_LinhKien_Loai ON LinhKien(MaLoai);
CREATE INDEX IX_Nhap_LinhKien ON ChiTietNhap(MaLK) INCLUDE(SoLuong);
CREATE INDEX IX_Xuat_LinhKien ON ChiTietXuat(MaLK) INCLUDE(SoLuong);
GO
CREATE VIEW v_TonKho AS
SELECT l.*, COALESCE(n.TongNhap,0)-COALESCE(x.TongXuat,0) AS TonKho
FROM LinhKien l
LEFT JOIN (SELECT MaLK,SUM(CAST(SoLuong AS bigint)) TongNhap FROM ChiTietNhap GROUP BY MaLK) n ON n.MaLK=l.MaLK
LEFT JOIN (SELECT MaLK,SUM(CAST(SoLuong AS bigint)) TongXuat FROM ChiTietXuat GROUP BY MaLK) x ON x.MaLK=l.MaLK;
GO
