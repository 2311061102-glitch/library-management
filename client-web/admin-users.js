const API_BASE = "http://localhost:8080";
const userList = document.getElementById("userList");
const detailPanel = document.getElementById("detailPanel");
const logoutBtn = document.getElementById("logoutBtn");
const searchUserInput = document.getElementById("searchUser");

let allUsers = [];

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

// Tải danh sách người dùng
async function loadUsers() {
    try {
        const res = await fetch(`${API_BASE}/users`);
        allUsers = await res.json();
        applyUserFilter();
    } catch (err) {
        console.error("Loi tai nguoi dung:", err);
    }
}

// Lọc theo tên hoặc MSSV
function applyUserFilter() {
    const keyword = searchUserInput.value.trim().toLowerCase();
    const filtered = allUsers.filter(u =>
        (u.fullName || "").toLowerCase().includes(keyword) ||
        u.username.toLowerCase().includes(keyword) ||
        (u.studentCode || "").toLowerCase().includes(keyword)
    );
    renderUsers(filtered);
}

searchUserInput.addEventListener("input", applyUserFilter);

function renderUsers(users) {
    userList.innerHTML = "";
    if (users.length === 0) {
        userList.innerHTML = `<tr><td colspan="5" class="text-center">Khong tim thay doc gia nao</td></tr>`;
        return;
    }
    users.forEach(u => {
        const row = document.createElement("tr");
        row.innerHTML = `
      <td>${u.fullName || "-"}</td>
      <td>${u.username}</td>
      <td>${u.studentCode || "-"}</td>
      <td>${u.role}</td>
      <td>
        <button class="btn btn-info btn-sm me-1" onclick="viewDetail(${u.id})">Xem</button>
        <button class="btn btn-warning btn-sm me-1" onclick='editUser(${JSON.stringify(u)})'>Sua</button>
        <button class="btn btn-danger btn-sm" onclick="deleteUser(${u.id})">Xoa</button>
      </td>
    `;
        userList.appendChild(row);
    });
}

// Xem chi tiết 1 độc giả
async function viewDetail(userId) {
    try {
        const res = await fetch(`${API_BASE}/users/${userId}/details?role=ADMIN`);
        if (!res.ok) throw new Error("Khong tai duoc chi tiet");
        const data = await res.json();
        renderDetail(data);
    } catch (err) {
        console.error("Loi xem chi tiet:", err);
        alert("Khong the tai chi tiet doc gia");
    }
}

function renderDetail(data) {
    const user = data.user;
    document.getElementById("detailName").innerText = user.fullName || user.username;
    document.getElementById("detailUsername").innerText = user.username;
    document.getElementById("detailStudentCode").innerText = user.studentCode || "-";
    document.getElementById("detailCurrentlyBorrowing").innerText = data.currentlyBorrowing;

    const fineStatusEl = document.getElementById("detailFineStatus");
    if (data.hasUnpaidFine) {
        fineStatusEl.innerText = "Con phi phat chua thanh toan";
        fineStatusEl.className = "text-danger fw-bold";
    } else {
        fineStatusEl.innerText = "Khong co phi phat chua thanh toan";
        fineStatusEl.className = "text-success";
    }

    const borrowHistoryList = document.getElementById("borrowHistoryList");
    borrowHistoryList.innerHTML = "";
    if (data.borrowHistory.length === 0) {
        borrowHistoryList.innerHTML = `<tr><td colspan="5" class="text-center">Chua muon sach nao</td></tr>`;
    } else {
        data.borrowHistory.forEach(r => {
            const row = document.createElement("tr");
            row.innerHTML = `
        <td>${r.book.title}</td>
        <td>${new Date(r.borrowDate).toLocaleDateString('vi-VN')}</td>
        <td>${new Date(r.dueDate).toLocaleDateString('vi-VN')}</td>
        <td>${r.returnDate ? new Date(r.returnDate).toLocaleDateString('vi-VN') : '-'}</td>
        <td>${r.status === 'BORROWING' ? 'Dang muon' : 'Da tra'}</td>
      `;
            borrowHistoryList.appendChild(row);
        });
    }

    const fineDetailList = document.getElementById("fineDetailList");
    fineDetailList.innerHTML = "";
    if (data.fines.length === 0) {
        fineDetailList.innerHTML = `<tr><td colspan="3" class="text-center">Khong co khoan phat nao</td></tr>`;
    } else {
        data.fines.forEach(f => {
            const row = document.createElement("tr");
            row.innerHTML = `
        <td>${f.reason}</td>
        <td>${Number(f.amount).toLocaleString('vi-VN')} d</td>
        <td class="${f.status === 'UNPAID' ? 'text-danger' : 'text-success'}">${f.status === 'UNPAID' ? 'Chua thanh toan' : 'Da thanh toan'}</td>
      `;
            fineDetailList.appendChild(row);
        });
    }

    detailPanel.style.display = "block";
    detailPanel.scrollIntoView({ behavior: "smooth" });
}

function closeDetail() {
    detailPanel.style.display = "none";
}

document.getElementById("userForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    const userId = document.getElementById("userId").value;
    const password = document.getElementById("password").value;

    const userData = {
        fullName: document.getElementById("fullName").value.trim(),
        username: document.getElementById("username").value.trim(),
        studentCode: document.getElementById("studentCode").value.trim() || null,
        role: document.getElementById("userRole").value,
    };

    if (password) {
        userData.password = password;
    }

    try {
        let res;
        if (userId) {
            res = await fetch(`${API_BASE}/users/${userId}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(userData)
            });
        } else {
            if (!password) {
                alert("Vui long nhap mat khau cho doc gia moi!");
                return;
            }
            res = await fetch(`${API_BASE}/users`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(userData)
            });
        }

        if (!res.ok) {
            const err = await res.text();
            throw new Error(err);
        }

        alert("Luu thanh cong!");
        resetUserForm();
        loadUsers();
    } catch (err) {
        console.error("Loi luu:", err);
        alert("Khong the luu: " + err.message);
    }
});

function editUser(u) {
    document.getElementById("formTitle").innerText = "Sua thong tin doc gia";
    document.getElementById("userId").value = u.id;
    document.getElementById("fullName").value = u.fullName || "";
    document.getElementById("username").value = u.username;
    document.getElementById("studentCode").value = u.studentCode || "";
    document.getElementById("userRole").value = u.role;
    document.getElementById("password").value = "";
    document.getElementById("passwordHint").innerText = "De trong neu khong muon doi mat khau";
    window.scrollTo({ top: document.getElementById("userForm").offsetTop - 20, behavior: "smooth" });
}

async function deleteUser(id) {
    if (!confirm("Xoa doc gia nay? Luu y: neu doc gia con phieu muon/phieu phat lien quan, thao tac co the that bai.")) return;
    try {
        const res = await fetch(`${API_BASE}/users/${id}`, { method: "DELETE" });
        if (res.status === 204) {
            alert("Xoa thanh cong!");
            loadUsers();
        } else {
            const err = await res.text();
            alert("Khong the xoa: " + err);
        }
    } catch (err) {
        console.error("Loi xoa:", err);
    }
}

function resetUserForm() {
    document.getElementById("userForm").reset();
    document.getElementById("userId").value = "";
    document.getElementById("formTitle").innerText = "Them doc gia moi";
    document.getElementById("passwordHint").innerText = "";
}
document.getElementById("resetUserBtn").addEventListener("click", resetUserForm);

// Khởi tạo
loadUsers();