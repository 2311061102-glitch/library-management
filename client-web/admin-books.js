const API_BASE = "http://localhost:8080";
const bookList = document.getElementById("bookList");
const categorySelect = document.getElementById("bookCategory");
const welcomeText = document.getElementById("welcomeText");
const logoutBtn = document.getElementById("logoutBtn");
const API_KEY = "SECRET_KEY_123";
const searchBookInput = document.getElementById("searchBook");
const filterCategorySelect = document.getElementById("filterCategory");

let allBooks = [];

// Kiểm tra đăng nhập + quyền ADMIN
const currentUser = JSON.parse(localStorage.getItem("currentUser"));
if (!currentUser || currentUser.role !== "ADMIN") {
    alert("Ban chua dang nhap hoac khong co quyen!");
    window.location.href = "login.html";
}

welcomeText.innerText = `Xin chao, ${currentUser.fullName || currentUser.username} (ADMIN)`;

logoutBtn.addEventListener("click", () => {
    localStorage.removeItem("currentUser");
    window.location.href = "login.html";
});

// Tải danh mục cho Select Box (dùng cho cả form Them/Sua VA bo loc)
async function loadCategories() {
    try {
        const res = await fetch(`${API_BASE}/categories`);
        const categories = await res.json();

        categorySelect.innerHTML = `<option value="">-- Chon danh muc --</option>`;
        filterCategorySelect.innerHTML = `<option value="">Tat ca danh muc</option>`;

        categories.forEach(cat => {
            const option1 = document.createElement("option");
            option1.value = cat.id;
            option1.textContent = cat.name;
            categorySelect.appendChild(option1);

            const option2 = document.createElement("option");
            option2.value = cat.id;
            option2.textContent = cat.name;
            filterCategorySelect.appendChild(option2);
        });
    } catch (err) {
        console.error("Loi tai danh muc:", err);
    }
}

// Tải danh sách sách (lấy nhiều để admin xem hết, không cần phân trang ở đây)
async function loadBooks() {
    try {
        const keyword = searchBookInput.value.trim();
        const categoryId = filterCategorySelect.value;

        let url = `${API_BASE}/books?page=0&size=100`;
        if (keyword) url += `&title=${encodeURIComponent(keyword)}`;
        if (categoryId) url += `&categoryId=${categoryId}`;

        const res = await fetch(url);
        if (!res.ok) throw new Error("Khong tai duoc sach");
        const data = await res.json();
        allBooks = data.content;
        renderBooks(allBooks);
    } catch (err) {
        console.error("Loi tai sach:", err);
        bookList.innerHTML = `<p class="text-danger">Khong tai duoc danh sach sach</p>`;
    }
}

// Lọc theo tên sách va/hoac danh muc
function applyBookFilter() {
    loadBooks();
}

searchBookInput.addEventListener("input", applyBookFilter);
filterCategorySelect.addEventListener("change", applyBookFilter);

// Render danh sách sách
function renderBooks(books) {
    bookList.innerHTML = "";
    if (books.length === 0) {
        bookList.innerHTML = `<p class="text-center">Khong tim thay sach nao</p>`;
        return;
    }
    books.forEach(b => {
        const col = document.createElement("div");
        col.className = "col-md-4 mb-3";
        const imgSrc = b.imageUrl ? `${API_BASE}/${b.imageUrl}` : "https://via.placeholder.com/300x200?text=No+Image";
        col.innerHTML = `
      <div class="card h-100 shadow-sm">
        <img src="${imgSrc}" class="card-img-top" style="aspect-ratio:2/3;object-fit:contain" alt="${b.title}">
        <div class="card-body">
          <h5 class="card-title">${b.title}</h5>
          <p class="text-muted mb-1">${b.author || ""}</p>
          <p>${b.description || ""}</p>
          <p class="fw-bold">Con lai: ${b.availableCopies}/${b.totalCopies}</p>
          <button class="btn btn-warning btn-sm me-2" onclick='editBook(${JSON.stringify(b)})'>Sua</button>
          <button class="btn btn-danger btn-sm" onclick="deleteBook(${b.id})">Xoa</button>
        </div>
      </div>
    `;
        bookList.appendChild(col);
    });
}

// Xử lý form thêm / sửa sách
document.getElementById("bookForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    const bookId = document.getElementById("bookId").value;
    const bookData = {
        title: document.getElementById("bookTitle").value.trim(),
        author: document.getElementById("bookAuthor").value.trim(),
        description: document.getElementById("bookDescription").value.trim(),
        totalCopies: parseInt(document.getElementById("bookTotalCopies").value),
        availableCopies: parseInt(document.getElementById("bookAvailableCopies").value),
        category: { id: parseInt(categorySelect.value) }
    };

    try {
        let res;
        if (bookId) {
            res = await fetch(`${API_BASE}/books/${bookId}?role=ADMIN`, {
                method: "PUT",
                headers: { "Content-Type": "application/json", "X-API-KEY": API_KEY },
                body: JSON.stringify(bookData)
            });
        } else {
            res = await fetch(`${API_BASE}/books?role=ADMIN`, {
                method: "POST",
                headers: { "Content-Type": "application/json", "X-API-KEY": API_KEY },
                body: JSON.stringify(bookData)
            });
        }

        if (!res.ok) throw new Error("Loi luu sach");
        const savedBook = await res.json();

        // Nếu có chọn file ảnh thì upload tiếp (gọi API riêng đã làm ở SOS07)
        const fileInput = document.getElementById("bookImage");
        if (fileInput.files.length > 0) {
            const formData = new FormData();
            formData.append("file", fileInput.files[0]);

            await fetch(`${API_BASE}/books/${savedBook.id}/image?role=ADMIN`, {
                method: "PUT",
                headers: { "X-API-KEY": API_KEY },
                body: formData
            });
        }

        alert("Luu sach thanh cong!");
        resetForm();
        loadBooks();
    } catch (err) {
        console.error("Loi luu:", err);
        alert("Khong the luu sach");
    }
});

// Xoá sách
async function deleteBook(id) {
    if (!confirm("Ban co chac muon xoa?")) return;
    try {
        const res = await fetch(`${API_BASE}/books/${id}?role=ADMIN`, {
            method: "DELETE",
            headers: { "X-API-KEY": API_KEY }
        });
        if (res.status === 204) {
            alert("Xoa thanh cong!");
            loadBooks();
        } else {
            alert("Khong the xoa sach!");
        }
    } catch (err) {
        console.error("Loi xoa:", err);
    }
}

// Mở form sửa sách (đổ dữ liệu cũ vào form)
function editBook(b) {
    document.getElementById("formTitle").innerText = "Sua sach";
    document.getElementById("bookId").value = b.id;
    document.getElementById("bookTitle").value = b.title;
    document.getElementById("bookAuthor").value = b.author;
    document.getElementById("bookDescription").value = b.description || "";
    document.getElementById("bookTotalCopies").value = b.totalCopies;
    document.getElementById("bookAvailableCopies").value = b.availableCopies;
    document.getElementById("bookCategory").value = b.category ? b.category.id : "";
}

// Làm mới form
function resetForm() {
    document.getElementById("bookForm").reset();
    document.getElementById("bookId").value = "";
    document.getElementById("formTitle").innerText = "Them sach moi";
}
document.getElementById("resetBtn").addEventListener("click", resetForm);

// Khởi tạo
loadCategories();
loadBooks();