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
