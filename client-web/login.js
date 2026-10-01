const API_BASE = "http://localhost:8080";

document.getElementById("loginBtn").addEventListener("click", async () => {
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();

    try {
        const res = await fetch(`${API_BASE}/users/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password }),
        });

        if (res.ok) {
            const user = await res.json();
            localStorage.setItem("currentUser", JSON.stringify(user));

            // Điều hướng theo vai trò: ADMIN vào trang quản trị (SOS09), CUSTOMER vào trang xem sách
            if (user.role === "ADMIN") {
                window.location.href = "admin-books.html";
            } else {
                window.location.href = "index.html";
            }
        } else {
            document.getElementById("loginError").innerText = "Sai tai khoan hoac mat khau!";
        }
    } catch (err) {
        console.error("Loi ket noi:", err);
        document.getElementById("loginError").innerText = "Khong the ket noi server!";
    }
});