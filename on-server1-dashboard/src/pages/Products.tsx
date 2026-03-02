import { useEffect, useState, useMemo } from 'react';
import toast from 'react-hot-toast';
import { productsApi, adminApi } from '../api/client';
import {
  ArrowPathIcon, PlusIcon, MagnifyingGlassIcon, FolderIcon,
  StarIcon, PencilSquareIcon, TrashIcon, LinkIcon, CubeIcon,
  CloudArrowDownIcon, CheckBadgeIcon, ServerStackIcon, DevicePhoneMobileIcon,
  Cog6ToothIcon, EyeIcon, EyeSlashIcon, GlobeAltIcon, KeyIcon, UserIcon
} from '@heroicons/react/24/outline';
import PageBanner from '../components/PageBanner';
import { StarIcon as StarSolid } from '@heroicons/react/24/solid';

interface Category {
  id: string;
  name: string;
  nameAr: string | null;
  icon: string | null;
  _count?: { products: number };
  children?: Category[];
}

interface Product {
  id: string;
  name: string;
  nameAr: string;
  description: string | null;
  price: number;
  costPrice?: number;
  type: string;
  serviceType?: string;
  externalId?: string;
  deliveryTime?: string;
  groupName?: string;
  isFeatured: boolean;
  isActive: boolean;
  sortOrder: number;
  image?: string;
  categoryId: string;
  category?: { id: string; name: string; nameAr?: string; icon?: string };
}

const emptyForm = {
  name: '', nameAr: '', price: '', type: 'SERVICE',
  description: '', categoryId: '', isFeatured: false, image: '',
  sortOrder: '0',
};

export default function Products() {
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);
  const [providerBalance, setProviderBalance] = useState<number | null>(null);
  const [markupPercent, setMarkupPercent] = useState<number | ''>('');
  const [showForm, setShowForm] = useState(false);
  const [editProduct, setEditProduct] = useState<Product | null>(null);
  const [syncResult, setSyncResult] = useState<any>(null);

  // Provider settings
  const [showProviderSettings, setShowProviderSettings] = useState(false);
  const [providerSettings, setProviderSettings] = useState({ url: '', username: '', apiKey: '' });
  const [providerForm, setProviderForm] = useState({ url: '', username: '', apiKey: '' });
  const [showApiKey, setShowApiKey] = useState(false);
  const [savingProvider, setSavingProvider] = useState(false);
  const [deletingProducts, setDeletingProducts] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  // Filters
  const [filterCategory, setFilterCategory] = useState('');
  const [filterFeatured, setFilterFeatured] = useState('');
  const [filterServiceType, setFilterServiceType] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const perPage = 50;

  // Form state
  const [form, setForm] = useState(emptyForm);

  useEffect(() => {
    loadProducts();
    loadCategories();
    loadProviderBalance();
    loadProviderSettings();
  }, []);

  const loadProducts = async () => {
    try {
      const response = await productsApi.getAll({ limit: 1000 });
      setProducts(response.data.data.products || []);
    } catch (error) {
      toast.error('فشل تحميل المنتجات');
    } finally {
      setLoading(false);
    }
  };

  const loadCategories = async () => {
    try {
      const response = await productsApi.getCategories();
      setCategories(response.data.data || []);
    } catch (error) {
      console.error('Failed to load categories');
    }
  };

  const loadProviderBalance = async () => {
    try {
      const response = await adminApi.getProviderBalance();
      if (response.data.data?.success) {
        setProviderBalance(response.data.data.balance);
      }
    } catch (error) {
      console.error('Failed to load provider balance');
    }
  };

  const loadProviderSettings = async () => {
    try {
      const response = await adminApi.getProviderSettings();
      const data = response.data.data;
      setProviderSettings(data);
      setProviderForm({ url: data.url, username: data.username, apiKey: '' });
    } catch (error) {
      console.error('Failed to load provider settings');
    }
  };

  const handleSaveProviderSettings = async () => {
    setSavingProvider(true);
    try {
      const updates: any = {};
      if (providerForm.url) updates.url = providerForm.url;
      if (providerForm.username) updates.username = providerForm.username;
      if (providerForm.apiKey) updates.apiKey = providerForm.apiKey;
      await adminApi.updateProviderSettings(updates);
      toast.success('تم تحديث إعدادات المصدر');
      loadProviderSettings();
      setShowProviderSettings(false);
    } catch (error) {
      toast.error('فشل تحديث الإعدادات');
    } finally {
      setSavingProvider(false);
    }
  };

  // Flatten categories for dropdowns
  const flatCategories = useMemo(() => {
    const flat: Category[] = [];
    for (const cat of categories) {
      flat.push(cat);
      if (cat.children) {
        for (const child of cat.children) flat.push(child);
      }
    }
    return flat;
  }, [categories]);

  // Filtered & paginated products
  const filteredProducts = useMemo(() => {
    let list = products;
    if (filterServiceType) list = list.filter(p => p.serviceType === filterServiceType);
    if (filterCategory) list = list.filter(p => p.categoryId === filterCategory);
    if (filterFeatured === 'yes') list = list.filter(p => p.isFeatured);
    if (filterFeatured === 'no') list = list.filter(p => !p.isFeatured);
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      list = list.filter(p =>
        p.name.toLowerCase().includes(q) ||
        (p.nameAr && p.nameAr.toLowerCase().includes(q)) ||
        (p.groupName && p.groupName.toLowerCase().includes(q)) ||
        (p.externalId && p.externalId.includes(q))
      );
    }
    return list;
  }, [products, filterCategory, filterFeatured, filterServiceType, searchQuery]);

  // Service type counts
  const serviceTypeCounts = useMemo(() => {
    const counts = { all: products.length, IMEI: 0, SERVER: 0 };
    for (const p of products) {
      if (p.serviceType === 'IMEI') counts.IMEI++;
      else if (p.serviceType === 'SERVER') counts.SERVER++;
    }
    return counts;
  }, [products]);

  const totalPages = Math.ceil(filteredProducts.length / perPage);
  const paginatedProducts = filteredProducts.slice((currentPage - 1) * perPage, currentPage * perPage);

  // Reset page when filters change
  useEffect(() => { setCurrentPage(1); }, [filterCategory, filterFeatured, filterServiceType, searchQuery]);

  const handleSync = async () => {
    if (markupPercent === '' || markupPercent < 0) {
      toast.error('يجب تحديد نسبة الربح أولاً');
      return;
    }
    setSyncing(true);
    setSyncResult(null);
    try {
      const response = await adminApi.syncProducts(markupPercent);
      const result = response.data.data;
      setSyncResult(result);
      toast.success(`تم المزامنة: ${result.created} جديد، ${result.updated} محدث`);
      loadProducts();
      loadCategories();
    } catch (error) {
      toast.error('فشلت المزامنة مع المزود');
    } finally {
      setSyncing(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const data: any = {
        name: form.name,
        nameAr: form.nameAr || null,
        price: parseFloat(form.price),
        type: form.type,
        description: form.description || null,
        isFeatured: form.isFeatured,
        image: form.image || null,
        sortOrder: parseInt(form.sortOrder) || 0,
      };
      if (form.categoryId) data.categoryId = form.categoryId;

      if (editProduct) {
        await productsApi.update(editProduct.id, data);
        toast.success('تم تحديث المنتج');
      } else {
        await productsApi.create(data);
        toast.success('تم إضافة المنتج');
      }
      setShowForm(false);
      setEditProduct(null);
      setForm(emptyForm);
      loadProducts();
    } catch (error) {
      toast.error('فشلت العملية');
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('هل أنت متأكد من الحذف؟')) return;
    try {
      await productsApi.delete(id);
      toast.success('تم حذف المنتج');
      loadProducts();
    } catch (error) {
      toast.error('فشل الحذف');
    }
  };

  /** Toggle featured inline */
  const toggleFeatured = async (product: Product) => {
    try {
      await productsApi.update(product.id, { isFeatured: !product.isFeatured });
      setProducts(prev => prev.map(p => p.id === product.id ? { ...p, isFeatured: !p.isFeatured } : p));
      toast.success(product.isFeatured ? 'تم إزالة التمييز' : 'تم تمييز المنتج ⭐');
    } catch {
      toast.error('فشل التحديث');
    }
  };

  /** Inline category change */
  const changeCategory = async (product: Product, newCategoryId: string) => {
    if (!newCategoryId || newCategoryId === product.categoryId) return;
    try {
      await productsApi.update(product.id, { categoryId: newCategoryId });
      const cat = flatCategories.find(c => c.id === newCategoryId);
      setProducts(prev => prev.map(p => p.id === product.id
        ? { ...p, categoryId: newCategoryId, category: cat ? { id: cat.id, name: cat.name, nameAr: cat.nameAr || undefined, icon: cat.icon || undefined } : p.category }
        : p
      ));
      toast.success('تم تغيير التصنيف');
    } catch {
      toast.error('فشل تغيير التصنيف');
    }
  };

  /** Inline sort order change */
  const changeSortOrder = async (product: Product, newOrder: number) => {
    try {
      await productsApi.update(product.id, { sortOrder: newOrder });
      setProducts(prev => prev.map(p => p.id === product.id ? { ...p, sortOrder: newOrder } : p));
    } catch {
      toast.error('فشل تحديث الترتيب');
    }
  };

  const openEditModal = (product: Product) => {
    setEditProduct(product);
    setForm({
      name: product.name,
      nameAr: product.nameAr || '',
      price: String(product.price),
      type: product.type,
      description: product.description || '',
      categoryId: product.categoryId || '',
      isFeatured: product.isFeatured,
      image: product.image || '',
      sortOrder: String(product.sortOrder ?? 0),
    });
    setShowForm(true);
  };

  const typeLabels: Record<string, string> = {
    SERVICE: 'خدمة',
    GIFT_CARD: 'بطاقة هدية',
    GAME_CARD: 'بطاقة لعبة',
    TOP_UP: 'شحن',
    SUBSCRIPTION: 'اشتراك',
  };

  return (
    <div>
      <PageBanner
        title="إدارة المنتجات"
        subtitle="عرض وتعديل المنتجات والخدمات الرقمية"
        icon={CubeIcon}
        gradient="from-orange-500 via-amber-500 to-yellow-400"
        pattern="grid"
      />
      <div className="flex items-center justify-between mb-4">
        <div>
          <h1 className="text-xl font-bold text-gray-900 dark:text-white">المنتجات</h1>
          <p className="text-xs text-gray-400 mt-0.5">
            الإجمالي: {products.length} منتج — التصنيفات: {flatCategories.length} — المميزة: {products.filter(p => p.isFeatured).length}
          </p>
        </div>
        <div className="flex items-center gap-1.5">
          <button onClick={loadProducts} className="p-1.5 rounded-lg text-gray-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-dark-card transition-all duration-200">
            <ArrowPathIcon className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
          <button onClick={() => { setEditProduct(null); setForm(emptyForm); setShowForm(true); }} className="btn-primary inline-flex items-center gap-1 text-sm px-3 py-1.5">
            <PlusIcon className="w-3.5 h-3.5" /> إضافة منتج
          </button>
        </div>
      </div>

      {/* DHRU FUSION Provider Panel */}
      <div className="relative overflow-hidden rounded-2xl mb-5 border border-gray-200/60 dark:border-dark-border">
        {/* Background image */}
        <div className="absolute inset-0 bg-gradient-to-r from-slate-900/95 via-blue-900/90 to-indigo-900/90">
          <img
            src="https://6990ab01681c79fa0bccfe99.imgix.net/ic_logo.svg"
            alt=""
            className="absolute left-4 top-1/2 -translate-y-1/2 w-24 h-24 opacity-10"
          />
          <img
            src="https://6990ab01681c79fa0bccfe99.imgix.net/ic_logo.svg"
            alt=""
            className="absolute right-8 top-1/2 -translate-y-1/2 w-40 h-40 opacity-[0.04]"
          />
        </div>

        <div className="relative z-10 p-4">
          {/* Header */}
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-xl bg-white/10 backdrop-blur-sm flex items-center justify-center border border-white/10">
                <img src="https://6990ab01681c79fa0bccfe99.imgix.net/ic_logo.svg" alt="DHRU FUSION" className="w-5 h-5" />
              </div>
              <div>
                <h2 className="text-sm font-bold text-white flex items-center gap-1.5">
                  DHRU FUSION
                  <span className="text-[10px] font-medium px-1.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 border border-emerald-500/20">متصل</span>
                </h2>
                <p className="text-[11px] text-blue-200/60">مزود الخدمات والمنتجات الرقمية</p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              {providerBalance !== null && (
                <span className="text-xs font-semibold bg-emerald-500/15 text-emerald-300 px-2.5 py-1 rounded-full border border-emerald-500/20">
                  ${providerBalance.toFixed(2)}
                </span>
              )}
              <button
                onClick={() => setShowProviderSettings(!showProviderSettings)}
                className="p-1.5 rounded-lg bg-white/10 hover:bg-white/20 text-white/70 hover:text-white transition-all duration-200 border border-white/10"
                title="إعدادات الاتصال"
              >
                <Cog6ToothIcon className="w-4 h-4" />
              </button>
            </div>
          </div>

          {/* Provider Settings Panel */}
          {showProviderSettings && (
            <div className="mb-3 p-3 rounded-xl bg-white/5 backdrop-blur-sm border border-white/10">
              <h3 className="text-xs font-semibold text-white/80 mb-2 flex items-center gap-1"><Cog6ToothIcon className="w-3.5 h-3.5" /> إعدادات الاتصال بالمصدر</h3>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
                <div>
                  <label className="block text-[10px] text-blue-200/60 mb-0.5 flex items-center gap-1"><GlobeAltIcon className="w-3 h-3" /> رابط API</label>
                  <input
                    value={providerForm.url}
                    onChange={e => setProviderForm({ ...providerForm, url: e.target.value })}
                    placeholder={providerSettings.url || 'https://...'}
                    className="w-full text-xs px-2.5 py-1.5 rounded-lg bg-white/10 border border-white/10 text-white placeholder-white/30 focus:outline-none focus:border-blue-400/50"
                  />
                </div>
                <div>
                  <label className="block text-[10px] text-blue-200/60 mb-0.5 flex items-center gap-1"><UserIcon className="w-3 h-3" /> اسم المستخدم</label>
                  <input
                    value={providerForm.username}
                    onChange={e => setProviderForm({ ...providerForm, username: e.target.value })}
                    placeholder={providerSettings.username || 'username'}
                    className="w-full text-xs px-2.5 py-1.5 rounded-lg bg-white/10 border border-white/10 text-white placeholder-white/30 focus:outline-none focus:border-blue-400/50"
                  />
                </div>
                <div>
                  <label className="block text-[10px] text-blue-200/60 mb-0.5 flex items-center gap-1"><KeyIcon className="w-3 h-3" /> مفتاح API</label>
                  <div className="relative">
                    <input
                      type={showApiKey ? 'text' : 'password'}
                      value={providerForm.apiKey}
                      onChange={e => setProviderForm({ ...providerForm, apiKey: e.target.value })}
                      placeholder={providerSettings.apiKey || '••••••••'}
                      className="w-full text-xs px-2.5 py-1.5 rounded-lg bg-white/10 border border-white/10 text-white placeholder-white/30 focus:outline-none focus:border-blue-400/50 pl-2.5 pr-7"
                    />
                    <button type="button" onClick={() => setShowApiKey(!showApiKey)} className="absolute left-1.5 top-1/2 -translate-y-1/2 text-white/40 hover:text-white/70">
                      {showApiKey ? <EyeSlashIcon className="w-3.5 h-3.5" /> : <EyeIcon className="w-3.5 h-3.5" />}
                    </button>
                  </div>
                </div>
              </div>
              <div className="flex justify-end mt-2">
                <button
                  onClick={handleSaveProviderSettings}
                  disabled={savingProvider}
                  className="text-xs px-3 py-1.5 rounded-lg bg-blue-500/80 hover:bg-blue-500 text-white transition-all duration-200 flex items-center gap-1 disabled:opacity-50"
                >
                  {savingProvider ? <ArrowPathIcon className="w-3 h-3 animate-spin" /> : <CheckBadgeIcon className="w-3 h-3" />}
                  حفظ الإعدادات
                </button>
              </div>
            </div>
          )}

          {/* Sync Controls */}
          <div className="flex flex-wrap items-end gap-3">
            <div>
              <label className="block text-[10px] text-blue-200/60 mb-0.5">نسبة الربح %</label>
              <input type="number" min="0" max="500" value={markupPercent} onChange={e => setMarkupPercent(parseInt(e.target.value) || 0)}
                className="w-20 text-xs px-2.5 py-1.5 rounded-lg bg-white/10 border border-white/10 text-white focus:outline-none focus:border-blue-400/50"
              />
            </div>
            <button onClick={handleSync} disabled={syncing}
              className="text-xs px-3 py-1.5 rounded-lg bg-blue-500/80 hover:bg-blue-500 text-white transition-all duration-200 flex items-center gap-1.5 disabled:opacity-50"
            >
              {syncing ? (
                <><ArrowPathIcon className="w-3.5 h-3.5 animate-spin" /> جاري المزامنة...</>
              ) : (<><CloudArrowDownIcon className="w-3.5 h-3.5" /> مزامنة المنتجات</>)}
            </button>
            <button onClick={loadProviderBalance}
              className="text-xs px-3 py-1.5 rounded-lg bg-white/10 hover:bg-white/20 text-white/80 hover:text-white border border-white/10 transition-all duration-200"
            >
              تحديث الرصيد
            </button>
            <button onClick={() => setShowDeleteConfirm(true)}
              className="text-xs px-3 py-1.5 rounded-lg bg-red-500/20 hover:bg-red-500/40 text-red-300 hover:text-red-200 border border-red-500/20 transition-all duration-200 flex items-center gap-1.5"
            >
              <TrashIcon className="w-3.5 h-3.5" /> حذف جميع المنتجات
            </button>
          </div>

          {/* Delete Confirmation */}
          {showDeleteConfirm && (
            <div className="mt-2.5 p-3 bg-red-500/10 backdrop-blur-sm rounded-xl border border-red-500/20">
              <p className="text-xs text-red-200 mb-2">⚠️ هل أنت متأكد من حذف جميع منتجات المصدر؟ لا يمكن التراجع عن هذا الإجراء.</p>
              <div className="flex items-center gap-2">
                <button
                  onClick={async () => {
                    setDeletingProducts(true);
                    try {
                      const res = await adminApi.deleteAllProviderProducts();
                      const count = res.data?.data?.deleted || 0;
                      toast.success(`تم حذف ${count} منتج بنجاح`);
                      setShowDeleteConfirm(false);
                      loadProducts();
                    } catch {
                      toast.error('فشل حذف المنتجات');
                    } finally {
                      setDeletingProducts(false);
                    }
                  }}
                  disabled={deletingProducts}
                  className="text-xs px-3 py-1.5 rounded-lg bg-red-500/80 hover:bg-red-500 text-white transition-all duration-200 flex items-center gap-1 disabled:opacity-50"
                >
                  {deletingProducts ? <><ArrowPathIcon className="w-3 h-3 animate-spin" /> جاري الحذف...</> : <><TrashIcon className="w-3 h-3" /> نعم، احذف الكل</>}
                </button>
                <button
                  onClick={() => setShowDeleteConfirm(false)}
                  className="text-xs px-3 py-1.5 rounded-lg bg-white/10 hover:bg-white/20 text-white/70 hover:text-white border border-white/10 transition-all duration-200"
                >
                  إلغاء
                </button>
              </div>
            </div>
          )}

          {syncResult && (
            <div className="mt-2.5 p-2 bg-white/5 backdrop-blur-sm rounded-lg text-[11px] border border-white/10">
              <p className="text-blue-200">
                <strong>نتيجة المزامنة:</strong> إجمالي {syncResult.total} — جديد: {syncResult.created} — محدث: {syncResult.updated} — تخطي: {syncResult.skipped}
              </p>
              {syncResult.errors?.length > 0 && (
                <p className="text-red-300 mt-0.5">أخطاء: {syncResult.errors.length}</p>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Service Type Tabs */}
      <div className="card mb-3 !py-2.5 !px-3">
        <div className="flex items-center gap-2">
          <span className="text-xs font-medium text-gray-500 dark:text-gray-400 ml-1">نوع الخدمة:</span>
          <div className="flex bg-gray-100 dark:bg-dark-surface rounded-lg p-0.5 gap-0.5">
            {[
              { value: '', label: 'الكل', icon: CubeIcon, count: serviceTypeCounts.all },
              { value: 'IMEI', label: 'IMEI', icon: DevicePhoneMobileIcon, count: serviceTypeCounts.IMEI },
              { value: 'SERVER', label: 'SERVER', icon: ServerStackIcon, count: serviceTypeCounts.SERVER },
            ].map(tab => (
              <button
                key={tab.value}
                onClick={() => setFilterServiceType(tab.value)}
                className={`flex items-center gap-1 px-3 py-1.5 rounded-md text-xs font-medium transition-all duration-200 ${
                  filterServiceType === tab.value
                    ? 'bg-white dark:bg-dark-card text-gray-900 dark:text-white shadow-sm'
                    : 'text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200'
                }`}
              >
                <tab.icon className="w-3.5 h-3.5" />
                {tab.label}
                <span className={`text-[10px] px-1.5 py-0.5 rounded-full ${
                  filterServiceType === tab.value
                    ? 'bg-primary-100 dark:bg-primary-900/30 text-primary-700 dark:text-primary-400'
                    : 'bg-gray-200 dark:bg-dark-border text-gray-500 dark:text-gray-400'
                }`}>{tab.count}</span>
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Filters Bar */}
      <div className="card mb-4 !py-3 !px-3">
        <div className="flex flex-wrap items-end gap-3">
          {/* Search */}
          <div className="flex-1 min-w-[180px]">
            <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-0.5 flex items-center gap-1"><MagnifyingGlassIcon className="w-3.5 h-3.5" /> بحث</label>
            <input
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              placeholder="اسم المنتج، المجموعة، أو المعرف..."
              className="input-field text-sm"
            />
          </div>
          {/* Category Filter */}
          <div className="min-w-[180px]">
            <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-0.5 flex items-center gap-1"><FolderIcon className="w-3.5 h-3.5" /> التصنيف</label>
            <select value={filterCategory} onChange={e => setFilterCategory(e.target.value)} className="input-field text-sm">
              <option value="">جميع التصنيفات</option>
              {flatCategories.map(cat => (
                <option key={cat.id} value={cat.id}>{cat.icon || ''} {cat.nameAr || cat.name} ({cat._count?.products ?? ''})</option>
              ))}
            </select>
          </div>
          {/* Featured Filter */}
          <div className="min-w-[120px]">
            <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-0.5 flex items-center gap-1"><StarIcon className="w-3.5 h-3.5" /> المميزة</label>
            <select value={filterFeatured} onChange={e => setFilterFeatured(e.target.value)} className="input-field text-sm">
              <option value="">الكل</option>
              <option value="yes">مميزة فقط</option>
              <option value="no">غير مميزة</option>
            </select>
          </div>
          {/* Results count */}
          <div className="text-xs text-gray-500 self-end pb-1.5">
            {filteredProducts.length} نتيجة
          </div>
        </div>
      </div>

      {/* Product Form Modal */}
      {showForm && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4" onClick={e => { if (e.target === e.currentTarget) { setShowForm(false); setEditProduct(null); } }}>
          <div className="card max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <h2 className="text-xl font-bold mb-4 text-gray-900 dark:text-white flex items-center gap-2">
              {editProduct ? <><PencilSquareIcon className="w-5 h-5 text-blue-500" /> تعديل المنتج</> : <><PlusIcon className="w-5 h-5 text-emerald-500" /> إضافة منتج جديد</>}
            </h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">الاسم (EN) *</label>
                  <input value={form.name} onChange={e => setForm({...form, name: e.target.value})} className="input-field" required />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">الاسم (AR)</label>
                  <input value={form.nameAr} onChange={e => setForm({...form, nameAr: e.target.value})} className="input-field" />
                </div>
              </div>
              <div className="grid grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">السعر *</label>
                  <input type="number" step="0.01" value={form.price} onChange={e => setForm({...form, price: e.target.value})} className="input-field" required />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">النوع</label>
                  <select value={form.type} onChange={e => setForm({...form, type: e.target.value})} className="input-field">
                    {Object.entries(typeLabels).map(([key, label]) => (
                      <option key={key} value={key}>{label}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">ترتيب الظهور</label>
                  <input type="number" value={form.sortOrder} onChange={e => setForm({...form, sortOrder: e.target.value})} className="input-field" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1 flex items-center gap-1"><FolderIcon className="w-4 h-4" /> التصنيف</label>
                <select value={form.categoryId} onChange={e => setForm({...form, categoryId: e.target.value})} className="input-field">
                  <option value="">بدون تصنيف</option>
                  {flatCategories.map(cat => (
                    <option key={cat.id} value={cat.id}>{cat.icon || ''} {cat.nameAr || cat.name}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">رابط الصورة</label>
                <input value={form.image} onChange={e => setForm({...form, image: e.target.value})} className="input-field" placeholder="https://..." />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">الوصف</label>
                <textarea value={form.description} onChange={e => setForm({...form, description: e.target.value})} className="input-field" rows={3} />
              </div>
              <label className="flex items-center gap-2 cursor-pointer p-3 rounded-lg border border-gray-200 dark:border-dark-border hover:bg-gray-50 dark:hover:bg-dark-surface transition">
                <input type="checkbox" checked={form.isFeatured} onChange={e => setForm({...form, isFeatured: e.target.checked})} className="w-5 h-5 rounded accent-primary-500" />
                <span className="text-sm font-medium text-gray-700 dark:text-gray-300 flex items-center gap-1"><StarIcon className="w-4 h-4 text-amber-500" /> منتج مميز (يظهر في الصفحة الرئيسية)</span>
              </label>
              <div className="flex gap-3 pt-2">
                <button type="submit" className="btn-primary flex-1 inline-flex items-center justify-center gap-1.5"><CheckBadgeIcon className="w-4 h-4" /> حفظ</button>
                <button type="button" onClick={() => { setShowForm(false); setEditProduct(null); }} className="flex-1 py-2 px-4 rounded-lg border border-gray-300 dark:border-dark-border text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-dark-card transition">
                  إلغاء
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Products Table */}
      <div className="card">
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="w-10 h-10 border-[3px] border-gray-200 dark:border-dark-border border-t-primary-500 rounded-full animate-spin"></div>
          </div>
        ) : filteredProducts.length === 0 ? (
          <div className="text-center py-16">
            <CubeIcon className="w-12 h-12 text-gray-300 dark:text-gray-600 mx-auto mb-3" />
            <p className="text-gray-400">لا توجد منتجات مطابقة للفلتر</p>
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-100 dark:border-dark-border">
                    <th className="table-header">المنتج</th>
                    <th className="table-header">التصنيف</th>
                    <th className="table-header">النوع</th>
                    <th className="table-header">التكلفة</th>
                    <th className="table-header">السعر</th>
                    <th className="table-header text-center">مميز</th>
                    <th className="table-header text-center">الترتيب</th>
                    <th className="table-header">إجراءات</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedProducts.map((product) => (
                    <tr key={product.id} className="table-row group">
                      <td className="py-3 px-3">
                        <div className="max-w-[250px]">
                          <p className="font-medium text-gray-900 dark:text-white truncate">{product.nameAr || product.name}</p>
                          <p className="text-xs text-gray-500 truncate">{product.name}</p>
                          {product.externalId && <p className="text-xs text-green-500 flex items-center gap-0.5"><LinkIcon className="w-3 h-3" />{product.externalId}</p>}
                        </div>
                      </td>
                      <td className="py-3 px-3">
                        <select
                          value={product.categoryId || ''}
                          onChange={e => changeCategory(product, e.target.value)}
                          className="text-xs bg-transparent border border-transparent hover:border-gray-300 dark:hover:border-dark-border rounded px-1 py-1 text-gray-600 dark:text-gray-400 cursor-pointer focus:ring-1 focus:ring-primary-500 focus:border-primary-500 outline-none max-w-[160px]"
                        >
                          <option value="">بدون</option>
                          {flatCategories.map(cat => (
                            <option key={cat.id} value={cat.id}>{cat.icon || ''} {cat.nameAr || cat.name}</option>
                          ))}
                        </select>
                      </td>
                      <td className="py-3 px-3">
                        <span className="text-gray-600 dark:text-gray-400 text-xs">{typeLabels[product.type] || product.type}</span>
                        {product.serviceType && <span className="block text-xs text-blue-500">{product.serviceType}</span>}
                      </td>
                      <td className="py-3 px-3 text-gray-500 text-xs">{product.costPrice != null ? `$${product.costPrice}` : '-'}</td>
                      <td className="py-3 px-3 font-semibold text-primary-600">${product.price}</td>
                      <td className="py-3 px-3 text-center">
                        <button
                          onClick={() => toggleFeatured(product)}
                          className={`transition-all duration-200 hover:scale-125 ${product.isFeatured ? 'text-amber-400' : 'text-gray-300 dark:text-gray-600 hover:text-amber-300'}`}
                          title={product.isFeatured ? 'إزالة من المميزة' : 'إضافة للمميزة'}
                        >
                          {product.isFeatured ? <StarSolid className="w-5 h-5" /> : <StarIcon className="w-5 h-5" />}
                        </button>
                      </td>
                      <td className="py-3 px-3 text-center">
                        <input
                          type="number"
                          value={product.sortOrder ?? 0}
                          onChange={e => {
                            const val = parseInt(e.target.value) || 0;
                            setProducts(prev => prev.map(p => p.id === product.id ? { ...p, sortOrder: val } : p));
                          }}
                          onBlur={e => changeSortOrder(product, parseInt(e.target.value) || 0)}
                          className="w-16 text-center text-xs bg-transparent border border-transparent hover:border-gray-300 dark:hover:border-dark-border rounded px-1 py-1 text-gray-600 dark:text-gray-400 focus:ring-1 focus:ring-primary-500 focus:border-primary-500 outline-none"
                        />
                      </td>
                      <td className="py-3 px-3">
                        <div className="flex gap-2">
                          <button onClick={() => openEditModal(product)} className="inline-flex items-center gap-1 px-2 py-1.5 rounded-lg text-xs font-medium text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-500/10 hover:bg-blue-100 dark:hover:bg-blue-500/20 transition-all duration-200">
                            <PencilSquareIcon className="w-3.5 h-3.5" /> تعديل
                          </button>
                          <button onClick={() => handleDelete(product.id)} className="inline-flex items-center gap-1 px-2 py-1.5 rounded-lg text-xs font-medium text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-500/10 hover:bg-red-100 dark:hover:bg-red-500/20 transition-all duration-200">
                            <TrashIcon className="w-3.5 h-3.5" /> حذف
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex items-center justify-between mt-4 pt-4 border-t border-gray-200 dark:border-dark-border">
                <p className="text-sm text-gray-500">
                  عرض {(currentPage - 1) * perPage + 1} - {Math.min(currentPage * perPage, filteredProducts.length)} من {filteredProducts.length}
                </p>
                <div className="flex gap-1">
                  <button
                    onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                    disabled={currentPage === 1}
                    className="px-3 py-1.5 text-sm rounded-lg border border-gray-300 dark:border-dark-border text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-dark-card disabled:opacity-40 disabled:cursor-not-allowed transition"
                  >
                    السابق
                  </button>
                  {Array.from({ length: Math.min(totalPages, 7) }, (_, i) => {
                    let page: number;
                    if (totalPages <= 7) {
                      page = i + 1;
                    } else if (currentPage <= 4) {
                      page = i + 1;
                    } else if (currentPage >= totalPages - 3) {
                      page = totalPages - 6 + i;
                    } else {
                      page = currentPage - 3 + i;
                    }
                    return (
                      <button
                        key={page}
                        onClick={() => setCurrentPage(page)}
                        className={`px-3 py-1.5 text-sm rounded-lg transition ${currentPage === page
                          ? 'bg-primary-500 text-black font-semibold'
                          : 'border border-gray-300 dark:border-dark-border text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-dark-card'
                        }`}
                      >
                        {page}
                      </button>
                    );
                  })}
                  <button
                    onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                    disabled={currentPage === totalPages}
                    className="px-3 py-1.5 text-sm rounded-lg border border-gray-300 dark:border-dark-border text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-dark-card disabled:opacity-40 disabled:cursor-not-allowed transition"
                  >
                    التالي
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
