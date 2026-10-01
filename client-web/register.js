const API_BASE = "http://localhost:8080";

document.getElementById("registerBtn").addEventListener("click", async () => {
    const fullName = document.getElementById("fullName").value.trim();
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirmPassword").value;
    const studentCode = document.getElementById("studentCode").value.trim();

    const errorEl = document.getElementById("registerError");
    const successEl = document.getElementById("registerSuccess");
    errorEl.innerText = "";
    successEl.innerText = "";

    // Validate phía Client truoc khi goi API
    if (!fullName || !username || !password) {
        errorEl.innerText = "Vui long dien day du thong tin bat buoc!";
        return;
    }
    if (password.length < 6) {
        errorEl.innerText = "Mat khau phai co it nhat 6 ky tu!";
        return;
    }
    if (password !== confirmPassword) {
        errorEl.innerText = "Mat khau nhap lai khong khop!";
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/users/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                fullName,
                username,
                password,
                studentCode: studentCode || null
            })
        });

        const data = await res.json();

        if (res.ok) {
            successEl.innerText = "Dang ky thanh cong! Dang chuyen sang trang dang nhap...";
            setTimeout(() => {
                window.location.href = "login.html";
            }, 1500);
        } else {
            errorEl.innerText = typeof data === "string" ? data : "Dang ky khong thanh cong";
        }
    } catch (err) {
        console.error("Loi dang ky:", err);
        errorEl.innerText = "Khong the ket noi server!";
    }
});