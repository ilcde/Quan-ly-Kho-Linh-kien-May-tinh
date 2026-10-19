# Hợp đồng áp dụng mẫu báo cáo

Nguồn bất biến: C:/Users/ilcde/Music/Bao cao do an java.docx. Mẫu đã xuất bằng Word ra docs/qa/reference/reference.pdf, gồm 21 trang, một section. Renderer đóng gói không có LibreOffice trên Windows; dùng Word COM xuất PDF và pypdfium2 để kiểm tra ảnh, không dùng LibreOffice cài ngoài.

Khổ Letter 21.59 × 27.94 cm, lề bốn phía 2.54 cm, header/footer cách mép 1.27 cm. Mẫu dùng Times New Roman, nội dung 14 pt, tiêu đề chương đậm, mục con đậm/nghiêng; bảng Light Grid Accent 1, tiêu đề ba cột và lưới xanh; đánh số trang trong header. Nội dung mẫu trải rộng do nhiều ảnh và đoạn mã.

Áp dụng: giữ section, header/footer và các phần package không liên quan. Đổi toàn bộ thân báo cáo từ đề tài quản lý điểm sang kho linh kiện. Giữ trật tự tổng quan → phân tích thiết kế → phần mềm thử nghiệm → kết luận. Bổ sung bìa tên sinh viên, mục lục tự động, bảng kiểm thử và tài liệu tham khảo để phục vụ đầu ra người dùng. Ảnh của đề tài cũ không được trình bày; thay bằng ảnh kết xuất Swing thực và ERD đúng schema.

Vùng thay đổi: word/document.xml (toàn bộ nội dung theo đề tài mới), quan hệ ảnh của document, media mới, styles dành cho Heading và Title, settings cập nhật trường. Các phần nguồn khác giữ nguyên byte; tên sinh viên và lớp dùng thông tin người dùng cung cấp, mã sinh viên và giảng viên để trống. Kiểm tra hash nguồn không đổi sau khi tạo. Tài liệu thiết kế CSDL tái sử dụng cùng hình thức và mô tả bảng.

Slot: Title là tên đồ án; Heading 1 là ba chương; Heading 2 là các mục trong mẫu. Bảng từ điển dữ liệu giữ ba cột tên trường/kiểu dữ liệu/mô tả. Ảnh rộng vùng nội dung, không biến dạng. Mã nguồn trích đoạn ngắn, giữ nguyên nội dung Java, không sao chép cách ghép chuỗi SQL không an toàn từ mẫu.
