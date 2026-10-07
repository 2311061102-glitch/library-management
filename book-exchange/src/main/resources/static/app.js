const grid = document.querySelector('#book-grid');
const bookCardTemplate = document.querySelector('#book-card-template');
const searchInput = document.querySelector('#search-input');
const conditionFilter = document.querySelector('#condition-filter');
const sortBooks = document.querySelector('#sort-books');
const emptyState = document.querySelector('#empty-state');
const tradeDialog = document.querySelector('#trade-dialog');
const listingForm = document.querySelector('#listing-form');
const listingTypeInput = document.querySelector('#listing-type');
const listingHeading = document.querySelector('#listing-heading');
const priceField = document.querySelector('#price-field');
const priceInput = document.querySelector('#book-price');
const formMessage = document.querySelector('#form-message');
const toast = document.querySelector('#toast');

let books = [];
let activeCategory = 'all';
let cartCount = 0;
let toastTimer;

function showToast(message) {
  toast.textContent = message;
  toast.classList.add('visible');
  window.clearTimeout(toastTimer);
  toastTimer = window.setTimeout(() => toast.classList.remove('visible'), 3000);
}

function formatPrice(price) {
  if (price === null || price === undefined) return 'Liên hệ';
  return new Intl.NumberFormat('vi-VN').format(price) + 'đ';
}

function createBookCard(book, index) {
  const card = bookCardTemplate.content.firstElementChild.cloneNode(true);
  const image = card.querySelector('.book-image');
  const placeholder = card.querySelector('.cover-placeholder');
  const isExchange = book.listingType === 'TRAO_DOI';

  card.dataset.bookId = book.id;
  card.style.setProperty('--card-index', String(index));
  card.querySelector('.book-title').textContent = book.title;
  card.querySelector('.book-author').textContent = book.author;
  card.querySelector('.book-category').textContent = book.category || 'Khác';
  card.querySelector('.book-price').textContent = isExchange ? 'Đổi sách' : formatPrice(book.price);
  card.querySelector('.condition-badge').textContent = isExchange
    ? 'Trao đổi'
    : (book.bookCondition || 'Sách cũ');

  placeholder.textContent = book.title;
  if (book.imageUrl?.trim()) {
    image.src = book.imageUrl;
    image.alt = `Bìa sách ${book.title}`;
    image.hidden = false;
    placeholder.hidden = true;
    image.addEventListener('error', () => {
      image.hidden = true;
      placeholder.hidden = false;
    }, { once: true });
  }

  return card;
}

function renderBooks() {
  const keyword = searchInput.value.trim().toLocaleLowerCase('vi');
  const condition = conditionFilter.value.toLocaleLowerCase('vi');
  let visibleBooks = books.filter((book) => {
    const matchesKeyword = `${book.title} ${book.author} ${book.category || ''}`
      .toLocaleLowerCase('vi')
      .includes(keyword);
    const matchesCondition = !condition
      || (book.bookCondition || '').toLocaleLowerCase('vi').includes(condition);
    const matchesCategory = activeCategory === 'all'
      || (book.category || 'Khác') === activeCategory;
    return matchesKeyword && matchesCondition && matchesCategory;
  });

  if (sortBooks.value === 'price-asc') {
    visibleBooks.sort((a, b) => (a.price ?? Number.MAX_VALUE) - (b.price ?? Number.MAX_VALUE));
  } else if (sortBooks.value === 'price-desc') {
    visibleBooks.sort((a, b) => (b.price ?? 0) - (a.price ?? 0));
  } else if (sortBooks.value === 'title') {
    visibleBooks.sort((a, b) => a.title.localeCompare(b.title, 'vi'));
  }

  grid.replaceChildren(...visibleBooks.map(createBookCard));
  emptyState.hidden = visibleBooks.length > 0;
}

grid.addEventListener('click', (event) => {
  const button = event.target.closest('[data-action]');
  if (!button) return;

  const card = button.closest('.book-card');
  const book = books.find(({ id }) => String(id) === card.dataset.bookId);
  if (!book) return;

  if (button.dataset.action === 'buy') {
    showToast(`Bạn đã chọn "${book.title}". Tính năng đặt mua sẽ được bổ sung.`);
  } else {
    cartCount += 1;
    document.querySelector('#cart-count').textContent = String(cartCount);
    showToast(`Đã thêm "${book.title}" vào giỏ.`);
  }
});

async function loadBooks() {
  try {
    const response = await fetch('/api/books');
    if (!response.ok) throw new Error(`Máy chủ trả về mã ${response.status}`);
    books = await response.json();
    renderBooks();
  } catch (error) {
    grid.replaceChildren();
    const message = document.createElement('p');
    message.className = 'notice';
    message.textContent = 'Không tải được sách. Hãy kiểm tra MySQL và khởi động lại backend.';
    grid.append(message);
    console.error('Không thể tải danh sách sách:', error);
  }
}

function openListingForm(type) {
  const isExchange = type === 'TRAO_DOI';
  const listingType = isExchange ? 'TRAO_DOI' : 'BAN';
  listingHeading.textContent = isExchange ? 'Đăng sách để trao đổi' : 'Đăng sách để bán';
  priceField.hidden = isExchange;
  priceInput.required = !isExchange;
  formMessage.textContent = '';
  formMessage.classList.remove('error');
  listingForm.reset();
  listingTypeInput.value = listingType;
  tradeDialog.showModal();
}

document.querySelectorAll('[data-trade]').forEach((button) => {
  button.addEventListener('click', () => openListingForm(button.dataset.trade));
});

document.querySelector('#close-dialog').addEventListener('click', () => tradeDialog.close());
tradeDialog.addEventListener('click', (event) => {
  if (event.target === tradeDialog) tradeDialog.close();
});

listingForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  const submitButton = document.querySelector('#submit-listing');
  submitButton.disabled = true;
  formMessage.textContent = 'Đang đăng sách...';
  formMessage.classList.remove('error');

  const formData = new FormData(listingForm);
  const payload = Object.fromEntries(formData.entries());
  payload.price = payload.price ? Number(payload.price) : null;
  try {
    const response = await fetch('/api/books', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!response.ok) {
      const responseBody = await response.json().catch(() => null);
      throw new Error(
        responseBody?.message
          || responseBody?.detail
          || responseBody?.error
          || `Đăng sách thất bại (HTTP ${response.status}).`
      );
    }
    const savedBook = await response.json();
    books.unshift(savedBook);
    renderBooks();
    listingForm.reset();
    tradeDialog.close();
    showToast('Đăng sách thành công! Sách đã hiển thị ngay trên trang.');
  } catch (error) {
    formMessage.textContent = error instanceof Error
      ? error.message
      : 'Không thể kết nối máy chủ. Hãy kiểm tra backend rồi thử lại.';
    formMessage.classList.add('error');
    console.error('Không thể đăng sách:', error);
  } finally {
    submitButton.disabled = false;
  }
});

searchInput.addEventListener('input', renderBooks);
conditionFilter.addEventListener('change', renderBooks);
sortBooks.addEventListener('change', renderBooks);
document.querySelectorAll('[data-category]').forEach((button) => {
  button.addEventListener('click', () => {
    document.querySelector('.category.active')?.classList.remove('active');
    button.classList.add('active');
    activeCategory = button.dataset.category;
    renderBooks();
  });
});
document.querySelector('#cart-button').addEventListener('click', () => {
  showToast(cartCount ? `Giỏ hàng hiện có ${cartCount} sản phẩm.` : 'Giỏ hàng của bạn đang trống.');
});
document.querySelector('#account-button').addEventListener('click', () => {
  showToast('Tính năng tài khoản sẽ được bổ sung.');
});

loadBooks();
