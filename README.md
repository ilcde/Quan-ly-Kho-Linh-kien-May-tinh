# Quản lý kho linh kiện máy tính

Ứng dụng desktop quản lý danh mục linh kiện, nhà cung cấp và các phiếu nhập, xuất kho. Dữ liệu được lưu trên SQL Server, giao diện viết bằng Java Swing.

Đồ án môn Java — **Nguyễn Đức Tuệ, lớp TH30.18**.

## Chức năng

- Thêm, sửa, xóa và tìm kiếm linh kiện, loại linh kiện, nhà cung cấp.
- Lập phiếu nhập và phiếu xuất với nhiều mặt hàng.
- Xem lịch sử, chi tiết và tổng tiền của từng phiếu.
- Theo dõi tồn kho, cảnh báo linh kiện sắp hết.
- Kiểm tra số lượng trước khi xuất, không cho phép xuất vượt tồn.
- Xuất danh sách đang hiển thị ra file CSV.

## Công nghệ

- Java 21
- Java Swing, AWT
- SQL Server
- JDBC Driver for SQL Server 13.6.0

## Cài đặt và chạy

### 1. Chuẩn bị

Cài JDK 21 và SQL Server trên Windows. Đặt `JAVA_HOME` trỏ tới thư mục JDK và thêm thư mục `bin` của JDK vào `PATH`.

Bật TCP/IP cho instance SQL Server sử dụng. Cấu hình mẫu kết nối tới `localhost:1433`; nếu dùng cổng khác, cần sửa lại trong file cấu hình.

### 2. Tạo cơ sở dữ liệu

Mở SQL Server Management Studio và chạy lần lượt:

1. `sql/01_schema.sql` — tạo database `KhoLinhKienJava`, các bảng và view tồn kho.
2. `sql/02_seed.sql` — thêm dữ liệu mẫu để chạy thử.

Chỉ chạy các script này khi cài đặt lần đầu trên cơ sở dữ liệu mới.

### 3. Cấu hình kết nối

Sao chép file cấu hình mẫu:

```powershell
Copy-Item config.example.properties config.properties
```

Mặc định chương trình sử dụng Windows Authentication. Tài khoản Windows hiện tại cần có quyền truy cập database. Thư viện JDBC và DLL xác thực cho Java x64 nằm trong thư mục `lib`.

Nếu sử dụng SQL Server Authentication, bỏ `integratedSecurity=true` trong URL kết nối và đặt hai biến môi trường `KHO_DB_USER`, `KHO_DB_PASSWORD` trước khi chạy. File `config.properties` được loại khỏi Git bằng `.gitignore`.

Cấu hình mẫu dùng chứng chỉ tự ký cho kết nối cục bộ. Khi kết nối máy chủ qua mạng, cần cấu hình chứng chỉ phù hợp.

### 4. Khởi động

Nhấp đúp `Run.bat` hoặc chạy trong PowerShell:

```powershell
./run.ps1
```

Script sẽ biên dịch mã nguồn, tạo file JAR và mở ứng dụng. Đóng phiên bản đang chạy trước khi biên dịch lại để tránh lỗi khóa file JAR trên Windows.

## Sử dụng

Tạo loại linh kiện và nhà cung cấp trước, sau đó thêm linh kiện. Lập phiếu nhập để ghi nhận hàng vào kho, lập phiếu xuất khi giao hàng và xem số lượng còn lại tại màn hình **Tồn kho**.

Khi nhập hàng, kiểm tra lại đơn giá mua trước khi ghi sổ; giá được điền sẵn là giá bán tham khảo. Phiếu đã ghi sổ được giữ nguyên, chưa hỗ trợ sửa hoặc hủy phiếu.

## Cấu trúc thư mục

```text
src/main/java/vn/edu/kholinhkien/
    Main.java               Giao diện và xử lý thao tác người dùng
    Db.java                 Kết nối và truy vấn cơ sở dữ liệu
    InventoryService.java   Xử lý nghiệp vụ nhập, xuất kho
sql/                        Script tạo bảng và dữ liệu mẫu
lib/                        JDBC Driver và thư viện xác thực
tests/                      Kiểm thử tích hợp
docs/                       Báo cáo và thiết kế cơ sở dữ liệu
```

Cơ sở dữ liệu gồm 7 bảng: `LoaiLinhKien`, `NhaCungCap`, `LinhKien`, `PhieuNhap`, `ChiTietNhap`, `PhieuXuat`, `ChiTietXuat`. View `v_TonKho` tính tồn kho từ tổng số lượng nhập trừ tổng số lượng xuất. Các thao tác ghi phiếu được thực hiện trong giao dịch để tránh lưu dữ liệu dở dang khi có lỗi.

## Kiểm thử

Kiểm tra kết nối:

```powershell
./run.ps1 --check-db
```

Chạy kiểm thử tích hợp với SQL Server:

```powershell
./test.ps1
```

Bộ kiểm thử bao gồm cập nhật dữ liệu, khóa ngoại, dữ liệu nhập không hợp lệ, xuất vượt tồn, rollback và xuất đồng thời. Các bản ghi kiểm thử được tạo với mã riêng và dọn sau khi chạy.

## Hướng phát triển

- Đăng nhập và phân quyền người dùng.
- Điều chỉnh, hủy phiếu có lưu lịch sử.
- Quản lý số serial và bảo hành linh kiện.
- Thống kê nhập, xuất theo khoảng thời gian.

Giấy phép của thư viện JDBC được lưu trong `lib/Microsoft-JDBC-license.txt`.
