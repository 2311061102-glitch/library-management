# Borrow service

Service quản lý vòng đời mượn sách, trả sách, gia hạn và tiền phạt. Service lưu
`readerId`/`bookId` dạng số vì dữ liệu độc giả và sách thuộc các service khác.

## API chính

Các endpoint dưới đây được gọi qua API Gateway (bỏ tiền tố `/api` nếu gọi trực
tiếp service):

| Method | Endpoint | Quyền | Mô tả |
| --- | --- | --- | --- |
| POST | `/borrows` | Độc giả/Thủ thư | Mượn sách; thủ thư có thể truyền `readerId` |
| GET | `/borrows/my` | Đăng nhập | Lịch sử của mình; hỗ trợ `status`, `overdue` |
| GET | `/borrows/my/summary` | Đăng nhập | Tổng số đang mượn, đã trả, quá hạn và nợ phạt |
| PATCH | `/borrows/{id}/renew` | Chủ phiếu/Thủ thư | Gia hạn phiếu chưa quá hạn |
| PUT | `/borrows/{id}/return` | Chủ phiếu/Thủ thư | Trả sách và tự sinh phạt nếu trễ |
| GET | `/borrows` | Thủ thư | Danh sách phân trang; lọc `status`, `readerId` |
| GET | `/fines/my` | Đăng nhập | Danh sách phạt của mình |
| GET | `/fines` | Thủ thư | Danh sách phạt phân trang; lọc `paid` |
| PATCH | `/fines/{id}/pay` | Thủ thư | Xác nhận đã thu tiền phạt |

## Quy tắc nghiệp vụ

- Không thể mượn khi còn phạt chưa thanh toán, đã đạt giới hạn sách hoặc đang
  mượn cùng đầu sách.
- Khi mượn, service gọi `book-service` để giữ một bản sách; nếu lưu phiếu lỗi
  sẽ gọi bù trừ để trả lại bản.
- Khi trả, service chỉ đóng phiếu sau khi `book-service` xác nhận nhận lại bản.
- Gia hạn mặc định tối đa một lần, mỗi lần thêm `borrow.loan-days` ngày; không
  được gia hạn phiếu đã quá hạn hoặc còn nợ phạt.
- Các thao tác của độc giả luôn lấy `userId` từ JWT, không tin `readerId` do
  client gửi.

## Chạy và kiểm thử

```bash
mvn test
mvn spring-boot:run
```

