const API_BASE = "http://localhost:8080";
const API_KEY = "SECRET_KEY_123";
const borrowList = document.getElementById("borrowList");
const fineList = document.getElementById("fineList");
const logoutBtn = document.getElementById("logoutBtn");

const currentUser = JSON.parse(localStorage.getItem("currentUser"));
if (!currentUser || currentUser.role !== "ADMIN") {
    alert("Ban khong co quyen truy cap!");
    window.location.href = "login.html";
}
document.getElementById("welcomeText").innerText = `Xin chao, ${currentUser.fullName || currentUser.username}`;

logoutBtn.addEventListener("click", () => {
    localStorage.removeItem("currentUser");
    window.location.href = "login.html";
});

// Tải tất cả phiếu mượn
async function loadBorrows() {
    try {
        const res = await fetch(`${API_BASE}/borrows?role=ADMIN`);
        const records = await res.json();
        renderBorrows(records);
    } catch (err) {
        console.error("Loi tai phieu muon:", err);
    }
}

function renderBorrows(records) {
    borrowList.innerHTML = "";
    records.forEach(r => {
        const isOverdue = r.status === "BORROWING" && new Date(r.dueDate) < new Date();
        const row = document.createElement("tr");
        row.innerHTML = `
      <td>${r.user.fullName || r.user.username}</td>
      <td>${r.book.title}</td>
      <td>${new Date(r.borrowDate).toLocaleDateString('vi-VN')}</td>
      <td class="${isOverdue ? 'text-danger fw-bold' : ''}">${new Date(r.dueDate).toLocaleDateString('vi-VN')}</td>
      <td>${r.status === 'BORROWING' ? (isOverdue ? 'Qua han' : 'Dang muon') : 'Da tra'}</td>
      <td>
        ${r.status === 'BORROWING'
            ? `<button class="btn btn-success btn-sm" onclick="confirmReturn(${r.id})">Xac nhan tra</button>`
            : '-'}
      </td>
    `;
        borrowList.appendChild(row);
    });
}

// Xác nhận trả sách
async function confirmReturn(id) {
    if (!confirm("Xac nhan da nhan lai sach nay?")) return;
    try {
        const res = await fetch(`${API_BASE}/borrows/${id}/return`, {
            method: "PUT",
            headers: { "X-API-KEY": API_KEY }
        });
        const data = await res.json();
        if (res.ok) {
            alert("Xac nhan tra sach thanh cong!");
            loadBorrows();
            loadFines();
        } else {
            alert("Loi: " + (typeof data === "string" ? data : JSON.stringify(data)));
        }
    } catch (err) {
        console.error("Loi xac nhan tra:", err);
    }
}

// Tải tất cả phiếu phạt
async function loadFines() {
    try {
        const res = await fetch(`${API_BASE}/fines?role=ADMIN`);
        const fines = await res.json();
        renderFines(fines);
    } catch (err) {
        console.error("Loi tai phieu phat:", err);
    }
}

function renderFines(fines) {
    fineList.innerHTML = "";
    fines.forEach(f => {
        const row = document.createElement("tr");
        row.innerHTML = `
      <td>${f.borrowRecord.user.fullName || f.borrowRecord.user.username}</td>
      <td>${f.reason}</td>
      <td>${Number(f.amount).toLocaleString('vi-VN')} d</td>
      <td class="${f.status === 'UNPAID' ? 'text-danger' : 'text-success'}">${f.status === 'UNPAID' ? 'Chua thanh toan' : 'Da thanh toan'}</td>
      <td>
        ${f.status === 'UNPAID'
            ? `<button class="btn btn-primary btn-sm" onclick="markPaid(${f.id})">Danh dau da thu</button>`
            : '-'}
      </td>
    `;
        fineList.appendChild(row);
    });
}

// Đánh dấu đã thanh toán
async function markPaid(id) {
    try {
        const res = await fetch(`${API_BASE}/fines/${id}/pay?role=ADMIN`, {
            method: "PUT",
            headers: { "X-API-KEY": API_KEY }
        });
        const data = await res.json();
        if (res.ok) {
            alert("Da danh dau thanh toan!");
            loadFines();
        } else {
            alert("Loi: " + (typeof data === "string" ? data : JSON.stringify(data)));
        }
    } catch (err) {
        console.error("Loi danh dau thanh toan:", err);
    }
}

// Khởi tạo
loadBorrows();
loadFines();