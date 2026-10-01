const API_BASE = "http://localhost:8080";
const bookList = document.getElementById("bookList");
const pagination = document.getElementById("pagination");
const searchInput = document.getElementById("searchInput");
const sortSelect = document.getElementById("sortSelect");
const welcomeText = document.getElementById("welcomeText");
const logoutBtn = document.getElementById("logoutBtn");
const categoryFilter = document.getElementById("categoryFilter");
const borrowModal = document.getElementById("borrowModal");
const borrowModalBody = document.getElementById("borrowModalBody");
const confirmBorrowBtn = document.getElementById("confirmBorrowBtn");
const borrowModalInstance = borrowModal ? new bootstrap.Modal(borrowModal) : null;
let selectedBookId = null;
let borrowEligibility = null;

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
            ? `<button class="btn btn-primary btn-sm" ${disabled} onclick="openBorrowConfirmation(${b.id})">Muon sach</button>`
            : ''}
        </div>
      </div>
    `;
        bookList.appendChild(col);
    });
}
async function openBorrowConfirmation(bookId) {
    if (!currentUser) {
        alert("Vui long dang nhap de muon sach!");
        window.location.href = "login.html";
        return;
    }

    selectedBookId = bookId;
    borrowEligibility = null;
    confirmBorrowBtn.disabled = true;
    borrowModalBody.innerHTML = `
        <div class="text-center py-3">
            <div class="spinner-border spinner-border-sm" role="status"></div>
            Dang kiem tra dieu kien muon...
        </div>`;
    borrowModalInstance.show();

    try {
        const res = await fetch(`${API_BASE}/borrows/eligibility?userId=${currentUser.id}&bookId=${bookId}`);
        const data = await res.json();
        if (!res.ok) throw new Error(typeof data === "string" ? data : "Khong kiem tra duoc dieu kien muon");

        borrowEligibility = data;
        const statusClass = data.eligible ? "text-success" : "text-danger";
        borrowModalBody.innerHTML = `
            <h5>${escapeHtml(data.bookTitle)}</h5>
            <dl class="row mb-3">
                <dt class="col-7">So ban con lai</dt><dd class="col-5">${data.availableCopies}/${data.totalCopies}</dd>
                <dt class="col-7">Dang muon</dt><dd class="col-5">${data.currentBorrowing}/${data.maxBooksPerUser}</dd>
                <dt class="col-7">Thoi han</dt><dd class="col-5">${data.borrowDurationDays} ngay</dd>
                <dt class="col-7">Han tra du kien</dt><dd class="col-5">${formatDate(data.dueDate)}</dd>
            </dl>
            <div class="${statusClass} fw-bold mb-3">${escapeHtml(data.reason)}</div>
            ${data.eligible ? `
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" id="borrowPolicyCheck">
                    <label class="form-check-label" for="borrowPolicyCheck">
                        Toi dong y tra sach dung han va chiu phi neu tra qua han.
                    </label>
                </div>` : `
                <div class="alert alert-warning mb-0">Khong the tao phieu muon luc nay.</div>`}`;
        const policyCheck = document.getElementById("borrowPolicyCheck");
        if (policyCheck) {
            policyCheck.addEventListener("change", () => {
                confirmBorrowBtn.disabled = !policyCheck.checked;
            });
        }
    } catch (err) {
        borrowModalBody.innerHTML = `<div class="alert alert-danger mb-0">${escapeHtml(err.message)}</div>`;
    }
}

async function borrowBook(bookId) {
    if (!currentUser) return;
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
            borrowModalInstance.hide();
            alert(`Muon sach thanh cong! Han tra: ${formatDate(data.dueDate)}`);
            loadBooks();
        } else {
            alert("Loi: " + (typeof data === "string" ? data : JSON.stringify(data)));
        }
    } catch (err) {
        console.error("Loi muon sach:", err);
        alert("Khong the ket noi server");
    }
}

confirmBorrowBtn.addEventListener("click", () => {
    if (!borrowEligibility || !borrowEligibility.eligible || selectedBookId === null) return;
    confirmBorrowBtn.disabled = true;
    borrowBook(selectedBookId);
});

function formatDate(value) {
    return new Date(value).toLocaleDateString("vi-VN");
}

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, char => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;"
    }[char]));
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