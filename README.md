# Library Management

Hệ thống quản lý thư viện xây dựng bằng Spring Boot, RESTful API, MySQL và giao diện web.

## Yêu cầu

- JDK 17 trở lên
- MySQL 8
- Maven (hoặc Maven Wrapper có sẵn trong repository)

## Cấu hình và chạy

1. Tạo database `library_db` trong MySQL Workbench hoặc chạy file `library_db.sql` nếu bạn có file dữ liệu.
2. Đặt biến môi trường `DB_PASSWORD` bằng mật khẩu MySQL của tài khoản `root`.
3. Mở project bằng IntelliJ IDEA và chạy `vn.edu.crs.librarymanagement.LibraryManagementApplication`.
4. Mở giao diện tại [http://localhost:8080/](http://localhost:8080/).

Trong PowerShell, có thể đặt mật khẩu cho phiên terminal hiện tại bằng:

```powershell
$env:DB_PASSWORD = "mật_khẩu_MySQL_của_bạn"
```

## Chức năng

- Bạn đọc: đăng ký, đăng nhập, tìm kiếm sách, mượn sách, gia hạn, xem lịch sử và khoản phạt, cập nhật hồ sơ.
- Quản trị viên: tổng quan, quản lý sách và ảnh bìa, danh mục, độc giả, phiếu mượn/trả và phiếu phạt.
- API REST cho sách, danh mục, tài khoản, mượn/trả và tiền phạt.

## Nghiệp vụ mượn sách

- Mỗi người dùng chỉ được giữ tối đa số sách cấu hình trong `library.max-books-per-user`.
- Không thể mượn trùng một đầu sách khi phiếu cũ vẫn đang ở trạng thái `BORROWING`.
- Người dùng còn phiếu phạt `UNPAID` phải thanh toán trước khi mượn sách mới.
- Khi mượn, hệ thống trừ `available_copies` và tạo phiếu với hạn trả tự động.
- Chỉ phiếu đang mượn và chưa quá hạn mới được gia hạn; số lần và số ngày gia hạn lấy từ cấu hình.
- Người dùng có thể trả sách trực tiếp trong trang `Sach toi da muon`; hệ thống kiểm tra đúng chủ phiếu trước khi xử lý.
- Khi trả sách, hệ thống hoàn lại tồn kho. Nếu trả quá hạn, hệ thống tự tạo một phiếu phạt duy nhất và tải lại danh sách phạt.
- Mỗi bản sao có barcode và trạng thái `AVAILABLE`, `BORROWED`, `RESERVED`, `DAMAGED` hoặc `LOST`.
- Khi hết sách, độc giả có thể đặt trước; khi có bản trả, bản sao được chuyển `RESERVED` cho người đầu hàng đợi trong số ngày cấu hình.
- Trả sách cho phép chọn tình trạng `GOOD`, `DAMAGED` hoặc `LOST`; hư/mất tạo phạt theo giá sách nếu có, nếu chưa có giá thì dùng mức phạt cấu hình.
- Mỗi lần gia hạn được lưu tại bảng `loan_renewals`; gia hạn bị từ chối nếu sách đã có người đặt trước hoặc độc giả đang nợ phạt.

API nghiệp vụ mở rộng:

```text
POST /borrows/reservations
PUT  /borrows/{id}/return-details?userId={userId}
     Body: {"condition":"GOOD|DAMAGED|LOST","note":"..."}
PUT  /borrows/return-by-barcode
     Body: {"barcode":"BOOK-1-1","condition":"GOOD|DAMAGED|LOST","note":"..."}
```

Các API chính:

```text
POST /borrows
PUT  /borrows/{id}/renew?userId={userId}
PUT  /borrows/{id}/return
GET  /borrows/user/{userId}
GET  /borrows?role=ADMIN
GET  /borrows/eligibility?userId={userId}&bookId={bookId}
```

Trên giao diện, nút **Mượn sách** không tạo phiếu ngay. Hệ thống trước tiên gọi
`GET /borrows/eligibility` để hiển thị sách, số bản còn lại, số sách người dùng
đang giữ, hạn trả dự kiến và các điều kiện bị từ chối. Người dùng phải tích
đồng ý chính sách rồi mới xác nhận. Request tạo phiếu vẫn kiểm tra lại toàn bộ
điều kiện trong transaction để tránh việc dữ liệu thay đổi giữa lúc xem và lúc
xác nhận.

## Bộ câu hỏi bảo vệ và cách trả lời

### 1. Vì sao chọn kiến trúc này?

Project hiện dùng **layered monolith**: Controller nhận HTTP request, Service xử lý nghiệp vụ, Repository truy cập dữ liệu và Entity ánh xạ bảng. Với bài quản lý thư viện, cách này dễ triển khai, debug và chạy demo trên một máy. Nếu chọn microservices, nhóm phải bổ sung service discovery, gateway, giao tiếp mạng, timeout/retry, tracing, bảo mật giữa service và cơ chế dữ liệu phân tán; chi phí vận hành lớn hơn khi quy mô bài chưa cần.

### 2. Thực thể trung tâm nằm ở module nào?

`Book` là thực thể trung tâm của luồng mượn sách:

```text
BookService       -> quản lý sách và tồn kho
CategoryService   -> phân loại Book
BorrowService     -> tạo BorrowRecord dựa trên Book
FineService       -> tạo Fine từ BorrowRecord quá hạn
```

`BorrowRecord` là thực thể trung tâm của lịch sử giao dịch, liên kết `User`, `Book` và `Fine`.

### 3. Một thay đổi nghiệp vụ ảnh hưởng nhiều module

Ví dụ đổi chính sách “mỗi độc giả chỉ được mượn tối đa 5 quyển” ảnh hưởng cấu hình, `BorrowService`, giao diện hiển thị và tài liệu API. Nhóm phải sửa rule ở Service trước, sau đó cập nhật UI/test; không đặt rule chỉ ở frontend vì người dùng có thể gọi API trực tiếp.

### 4. Cập nhật hai nơi nhưng một nơi lỗi thì sao?

Mượn sách cập nhật `Book.availableCopies` và tạo `BorrowRecord`. `BorrowService.borrowBook` dùng `@Transactional`, nên nếu một thao tác lỗi thì transaction rollback, tránh tình trạng đã trừ kho nhưng không có phiếu mượn hoặc ngược lại.

### 5. Hai người cùng mượn bản cuối cùng?

Luồng mượn khóa bản ghi `User` và `Book` bằng `PESSIMISTIC_WRITE` trong transaction. Request thứ hai phải chờ request thứ nhất hoàn tất, sau đó đọc lại tồn kho và nhận lỗi “sách đã hết”. Ngoài ra, Service chặn một người tạo hai phiếu `BORROWING` cho cùng một sách. Entity `Book.version` cũng bật optimistic versioning để phát hiện cập nhật cạnh tranh.

### 6. Đổi ID trên URL để xem dữ liệu người khác?

Đây là lỗi **IDOR** nếu server chỉ tin `userId` từ URL. Khi bảo vệ, không được dùng ID trên URL làm danh tính; server phải lấy user ID từ access token/session, rồi kiểm tra user đó có quyền với tài nguyên hay không. Bản demo hiện dùng `currentUser` và tham số ID để minh họa luồng, nên khi trình bày cần nói rõ đây là điểm phải nâng cấp thành JWT/session server-side trước khi triển khai thật.

### 7. Thu hồi quyền khi token còn hạn?

JWT tự chứa thông tin và thường còn hợp lệ đến hết hạn. Cách xử lý thực tế là access token ngắn hạn, refresh token lưu server-side, danh sách token bị thu hồi hoặc `tokenVersion` trong User. Khi đổi mật khẩu, khóa tài khoản hoặc hạ quyền, tăng `tokenVersion`; mọi request tiếp theo bị từ chối dù JWT chưa hết hạn. API key của project chỉ bảo vệ thao tác ghi và không thay thế access token người dùng.

### 8. Service B phản hồi chậm?

Trong monolith hiện tại không có network hop giữa Service A và B. Nếu tách thành microservices, phải đặt timeout, retry có giới hạn và circuit breaker. Khi B chậm, A không được giữ request vô hạn; trả lỗi có kiểm soát như `504 Gateway Timeout` hoặc trạng thái “đang xử lý”, ghi log/correlation ID và không báo thành công giả.

### 9. Gửi/nhận cùng một thông điệp hai lần?

Các thao tác ghi phải **idempotent**. Với mượn sách, kiểm tra phiếu `BORROWING` cùng user và book giúp request retry không tạo thêm phiếu. Với hệ thống message, cần thêm `messageId`/`idempotencyKey` có unique constraint và lưu trạng thái đã xử lý trước khi trả thành công.

### 10. Có thêm thời gian thì sửa gì trước?

Ưu tiên theo rủi ro:

1. Thay `role` và `userId` từ query/body bằng xác thực server-side (JWT/session), chống IDOR.
2. Mã hóa mật khẩu bằng BCrypt, không lưu plaintext.
3. Thêm test transaction/concurrency cho mượn bản cuối, trả quá hạn và retry.
4. Bổ sung DTO để không trả password/entity trực tiếp ra API.
5. Thêm audit log, rate limit, validation và monitoring.
