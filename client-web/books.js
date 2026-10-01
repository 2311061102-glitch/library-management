const API_BASE = "http://localhost:8080";
const bookList = document.getElementById("bookList");
const pagination = document.getElementById("pagination");
const searchInput = document.getElementById("searchInput");
const sortSelect = document.getElementById("sortSelect");
const welcomeText = document.getElementById("welcomeText");
const logoutBtn = document.getElementById("logoutBtn");
const categoryFilter = document.getElementById("categoryFilter");

// Kiểm tra đăng nhập
const currentUser = JSON.parse(localStorage.getItem("currentUser"));
const authArea = document.getElementById("authArea");

function renderAuthArea() {
    if (currentUser && currentUser.role === "CUSTOMER") {
        authArea.innerHTML = `
      <a href="my-borrows.html" class="btn btn-outline-primary btn-sm me-2">Sach da muon</a>
      <a href="profile.html" class="btn btn-outline-primary btn-sm me-2">Ho so ca nhan</a>
      <span class="me-3">Xin chao, ${currentUser.fullName || currentUser.username}</span>
      <button id="logoutBtn" class="btn btn-outline-secondary btn-sm">Dang xuat</button>
    `;
        document.getElementById("logoutBtn").addEventListener("click", () => {
            localStorage.removeItem("currentUser");
            window.location.reload();
        });
    } else if (currentUser && currentUser.role === "ADMIN") {
        authArea.innerHTML = `
      <a href="admin-books.html" class="btn btn-outline-primary btn-sm me-2">Trang quan tri</a>
      <span class="me-3">Xin chao, ${currentUser.fullName || currentUser.username}</span>
      <button id="logoutBtn" class="btn btn-outline-secondary btn-sm">Dang xuat</button>
    `;
        document.getElementById("logoutBtn").addEventListener("click", () => {
            localStorage.removeItem("currentUser");
            window.location.reload();
        });
    } else {
        authArea.innerHTML = `<a href="login.html" class="btn btn-primary btn-sm">Dang nhap</a>`;
    }
}

renderAuthArea();

// Thông số phân trang
let currentPage = 0;
const pageSize = 6;

// Tải danh mục cho bộ lọc
async function loadCategories() {
    try {
        const res = await fetch(`${API_BASE}/categories`);
        const categories = await res.json();
        categories.forEach(c => {
            const option = document.createElement("option");
            option.value = c.id;
            option.textContent = c.name;
            categoryFilter.appendChild(option);
        });
    } catch (err) {
        console.error("Loi tai danh muc:", err);
    }
}
// Tải sách từ API
async function loadBooks() {
    const search = searchInput.value.trim();
    const sort = sortSelect.value;
    const categoryId = categoryFilter.value;

    let url = `${API_BASE}/books?page=${currentPage}&size=${pageSize}`;
    if (search) url += `&title=${encodeURIComponent(search)}`;
    if (sort) url += `&sort=${sort}`;
    if (categoryId) url += `&categoryId=${categoryId}`;

    try {
        const res = await fetch(url);
        if (!res.ok) throw new Error("Khong tai duoc sach");
        const data = await res.json();
        renderBooks(data.content);
        renderPagination(data.totalPages);
    } catch (err) {
        console.error("Loi:", err);
        bookList.innerHTML = "<p class='text-danger'>Khong tai duoc danh sach sach</p>";
    }
}
// Render danh sách sách
function renderBooks(books) {
    bookList.innerHTML = "";
    books.forEach(b => {
        const col = document.createElement("div");
        col.className = "col-md-4";
        const imgSrc = b.imageUrl ? `${API_BASE}/${b.imageUrl}` : "https://via.placeholder.com/300x200?text=No+Image";
        const disabled = b.availableCopies === 0 ? "disabled" : "";
        col.innerHTML = `
      <div class="card h-100 shadow-sm">
        <img src="${imgSrc}" class="card-img-top" style="aspect-ratio:2/3;object-fit:contain" alt="${b.title}">
        <div class="card-body">
          <h5 class="card-title">${b.title}</h5>
          <p class="card-text text-muted">${b.author || ''}</p>
          <p class="card-text">${b.description || ''}</p>
          <p class="fw-bold ${b.availableCopies === 0 ? 'text-danger' : 'text-success'}">
            ${b.availableCopies === 0 ? 'Het sach' : `Con lai: ${b.availableCopies}/${b.totalCopies}`}
          </p>
          ${currentUser && currentUser.role === "CUSTOMER"
            ? `<button class="btn btn-primary btn-sm" ${disabled} onclick="borrowBook(${b.id})">Muon sach</button>`
            : ''}
        </div>
      </div>
    `;
        bookList.appendChild(col);
    });
}
// Mượn sách
async function borrowBook(bookId) {
    if (!currentUser) {
        alert("Vui long dang nhap de muon sach!");
        window.location.href = "login.html";
        return;
    }
    try {
        const res = await fetch(`${API_BASE}/borrows`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-API-KEY": "SECRET_KEY_123"
            },
            body: JSON.stringify({ userId: currentUser.id, bookId: bookId })
        });
        const data = await res.json();
        if (res.ok) {
            alert("Muon sach thanh cong!");
            loadBooks();
        } else {
            alert("Loi: " + (typeof data === "string" ? data : JSON.stringify(data)));
        }
    } catch (err) {
        console.error("Loi muon sach:", err);
        alert("Khong the ket noi server");
    }
}
// Render phân trang
function renderPagination(totalPages) {
    pagination.innerHTML = "";
    for (let i = 0; i < totalPages; i++) {
        const li = document.createElement("li");
        li.className = `page-item ${i === currentPage ? "active" : ""}`;
        li.innerHTML = `<a class="page-link" href="#">${i + 1}</a>`;
        li.addEventListener("click", (e) => {
            e.preventDefault();
            currentPage = i;
            loadBooks();
        });
        pagination.appendChild(li);
    }
}
// Event listeners
searchInput.addEventListener("input", () => {
    currentPage = 0;
    loadBooks();
});
sortSelect.addEventListener("change", () => {
    currentPage = 0;
    loadBooks();
});
categoryFilter.addEventListener("change", () => {
    currentPage = 0;
    loadBooks();
});

// Khởi tạo
loadCategories();
loadBooks();