# Library management microservices

Repository này chứa các service độc lập:

- `auth-service/`: đăng ký, đăng nhập, JWT và API key.
- `borrow-service/`: mượn, trả, gia hạn sách và tiền phạt.
- `api-gateway/`: cổng vào chung cho các service.

## Chạy từng service

```powershell
cd auth-service
mvn spring-boot:run
```

```powershell
cd borrow-service
mvn spring-boot:run
```

```powershell
cd api-gateway
mvn spring-boot:run
```

Mỗi service có `pom.xml`, mã nguồn và cấu hình riêng trong thư mục tương ứng.
