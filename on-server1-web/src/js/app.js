/**
 * ON-SERVER1 Web App
 * Main application logic
 */

(function () {
  'use strict';

  // ============================================
  // State
  // ============================================
  const state = {
    products: [],
    categories: [],
    banners: [],
    featuredProducts: [],
    currentPage: 1,
    totalPages: 1,
    currentType: '',
    currentCategory: '',
    searchQuery: '',
    isLoading: false,
  };

  // ============================================
  // DOM Elements
  // ============================================
  const $ = (sel) => document.querySelector(sel);
  const $$ = (sel) => document.querySelectorAll(sel);

  const els = {
    loadingSpinner: $('#loadingSpinner'),
    bannersSlider: $('#bannersSlider'),
    bannersDots: $('#bannersDots'),
    categoriesGrid: $('#categoriesGrid'),
    featuredGrid: $('#featuredGrid'),
    productsGrid: $('#productsGrid'),
    filterTabs: $('#filterTabs'),
    searchInput: $('#searchInput'),
    productModal: $('#productModal'),
    modalClose: $('#modalClose'),
    modalBody: $('#modalBody'),
    loadMore: $('#loadMore'),
    loadMoreBtn: $('#loadMoreBtn'),
    toast: $('#toast'),
  };

  // ============================================
  // Toast Notifications
  // ============================================
  let toastTimer;
  function showToast(message, type = 'info') {
    clearTimeout(toastTimer);
    els.toast.textContent = message;
    els.toast.className = `toast show ${type}`;
    toastTimer = setTimeout(() => {
      els.toast.classList.remove('show');
    }, 3000);
  }

  // ============================================
  // Loading
  // ============================================
  function showLoading() {
    els.loadingSpinner.classList.remove('hidden');
  }

  function hideLoading() {
    els.loadingSpinner.classList.add('hidden');
  }

  // ============================================
  // Skeleton Cards
  // ============================================
  function renderSkeletons(container, count = 6) {
    let html = '';
    for (let i = 0; i < count; i++) {
      html += `
        <div class="skeleton-card">
          <div class="skeleton skeleton-image"></div>
          <div class="skeleton skeleton-text"></div>
          <div class="skeleton skeleton-text-sm"></div>
        </div>
      `;
    }
    container.innerHTML = html;
  }

  // ============================================
  // Product Type Labels
  // ============================================
  const typeLabels = {
    SERVICE: 'خدمة',
    GIFT_CARD: 'بطاقة هدية',
    GAME_CARD: 'بطاقة لعبة',
    TOP_UP: 'شحن',
    SUBSCRIPTION: 'اشتراك',
  };

  // ============================================
  // Render Banners
  // ============================================
  function renderBanners(banners) {
    if (!banners || banners.length === 0) {
      els.bannersSlider.parentElement.style.display = 'none';
      return;
    }

    els.bannersSlider.innerHTML = banners
      .map(
        (banner) => `
        <div class="banner-item" ${banner.link ? `onclick="window.open('${banner.link}', '_self')"` : ''}>
          <img src="${banner.image}" alt="${banner.title || ''}" loading="lazy" onerror="this.parentElement.style.display='none'" />
          ${banner.title ? `<div class="banner-overlay"><h3>${banner.title}</h3></div>` : ''}
        </div>
      `
      )
      .join('');

    // Dots
    els.bannersDots.innerHTML = banners
      .map((_, i) => `<div class="dot ${i === 0 ? 'active' : ''}" data-index="${i}"></div>`)
      .join('');

    // Auto scroll
    let currentBanner = 0;
    const slider = els.bannersSlider;

    function scrollToBanner(index) {
      const items = slider.querySelectorAll('.banner-item');
      if (items[index]) {
        items[index].scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'center' });
        els.bannersDots.querySelectorAll('.dot').forEach((dot, i) => {
          dot.classList.toggle('active', i === index);
        });
      }
    }

    // Dots click
    els.bannersDots.addEventListener('click', (e) => {
      if (e.target.classList.contains('dot')) {
        currentBanner = parseInt(e.target.dataset.index);
        scrollToBanner(currentBanner);
      }
    });

    // Auto slide
    if (banners.length > 1) {
      setInterval(() => {
        currentBanner = (currentBanner + 1) % banners.length;
        scrollToBanner(currentBanner);
      }, 5000);
    }
  }

  // ============================================
  // Render Categories
  // ============================================
  function renderCategories(categories) {
    if (!categories || categories.length === 0) {
      els.categoriesGrid.parentElement.parentElement.style.display = 'none';
      return;
    }

    // Add "All" option
    const allItem = `
      <div class="category-card active" data-id="" onclick="app.filterByCategory('')">
        <span class="category-icon">🏪</span>
        <span class="category-name">الكل</span>
      </div>
    `;

    els.categoriesGrid.innerHTML =
      allItem +
      categories
        .map(
          (cat) => `
          <div class="category-card" data-id="${cat.id}" onclick="app.filterByCategory('${cat.id}')">
            <span class="category-icon">${cat.icon || '📁'}</span>
            <span class="category-name">${cat.nameAr || cat.name}</span>
          </div>
        `
        )
        .join('');
  }

  // ============================================
  // Render Product Card
  // ============================================
  function createProductCard(product) {
    const imageHtml = product.image
      ? `<img class="product-image" src="${product.image}" alt="${product.nameAr || product.name}" loading="lazy" onerror="this.outerHTML='<div class=product-image-placeholder>📦</div>'" />`
      : `<div class="product-image-placeholder">📦</div>`;

    const badge = product.isFeatured ? `<span class="product-badge">⭐ مميز</span>` : '';
    const categoryName = product.category ? (product.category.nameAr || product.category.name) : '';
    const oldPrice = product.originalPrice
      ? `<span class="product-old-price">$${product.originalPrice}</span>`
      : '';

    return `
      <div class="product-card" onclick="app.showProduct('${product.id}')">
        ${badge}
        ${imageHtml}
        <div class="product-info">
          ${categoryName ? `<p class="product-category">${categoryName}</p>` : ''}
          <p class="product-name">${product.nameAr || product.name}</p>
          <div class="product-price-row">
            <div>
              <span class="product-price">$${product.price}</span>
              ${oldPrice}
            </div>
            <span class="product-type-badge">${typeLabels[product.type] || product.type}</span>
          </div>
        </div>
      </div>
    `;
  }

  // ============================================
  // Render Products
  // ============================================
  function renderProducts(products, container, append = false) {
    if (!products || products.length === 0) {
      if (!append) {
        container.innerHTML = `
          <div class="empty-state" style="grid-column: 1/-1">
            <div class="empty-state-icon">🔍</div>
            <p class="empty-state-text">لا توجد منتجات</p>
          </div>
        `;
      }
      return;
    }

    const html = products.map(createProductCard).join('');
    if (append) {
      container.insertAdjacentHTML('beforeend', html);
    } else {
      container.innerHTML = html;
    }
  }

  // ============================================
  // Load Products
  // ============================================
  async function loadProducts(page = 1, append = false) {
    if (state.isLoading) return;
    state.isLoading = true;

    if (!append) {
      renderSkeletons(els.productsGrid);
    }

    try {
      const params = { page, limit: 20 };
      if (state.currentType) params.type = state.currentType;
      if (state.currentCategory) params.categoryId = state.currentCategory;
      if (state.searchQuery) params.search = state.searchQuery;

      const response = await api.products.getAll(params);
      const { products, pagination } = response.data;

      state.products = append ? [...state.products, ...products] : products;
      state.currentPage = pagination.page;
      state.totalPages = pagination.totalPages;

      renderProducts(products, els.productsGrid, append);

      // Show/hide load more
      if (state.currentPage < state.totalPages) {
        els.loadMore.style.display = 'block';
      } else {
        els.loadMore.style.display = 'none';
      }
    } catch (error) {
      showToast(error.message, 'error');
      if (!append) els.productsGrid.innerHTML = '';
    } finally {
      state.isLoading = false;
    }
  }

  // ============================================
  // Show Product Detail
  // ============================================
  async function showProduct(id) {
    try {
      const response = await api.products.getById(id);
      const product = response.data;

      const imageHtml = product.image
        ? `<img class="modal-image" src="${product.image}" alt="${product.nameAr || product.name}" onerror="this.outerHTML='<div class=modal-image-placeholder>📦</div>'" />`
        : `<div class="modal-image-placeholder">📦</div>`;

      const categoryName = product.category ? (product.category.nameAr || product.category.name) : '';
      const description = product.descriptionAr || product.description || '';

      els.modalBody.innerHTML = `
        ${imageHtml}
        <div class="modal-details">
          ${categoryName ? `<p class="product-category">${categoryName}</p>` : ''}
          <h2 class="product-name">${product.nameAr || product.name}</h2>
          ${description ? `<p class="modal-description">${description}</p>` : ''}
          <div class="modal-price-section">
            <div>
              <span class="modal-price">$${product.price}</span>
              ${product.originalPrice ? `<span class="product-old-price">$${product.originalPrice}</span>` : ''}
            </div>
            <span class="product-type-badge">${typeLabels[product.type] || product.type}</span>
          </div>
          <button class="btn-buy" onclick="app.buyProduct('${product.id}')">
            🛒 شراء الآن
          </button>
        </div>
      `;

      els.productModal.classList.add('active');
      document.body.style.overflow = 'hidden';
    } catch (error) {
      showToast('فشل تحميل تفاصيل المنتج', 'error');
    }
  }

  // ============================================
  // Buy Product
  // ============================================
  async function buyProduct(productId) {
    const token = localStorage.getItem('user_token');
    if (!token) {
      showToast('يرجى تسجيل الدخول أولاً', 'error');
      // Send message to Android WebView
      if (window.AndroidBridge) {
        window.AndroidBridge.navigateToLogin();
      }
      return;
    }

    try {
      await api.orders.create([{ productId, quantity: 1 }]);
      showToast('تم الشراء بنجاح! ✅', 'success');
      closeModal();
      // Notify Android
      if (window.AndroidBridge) {
        window.AndroidBridge.onPurchaseSuccess(productId);
      }
    } catch (error) {
      showToast(error.message || 'فشل الشراء', 'error');
    }
  }

  // ============================================
  // Close Modal
  // ============================================
  function closeModal() {
    els.productModal.classList.remove('active');
    document.body.style.overflow = '';
  }

  // ============================================
  // Filter by Type
  // ============================================
  function filterByType(type) {
    state.currentType = type;
    state.currentPage = 1;

    // Update active tab
    els.filterTabs.querySelectorAll('.filter-tab').forEach((tab) => {
      tab.classList.toggle('active', tab.dataset.type === type);
    });

    loadProducts(1);
  }

  // ============================================
  // Filter by Category
  // ============================================
  function filterByCategory(categoryId) {
    state.currentCategory = categoryId;
    state.currentPage = 1;

    // Update active category
    els.categoriesGrid.querySelectorAll('.category-card').forEach((card) => {
      card.classList.toggle('active', card.dataset.id === categoryId);
    });

    loadProducts(1);
  }

  // ============================================
  // Search
  // ============================================
  let searchTimer;
  function handleSearch(query) {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => {
      state.searchQuery = query.trim();
      state.currentPage = 1;
      loadProducts(1);
    }, 500);
  }

  // ============================================
  // Event Listeners
  // ============================================
  function initEventListeners() {
    // Filter tabs
    els.filterTabs.addEventListener('click', (e) => {
      if (e.target.classList.contains('filter-tab')) {
        filterByType(e.target.dataset.type);
      }
    });

    // Search
    els.searchInput.addEventListener('input', (e) => {
      handleSearch(e.target.value);
    });

    // Modal close
    els.modalClose.addEventListener('click', closeModal);
    els.productModal.addEventListener('click', (e) => {
      if (e.target === els.productModal) closeModal();
    });

    // Escape key
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') closeModal();
    });

    // Load more
    els.loadMoreBtn.addEventListener('click', () => {
      loadProducts(state.currentPage + 1, true);
    });
  }

  // ============================================
  // Initialize
  // ============================================
  async function init() {
    showLoading();
    initEventListeners();

    try {
      // Load all initial data in parallel
      const [bannersRes, categoriesRes, featuredRes] = await Promise.all([
        api.banners.getAll().catch(() => ({ data: [] })),
        api.products.getCategories().catch(() => ({ data: [] })),
        api.products.getFeatured().catch(() => ({ data: [] })),
      ]);

      // Render banners
      state.banners = bannersRes.data || [];
      renderBanners(state.banners);

      // Render categories
      state.categories = categoriesRes.data || [];
      renderCategories(state.categories);

      // Render featured products
      state.featuredProducts = featuredRes.data || [];
      if (state.featuredProducts.length > 0) {
        renderProducts(state.featuredProducts, els.featuredGrid);
      } else {
        els.featuredGrid.parentElement.style.display = 'none';
      }

      // Load all products
      await loadProducts(1);
    } catch (error) {
      showToast('فشل تحميل البيانات', 'error');
    } finally {
      hideLoading();
    }
  }

  // ============================================
  // Public API (for onclick handlers)
  // ============================================
  window.app = {
    showProduct,
    buyProduct,
    filterByCategory,
    filterByType,
  };

  // ============================================
  // Android WebView Bridge
  // ============================================
  // The Android app can set the auth token via:
  // window.setUserToken(token)
  window.setUserToken = function (token) {
    localStorage.setItem('user_token', token);
  };

  // Start the app
  document.addEventListener('DOMContentLoaded', init);
})();
