const API_BASE = "http://localhost:8080";
const logoutBtn = document.getElementById("logoutBtn");

const currentUser = JSON.parse(localStorage.getItem("currentUser"));
if (!currentUser) {
    alert("Vui long dang nhap!");
    window.location.href = "login.html";
}

logoutBtn.addEventListener("click", () => {
    localStorage.removeItem("currentUser");
    window.location.href = "login.html";
});

// Đổ dữ liệu hiện tại vào form
document.getElementById("profileFullName").value = currentUser.fullName || "";
document.getElementById("profileUsername").value = currentUser.username || "";
document.getElementById("profileStudentCode").value = currentUser.studentCode || "";

// Cập nhật thông tin cá nhân
document.getElementById("profileForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const messageEl = document.getElementById("profileMessage");
    messageEl.innerText = "";

    const updatedData = {
        fullName: document.getElementById("profileFullName").value.trim(),
        username: currentUser.username,
        studentCode: document.getElementById("profileStudentCode").value.trim() || null,
        role: currentUser.role
        // Khong gui password -> Backend se giu nguyen password cu (da xu ly o UserService.updateUser)
    };

    try {
        const res = await fetch(`${API_BASE}/users/${currentUser.id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(updatedData)
        });

        if (!res.ok) {
            const err = await res.text();
            throw new Error(err);
        }

        const updatedUser = await res.json();

        // Cap nhat lai Local Storage de dong bo voi thong tin moi
        localStorage.setItem("currentUser", JSON.stringify(updatedUser));

        messageEl.innerHTML = `<span class="text-success">Cap nhat thanh cong!</span>`;
    } catch (err) {
        console.error("Loi cap nhat:", err);
        messageEl.innerHTML = `<span class="text-danger">Khong the cap nhat: ${err.message}</span>`;
    }
});

// Đổi mật khẩu
document.getElementById("passwordForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const messageEl = document.getElementById("passwordMessage");
    messageEl.innerText = "";

    const oldPassword = document.getElementById("oldPassword").value;
    const newPassword = document.getElementById("newPassword").value;
    const confirmNewPassword = document.getElementById("confirmNewPassword").value;

    if (newPassword.length < 6) {
        messageEl.innerHTML = `<span class="text-danger">Mat khau moi phai co it nhat 6 ky tu</span>`;
        return;
    }
    if (newPassword !== confirmNewPassword) {
        messageEl.innerHTML = `<span class="text-danger">Mat khau nhap lai khong khop</span>`;
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/users/${currentUser.id}/change-password`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ oldPassword, newPassword })
        });

        const data = await res.text();

        if (res.ok) {
            messageEl.innerHTML = `<span class="text-success">${data}</span>`;
            document.getElementById("passwordForm").reset();
        } else {
            messageEl.innerHTML = `<span class="text-danger">${data}</span>`;
        }
    } catch (err) {
        console.error("Loi doi mat khau:", err);
        messageEl.innerHTML = `<span class="text-danger">Khong the ket noi server</span>`;
    }
});