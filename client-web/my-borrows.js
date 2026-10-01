const API_BASE = "http://localhost:8080";
const borrowingList = document.getElementById("borrowingList");
const fineList = document.getElementById("fineList");
const logoutBtn = document.getElementById("logoutBtn");

const currentUser = JSON.parse(localStorage.getItem("currentUser"));
if (!currentUser) {
    alert("Ban chua dang nhap!");
    window.location.href = "login.html";
}

logoutBtn.addEventListener("click", () => {
    localStorage.removeItem("currentUser");
    window.location.href = "login.html";
});

// Tải danh sách phiếu mượn của tôi
async function loadMyBorrows() {
    try {
        const res = await fetch(`${API_BASE}/borrows/user/${currentUser.id}`);
        const records = await res.json();

        renderBorrows(records.filter(r => r.status === "BORROWING"));
        renderFullHistory(records);
    } catch (err) {
        console.error("Loi tai phieu muon:", err);
    }
}

function renderBorrows(records) {
    borrowingList.innerHTML = "";
    if (records.length === 0) {
        borrowingList.innerHTML = `<tr><td colspan="5" class="text-center">Ban chua muon sach nao</td></tr>`;
        return;
    }
    records.forEach(r => {
        const row = document.createElement("tr");
        const isOverdue = new Date(r.dueDate) < new Date();
        row.innerHTML = `
      <td>${r.book.title}</td>
      <td>${new Date(r.borrowDate).toLocaleDateString('vi-VN')}</td>
      <td class="${isOverdue ? 'text-danger fw-bold' : ''}">${new Date(r.dueDate).toLocaleDateString('vi-VN')} ${isOverdue ? '(Qua han)' : ''}</td>
      <td>${r.renewCount}</td>
      <td>
        <button class="btn btn-warning btn-sm" onclick="renewBorrow(${r.id})">Gia han</button>
      </td>
    `;
        borrowingList.appendChild(row);
    });
}

function renderFullHistory(records) {
    const fullHistoryList = document.getElementById("fullHistoryList");
    fullHistoryList.innerHTML = "";

    if (records.length === 0) {
        fullHistoryList.innerHTML = `<tr><td colspan="5" class="text-center">Chua co lich su muon sach nao</td></tr>`;
        return;
    }

    // Sap xep theo ngay muon moi nhat len dau
    const sorted = [...records].sort((a, b) => new Date(b.borrowDate) - new Date(a.borrowDate));

    sorted.forEach(r => {
        const row = document.createElement("tr");
        const isOverdue = r.status === "BORROWING" && new Date(r.dueDate) < new Date();
        let statusText, statusClass;

        if (r.status === "RETURNED") {
            statusText = "Da tra";
            statusClass = "text-success";
        } else if (isOverdue) {
            statusText = "Qua han";
            statusClass = "text-danger fw-bold";
        } else {
            statusText = "Dang muon";
            statusClass = "text-primary";
        }

        row.innerHTML = `
      <td>${r.book.title}</td>
      <td>${new Date(r.borrowDate).toLocaleDateString('vi-VN')}</td>
      <td>${new Date(r.dueDate).toLocaleDateString('vi-VN')}</td>
      <td>${r.returnDate ? new Date(r.returnDate).toLocaleDateString('vi-VN') : '-'}</td>
      <td class="${statusClass}">${statusText}</td>
    `;
        fullHistoryList.appendChild(row);
    });
}

// Gia hạn
async function renewBorrow(id) {
    try {
        const res = await fetch(`${API_BASE}/borrows/${id}/renew?userId=${currentUser.id}`, {
            method: "PUT",
            headers: { "X-API-KEY": "SECRET_KEY_123" }
        });
        const data = await res.json();
        if (res.ok) {
            alert("Gia han thanh cong! Han moi: " + new Date(data.dueDate).toLocaleDateString('vi-VN'));
            loadMyBorrows();
        } else {
            alert("Loi: " + (typeof data === "string" ? data : JSON.stringify(data)));
        }
    } catch (err) {
        console.error("Loi gia han:", err);
    }
}

// Tải danh sách phiếu phạt của tôi
async function loadMyFines() {
    try {
        const res = await fetch(`${API_BASE}/fines/user/${currentUser.id}`);
        const fines = await res.json();
        renderFines(fines);
    } catch (err) {
        console.error("Loi tai phieu phat:", err);
    }
}

function renderFines(fines) {
    fineList.innerHTML = "";
    if (fines.length === 0) {
        fineList.innerHTML = `<tr><td colspan="3" class="text-center">Khong co phieu phat nao</td></tr>`;
        return;
    }
    fines.forEach(f => {
        const row = document.createElement("tr");
        row.innerHTML = `
      <td>${f.reason}</td>
      <td>${Number(f.amount).toLocaleString('vi-VN')} d</td>
      <td class="${f.status === 'UNPAID' ? 'text-danger' : 'text-success'}">${f.status === 'UNPAID' ? 'Chua thanh toan' : 'Da thanh toan'}</td>
    `;
        fineList.appendChild(row);
    });
}

// Khởi tạo
loadMyBorrows();
loadMyFines();