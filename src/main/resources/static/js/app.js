// === 全域變數 ===
let currentUser = null;
let currentCompressedImage = null;

// === 更新右上角購物車數量（永遠顯示）===
function updateCartCount() {
    const badge = document.querySelector('.cart-count');
    if (!badge) return;

    if (!currentUser) {
        badge.textContent = '0';
        badge.style.display = 'block';
        return;
    }

    fetch(`/api/cart?userId=${currentUser.id}`)
        .then(res => res.ok ? res.json() : [])
        .then(items => {
            const count = items.reduce((sum, item) => sum + (item.quantity || 0), 0);
            badge.textContent = count;
            badge.style.display = 'block';
        })
        .catch(() => {
            badge.textContent = '0';
            badge.style.display = 'block';
        });
}

// === 統一渲染右上角按鈕 ===
function renderAuthButtons(user) {
    const area = document.getElementById('auth-area');
    if (!area) return;

    currentUser = user;

    if (user && user.username) {
        area.innerHTML = `
            <span class="me-3"><span style="color:red">歡迎！</span> <span style="color:green">${user.username}</span></span>
            <a href="cart.html" class="btn btn-outline-primary btn-sm me-2 position-relative">
                購物車
                <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger cart-count" style="font-size: 0.65rem;">
                    0
                    <span class="visually-hidden">購物車數量</span>
                </span>
            </a>
            <button class="btn btn-outline-danger btn-sm" onclick="logout()">登出</button>
        `;
        updateCartCount();
    } else {
        area.innerHTML = `
            <button class="btn btn-outline-primary btn-sm me-2" data-bs-toggle="modal" data-bs-target="#loginModal">登入</button>
            <button class="btn btn-outline-success btn-sm" data-bs-toggle="modal" data-bs-target="#registerModal">註冊</button>
        `;
        const badge = document.querySelector('.cart-count');
        if (badge) {
            badge.textContent = '0';
            badge.style.display = 'block';
        }
    }
}

// === 檢查登入狀態 ===
function checkLogin() {
    const user = JSON.parse(localStorage.getItem('currentUser'));
    renderAuthButtons(user);
}

// === 登出 ===
function logout() {
    localStorage.removeItem('currentUser');
    renderAuthButtons(null);
    alert('已登出');
}

// === 登入 ===
function login() {
    const username = document.getElementById('loginUsername').value.trim();
    const password = document.getElementById('loginPassword').value;

    if (!username || !password) {
        alert('請輸入帳號與密碼');
        return;
    }

    fetch('/api/users/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    })
        .then(res => {
            if (!res.ok) {
                return res.text().then(text => { throw new Error(text || '登入失敗') });
            }
            return res.json();
        })
        .then(data => {
            localStorage.setItem('currentUser', JSON.stringify(data));
            bootstrap.Modal.getInstance(document.getElementById('loginModal')).hide();
            renderAuthButtons(data);
            updateCartCount();
            alert('登入成功！');
        })
        .catch(err => alert(err.message));
}

// === 註冊 ===
function register() {
    const username = document.getElementById('regUsername').value.trim();
    const password = document.getElementById('regPassword').value;
    const email = document.getElementById('regEmail').value.trim();

    if (!username || !password || !email) {
        alert('請填寫完整資料');
        return;
    }

    fetch('/api/users/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password, email })
    })
        .then(res => {
            if (!res.ok) return res.text().then(text => { throw new Error(text) });
            return res.json();
        })
        .then(user => {
            localStorage.setItem('currentUser', JSON.stringify(user));
            bootstrap.Modal.getInstance(document.getElementById('registerModal')).hide();
            renderAuthButtons(user);
            updateCartCount();
            alert('註冊成功！已自動登入');
        })
        .catch(err => alert(err.message));
}

// === 發送忘記密碼郵件 ===
function sendResetEmail() {
    const username = document.getElementById('resetUsername').value.trim();
    const messageEl = document.getElementById('resetMessage');

    if (!username) {
        messageEl.innerHTML = '<p class="text-danger">請輸入帳號</p>';
        return;
    }

    messageEl.innerHTML = '<p class="text-info">查詢中...</p>';

    fetch('/api/users/forgot-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username })
    })
        .then(res => {
            if (!res.ok) return res.text().then(text => { throw new Error(text) });
            return res.text();
        })
        .then(msg => {
            messageEl.innerHTML = `<p class="text-success">${msg}</p>`;
            setTimeout(() => {
                bootstrap.Modal.getInstance(document.getElementById('forgotPasswordModal')).hide();
            }, 2000);
        })
        .catch(err => {
            messageEl.innerHTML = `<p class="text-danger">${err.message}</p>`;
        });
}

// === 新增商品 ===
function addProduct() {
    const name = document.getElementById('name').value.trim();
    const price = document.getElementById('price').value;
    const description = document.getElementById('description').value.trim();
    const image = document.getElementById('image').files[0];

    if (!name || !price || price <= 0) {
        alert('請填寫正確的商品名稱與價格');
        return;
    }

    const formData = new FormData();
    formData.append('name', name);
    formData.append('price', price);
    formData.append('description', description);
    if (image) formData.append('image', image);

    fetch('/api/products', {
        method: 'POST',
        body: formData
    })
        .then(res => {
            if (!res.ok) return res.text().then(text => { throw new Error(text || '新增失敗') });
            return res.json();
        })
        .then(() => {
            bootstrap.Modal.getInstance(document.getElementById('addProductModal')).hide();
            document.getElementById('name').value = '';
            document.getElementById('price').value = '';
            document.getElementById('description').value = '';
            document.getElementById('image').value = '';
            loadProducts();
            alert('商品新增成功！');
        })
        .catch(err => alert(err.message || '新增失敗'));
}

// === 購物車相關 ===
function addToCart(productId) {
    if (!currentUser) return alert('請先登入');

    fetch(`/api/cart?userId=${currentUser.id}&productId=${productId}&quantity=1`, {
        method: 'POST'
    })
        .then(res => {
            if (!res.ok) throw new Error('加入失敗');
            alert('加入成功');
            updateCartCount();
            if (document.getElementById('cartTable')) loadCart();
        })
        .catch(err => alert(err.message));
}

function removeFromCart(productId) {
    if (!currentUser) return alert('請先登入');
    if (!confirm('確定移除此商品？')) return;

    fetch(`/api/cart?userId=${currentUser.id}&productId=${productId}`, {
        method: 'DELETE'
    })
        .then(res => {
            if (!res.ok) throw new Error('移除失敗');
            loadCart();
            updateCartCount();
            alert('已移除');
        })
        .catch(() => alert('移除失敗'));
}

function clearCart() {
    if (!currentUser) return alert('請先登入');
    if (!confirm('確定要清空購物車？')) return;

    fetch(`/api/cart/clear?userId=${currentUser.id}`, {
        method: 'DELETE'
    })
        .then(res => {
            if (!res.ok) return res.text().then(text => { throw new Error(text) });
            loadCart();
            updateCartCount();
            alert('購物車已清空！');
        })
        .catch(err => {
            console.error('清空失敗:', err);
            alert('清空失敗：' + err.message);
        });
}

function updateQuantity(productId, change) {
    if (!currentUser) return alert('請先登入');
    const row = document.querySelector(`tr[data-product-id="${productId}"]`);
    if (!row) return;

    const qtyInput = row.querySelector('input[type="text"]');
    let currentQty = parseInt(qtyInput.value) || 0;
    const newQty = currentQty + change;

    if (newQty < 1) {
        alert('數量至少為 1！');
        return;
    }

    fetch(`/api/cart/update?userId=${currentUser.id}&productId=${productId}&quantity=${newQty}`, {
        method: 'POST'
    })
        .then(res => {
            if (!res.ok) throw new Error('更新失敗');
            qtyInput.value = newQty;
            const unitPrice = parseFloat(row.dataset.unitPrice) || 0;
            const subtotal = unitPrice * newQty;
            row.querySelector('.subtotal').textContent = `$${subtotal.toFixed(0)}`;
            updateCartTotal();
            updateCartCount();
        })
        .catch(() => alert('更新失敗，請檢查網路'));
}

function updateCartTotal() {
    const subtotals = document.querySelectorAll('.subtotal');
    let total = 0;
    subtotals.forEach(el => {
        const value = el.textContent.replace('$', '').trim();
        total += parseFloat(value) || 0;
    });
    const totalEl = document.getElementById('cartTotal');
    if (totalEl) totalEl.textContent = `$${total.toFixed(0)}`;
}

// === 載入購物車（關鍵！配合 BigDecimal）===
function loadCart() {
    const tbody = document.getElementById('cartTable')?.querySelector('tbody');
    const totalEl = document.getElementById('cartTotal');
    if (!tbody || !totalEl) return;

    if (!currentUser) {
        tbody.innerHTML = `<tr><td colspan="4" class="text-center text-danger">請先登入</td></tr>`;
        totalEl.textContent = '$0';
        updateCartCount();
        return;
    }

    fetch(`/api/cart?userId=${currentUser.id}`)
        .then(res => {
            if (!res.ok) throw new Error('載入失敗');
            return res.json();
        })
        .then(items => {
            tbody.innerHTML = '';
            let total = 0;

            if (items.length === 0) {
                tbody.innerHTML = `<tr><td colspan="4" class="text-center">購物車是空的</td></tr>`;
                totalEl.textContent = '$0';
                updateCartCount();
                return;
            }

            items.forEach(item => {
                // 關鍵！BigDecimal → 轉數字
                const price = item.productPrice ? parseFloat(item.productPrice) : 0;
                const subtotal = price * item.quantity;
                total += subtotal;

                const row = document.createElement('tr');
                row.dataset.productId = item.productId;
                row.dataset.unitPrice = price;

                row.innerHTML = `
                    <td>
                        <img src="${item.productImageUrl || 'https://via.placeholder.com/50'}" 
                             width="50" height="50" class="me-2 rounded" style="object-fit: cover;" alt="${item.productName}">
                        ${item.productName || '未知商品'}
                    </td>
                    <td>
                        <button class="btn btn-sm btn-outline-secondary" onclick="updateQuantity(${item.productId}, -1)">-</button>
                        <input type="text" value="${item.quantity}" readonly 
                               style="width:40px; text-align:center; display:inline-block; margin:0 5px;" class="form-control d-inline">
                        <button class="btn btn-sm btn-outline-secondary" onclick="updateQuantity(${item.productId}, 1)">+</button>
                    </td>
                    <td class="subtotal fw-bold">$${subtotal.toFixed(0)}</td>
                    <td>
                        <button class="btn btn-sm btn-danger" onclick="removeFromCart(${item.productId})">移除</button>
                    </td>
                `;

                tbody.appendChild(row);
            });

            totalEl.textContent = `$${total.toFixed(0)}`;
            updateCartCount();
        })
        .catch(err => {
            console.error('載入失敗:', err);
            tbody.innerHTML = `<tr><td colspan="4" class="text-center text-danger">載入失敗，請重新整理</td></tr>`;
            totalEl.textContent = '$0';
            updateCartCount();
        });
}

// === 載入商品列表 ===
function loadProducts(keyword = '') {
    const container = document.getElementById('productContainer');
    if (!container) return;

    const url = keyword
        ? `/api/products/search?keyword=${encodeURIComponent(keyword)}`
        : '/api/products';

    fetch(url)
        .then(res => res.json())
        .then(products => {
            container.innerHTML = '';
            if (products.length === 0) {
                container.innerHTML = `
                    <div class="col-12">
                        <p class="text-center text-muted fs-5">
                            ${keyword ? `找不到「${keyword}」相關商品` : '尚未有商品'}
                        </p>
                    </div>
                `;
                return;
            }

            products.forEach(p => {
                const col = document.createElement('div');
                col.className = 'col';
                col.innerHTML = `
                    <div class="card h-100 product-card">
                        ${p.imageUrl
                        ? `<img src="${p.imageUrl}" class="card-img-top" alt="${p.name}" style="height:200px; object-fit:cover;">`
                        : `<div class="bg-light card-img-top d-flex align-items-center justify-content-center" style="height:200px;">
                                 <span class="text-muted">無圖片</span>
                               </div>`
                    }
                        <div class="card-body d-flex flex-column">
                            <h5 class="card-title">${p.name}</h5>
                            <p class="card-text text-muted flex-grow-1">${p.description || ''}</p>
                            <p class="card-text"><strong class="text-danger fs-5">$${Math.round(p.price)}</strong></p>
                            <div class="mt-auto">
                                <button class="btn btn-warning btn-sm w-100 mb-1" onclick="editProduct(${p.id})">編輯</button>
                                <button class="btn btn-danger btn-sm w-100 mb-1" onclick="deleteProduct(${p.id})">刪除</button>
                                <button class="btn btn-success btn-sm w-100" onclick="addToCart(${p.id})">加入購物車</button>
                            </div>
                        </div>
                    </div>
                `;
                container.appendChild(col);
            });
        })
        .catch(() => {
            container.innerHTML = '<div class="col-12"><p class="text-center text-danger">載入失敗</p></div>';
        });
}

// === 其他函式 ===
function editProduct(id) { window.location.href = `edit-product.html?id=${id}`; }
function deleteProduct(id) {
    if (confirm('確定刪除？')) {
        fetch(`/api/products/${id}`, { method: 'DELETE' })
            .then(() => loadProducts())
            .catch(() => alert('刪除失敗'));
    }
}

// === 圖片壓縮 ===
function compressImage(file, callback) {
    const maxSize = 1024;
    const quality = 0.8;

    const reader = new FileReader();
    reader.onload = function (e) {
        const img = new Image();
        img.onload = function () {
            const canvas = document.createElement('canvas');
            let width = img.width;
            let height = img.height;

            if (width > height && width > maxSize) {
                height = Math.round(height * maxSize / width);
                width = maxSize;
            } else if (height > maxSize) {
                width = Math.round(width * maxSize / height);
                height = maxSize;
            }

            canvas.width = width;
            canvas.height = height;
            const ctx = canvas.getContext('2d');
            ctx.drawImage(img, 0, 0, width, height);

            canvas.toBlob(function (blob) {
                const compressedFile = new File([blob], file.name, { type: file.type });
                callback(compressedFile);
            }, file.type, quality);
        };
        img.src = e.target.result;
    };
    reader.readAsDataURL(file);
}

function setupDragAndDrop() {
    const dropZone = document.getElementById('dropZone');
    const fileInput = document.getElementById('imageInput');
    const previewContainer = document.getElementById('imagePreviewContainer');
    const errorEl = document.getElementById('uploadError');

    if (!dropZone || !fileInput || !previewContainer || !errorEl) return;

    dropZone.addEventListener('click', () => fileInput.click());

    ['dragover', 'dragenter'].forEach(event => {
        dropZone.addEventListener(event, e => {
            e.preventDefault();
            e.stopPropagation();
            dropZone.classList.add('dragover');
        });
    });

    ['dragleave', 'dragend', 'drop'].forEach(event => {
        dropZone.addEventListener(event, e => {
            e.preventDefault();
            e.stopPropagation();
            dropZone.classList.remove('dragover');
        });
    });

    dropZone.addEventListener('drop', e => {
        const files = e.dataTransfer?.files;
        if (files && files.length > 0) handleFile(files[0]);
    });

    fileInput.addEventListener('change', e => {
        if (e.target.files.length > 0) handleFile(e.target.files[0]);
    });

    function handleFile(file) {
        errorEl.textContent = '';

        if (file.size > 5 * 1024 * 1024) {
            errorEl.textContent = '檔案太大！請選擇小於 5MB 的圖片';
            return;
        }

        compressImage(file, compressedFile => {
            const reader = new FileReader();
            reader.onload = e => {
                previewContainer.innerHTML = `
                    <img src="${e.target.result}" id="imagePreview" class="img-thumbnail" alt="預覽">
                    <p class="text-success small mt-1">已壓縮：${(compressedFile.size / 1024).toFixed(1)} KB</p>
                `;
            };
            reader.readAsDataURL(compressedFile);
            currentCompressedImage = compressedFile;
        });
    }
}

function loadProductForEdit() {
    const urlParams = new URLSearchParams(window.location.search);
    const id = urlParams.get('id');
    if (!id) {
        alert('無效的商品 ID');
        window.location.href = 'index.html';
        return;
    }

    document.getElementById('productId').value = id;

    fetch(`/api/products/${id}`)
        .then(res => { if (!res.ok) throw new Error('載入失敗'); return res.json(); })
        .then(p => {
            document.getElementById('name').value = p.name || '';
            document.getElementById('price').value = p.price || '';
            document.getElementById('description').value = p.description || '';

            const previewContainer = document.getElementById('imagePreviewContainer');
            if (p.imageUrl) {
                previewContainer.innerHTML = `
                    <img src="${p.imageUrl}" id="imagePreview" class="img-thumbnail" alt="目前圖片">
                    <p class="text-muted small mt-1">選擇新圖片以替換</p>
                `;
            } else {
                previewContainer.innerHTML = '<p class="text-muted">尚未上傳圖片</p>';
            }

            setupDragAndDrop();
        })
        .catch(err => {
            alert(err.message);
            window.location.href = 'index.html';
        });
}

function updateProduct() {
    const id = document.getElementById('productId').value;
    const name = document.getElementById('name').value.trim();
    const price = document.getElementById('price').value;
    const description = document.getElementById('description').value.trim();
    const image = currentCompressedImage;

    if (!name || !price || price <= 0) {
        alert('請填寫名稱與有效價格');
        return;
    }

    const formData = new FormData();
    formData.append('name', name);
    formData.append('price', price);
    if (description) formData.append('description', description);
    if (image) formData.append('image', image);

    fetch(`/api/products/${id}`, {
        method: 'PUT',
        body: formData
    })
        .then(res => {
            if (!res.ok) return res.text().then(text => { throw new Error(text) });
            return res.json();
        })
        .then(() => {
            alert('商品已成功更新！');
            window.location.href = 'index.html';
        })
        .catch(err => {
            const errorEl = document.getElementById('uploadError');
            if (errorEl) errorEl.textContent = err.message;
        });
}

// === 頁面載入 ===
document.addEventListener('DOMContentLoaded', () => {
    checkLogin();

    let searchTimeout;
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', () => {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                const keyword = searchInput.value.trim();
                loadProducts(keyword);
            }, 300);
        });
    }

    loadProducts();

    if (document.getElementById('cartTable')) {
        loadCart();
    }
});