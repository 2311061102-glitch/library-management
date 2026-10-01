const API_BASE = "http://localhost:8080";
const API_KEY = "SECRET_KEY_123";
const categoryList = document.getElementById("categoryList");
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

const searchCategoryInput = document.getElementById("searchCategory");
let allCategories = [];

async function loadCategories() {
    try {
        const res = await fetch(`${API_BASE}/categories`);
        allCategories = await res.json();
        renderCategories(allCategories);
    } catch (err) {
        console.error("Loi tai danh muc:", err);
    }
}

searchCategoryInput.addEventListener("input", () => {
    const keyword = searchCategoryInput.value.trim().toLowerCase();
    const filtered = allCategories.filter(c => c.name.toLowerCase().includes(keyword));
    renderCategories(filtered);
});

function renderCategories(categories) {
    categoryList.innerHTML = "";
    categories.forEach(c => {
        const row = document.createElement("tr");
        const bookCount = c.books ? c.books.length : 0;
        row.innerHTML = `
      <td>${c.name}</td>
      <td>${bookCount}</td>
      <td>
        <button class="btn btn-warning btn-sm me-2" onclick='editCategory(${JSON.stringify({id: c.id, name: c.name})})'>Sua</button>
        <button class="btn btn-danger btn-sm" onclick="deleteCategory(${c.id})">Xoa</button>
      </td>
    `;
        categoryList.appendChild(row);
    });
}

document.getElementById("categoryForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const id = document.getElementById("categoryId").value;
    const name = document.getElementById("categoryName").value.trim();

    try {
        let res;
        if (id) {
            res = await fetch(`${API_BASE}/categories/${id}?role=ADMIN`, {
                method: "PUT",
                headers: { "Content-Type": "application/json", "X-API-KEY": API_KEY },
                body: JSON.stringify({ name })
            });
        } else {
            res = await fetch(`${API_BASE}/categories?role=ADMIN`, {
                method: "POST",
                headers: { "Content-Type": "application/json", "X-API-KEY": API_KEY },
                body: JSON.stringify({ name })
            });
        }

        if (!res.ok) {
            const err = await res.text();
            throw new Error(err);
        }

        alert("Luu danh muc thanh cong!");
        resetForm();
        loadCategories();
    } catch (err) {
        console.error("Loi luu danh muc:", err);
        alert("Khong the luu danh muc: " + err.message);
    }
});

function editCategory(c) {
    document.getElementById("formTitle").innerText = "Sua danh muc";
    document.getElementById("categoryId").value = c.id;
    document.getElementById("categoryName").value = c.name;
}

async function deleteCategory(id) {
    if (!confirm("Xoa danh muc nay? Luu y: sach thuoc danh muc nay cung se bi xoa theo (cascade).")) return;
    try {
        const res = await fetch(`${API_BASE}/categories/${id}?role=ADMIN`, {
            method: "DELETE",
            headers: { "X-API-KEY": API_KEY }
        });
        if (res.status === 204) {
            alert("Xoa thanh cong!");
            loadCategories();
        } else {
            const err = await res.text();
            alert("Khong the xoa: " + err);
        }
    } catch (err) {
        console.error("Loi xoa:", err);
    }
}

function resetForm() {
    document.getElementById("categoryForm").reset();
    document.getElementById("categoryId").value = "";
    document.getElementById("formTitle").innerText = "Them danh muc moi";
}
document.getElementById("resetBtn").addEventListener("click", resetForm);

loadCategories();