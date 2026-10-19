USE KhoLinhKienJava;
GO
SET XACT_ABORT ON;
BEGIN TRAN;
INSERT LoaiLinhKien VALUES ('CPU',N'Bộ vi xử lý'),('RAM',N'Bộ nhớ RAM'),('SSD',N'Ổ cứng SSD'),('GPU',N'Card đồ họa'),('MAIN',N'Bo mạch chủ');
INSERT NhaCungCap VALUES ('NCC01',N'Nhà cung cấp mẫu An Phát','0900000001',N'Hà Nội'),('NCC02',N'Nhà cung cấp mẫu Minh Anh','0900000002',N'TP Hồ Chí Minh');
INSERT LinhKien VALUES ('CPU01',N'Intel Core i5 12400','CPU','Intel',N'Cái',3200000,3),('RAM01',N'Kingston DDR4 16GB','RAM','Kingston',N'Thanh',850000,5),('SSD01',N'Samsung SSD 980 500GB','SSD','Samsung',N'Cái',1200000,4),('GPU01',N'GeForce RTX 4060','GPU','ASUS',N'Cái',8000000,2),('MAIN01',N'Mainboard B660M','MAIN','MSI',N'Cái',2500000,2);
INSERT PhieuNhap(MaNCC,NguoiLap,GhiChu) VALUES('NCC01',N'Sinh viên',N'Dữ liệu minh họa để thử nghiệm');
DECLARE @p int=SCOPE_IDENTITY();
INSERT ChiTietNhap VALUES(@p,'CPU01',10,2700000),(@p,'RAM01',20,600000),(@p,'SSD01',12,900000),(@p,'GPU01',3,7000000),(@p,'MAIN01',8,2000000);
COMMIT;
