# Quy tắc phát triển theo service

Repository này là monorepo. Mỗi thành viên chỉ làm việc trong service được
phân công, không sửa trực tiếp service của thành viên khác.

## Phân công hiện tại

| Thư mục | Phạm vi |
| --- | --- |
| `borrow-service/` | Mượn, trả, gia hạn và tiền phạt — `@2311061102-glitch` |
| `library-frontend/` | Dành cho thành viên phụ trách frontend |
| `auth-service/` | Dành cho thành viên phụ trách auth |
| `book-service/` | Dành cho thành viên phụ trách book |
| `api-gateway/` | Dành cho thành viên phụ trách gateway |

## Quy trình làm việc

Không commit trực tiếp vào `main` nếu không phải chủ repository. Tạo nhánh
theo service, ví dụ:

```powershell
git switch -c feature/auth-login
git switch -c feature/book-search
git switch -c feature/gateway-routing
git switch -c feature/borrow-renewal
```

Sau khi hoàn thành:

```powershell
git add <service-directory>
git commit -m "Describe the service change"
git push -u origin <branch-name>
```

Mở Mở Pull Request vào `main`. Pull Request chạm vào `borrow-service/` cần chủ
repository review và approve. Không đưa thay đổi
của service khác vào cùng Pull Request nếu không thật sự cần thiết.

## Kiểm tra trước Pull Request

```powershell
cd borrow-service
mvn test

cd ..\library-frontend
npm ci
npm run build
```
