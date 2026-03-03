import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { adminApi } from '../api/client';
import {
  ArrowPathIcon, PlusIcon, PencilSquareIcon, TrashIcon,
  PhotoIcon, EyeSlashIcon, EyeIcon
} from '@heroicons/react/24/outline';
import PageBanner from '../components/PageBanner';

interface Banner {
  id: string;
  title: string | null;
  image: string;
  link: string | null;
  sortOrder: number;
  isActive: boolean;
  createdAt: string;
}

export default function Banners() {
  const [banners, setBanners] = useState<Banner[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editBanner, setEditBanner] = useState<Banner | null>(null);

  const [form, setForm] = useState({
    title: '',
    image: '',
    link: '',
    sortOrder: '0',
    isActive: true,
  });

  useEffect(() => { loadBanners(); }, []);

  const loadBanners = async () => {
    try {
      const response = await adminApi.getBanners();
      setBanners(response.data.data || []);
    } catch (error) {
      toast.error('فشل تحميل البانرات');
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setForm({ title: '', image: '', link: '', sortOrder: '0', isActive: true });
    setEditBanner(null);
    setShowForm(false);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.image) {
      toast.error('رابط الصورة مطلوب');
      return;
    }
    try {
      const data = {
        title: form.title || null,
        image: form.image,
        link: form.link || null,
        sortOrder: parseInt(form.sortOrder) || 0,
        isActive: form.isActive,
      };
      if (editBanner) {
        await adminApi.updateBanner(editBanner.id, data);
        toast.success('تم تحديث البانر');
      } else {
        await adminApi.createBanner(data);
        toast.success('تم إضافة البانر');
      }
      resetForm();
      loadBanners();
    } catch (error) {
      toast.error('فشلت العملية');
    }
  };

  const handleEdit = (banner: Banner) => {
    setEditBanner(banner);
    setForm({
      title: banner.title || '',
      image: banner.image,
      link: banner.link || '',
      sortOrder: String(banner.sortOrder),
      isActive: banner.isActive,
    });
    setShowForm(true);
  };

  const handleDelete = async (id: string) => {
    if (!confirm('هل أنت متأكد من حذف هذا البانر؟')) return;
    try {
      await adminApi.deleteBanner(id);
      toast.success('تم حذف البانر');
      loadBanners();
    } catch (error) {
      toast.error('فشل الحذف');
    }
  };

  const toggleActive = async (banner: Banner) => {
    try {
      await adminApi.updateBanner(banner.id, { isActive: !banner.isActive });
      toast.success(banner.isActive ? 'تم إخفاء البانر' : 'تم تفعيل البانر');
      loadBanners();
    } catch (error) {
      toast.error('فشلت العملية');
    }
  };

  return (
    <div>
      <PageBanner
        title="إدارة البانرات"
        subtitle="إدارة إعلانات ولافتات الواجهة الرئيسية"
        icon={PhotoIcon}
        gradient="from-pink-600 via-rose-500 to-red-400"
        pattern="lines"
      />
      <div className="flex flex-col sm:flex-row sm:items-center justify-between mb-6 gap-3">
        <h1 className="page-title">البانرات</h1>
        <div className="flex items-center gap-2">
          <button onClick={loadBanners} className="p-2 rounded-xl text-gray-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-dark-card transition-all duration-200">
            <ArrowPathIcon className={`w-5 h-5 ${loading ? 'animate-spin' : ''}`} />
          </button>
          <button onClick={() => { resetForm(); setShowForm(true); }} className="btn-primary inline-flex items-center gap-1.5"><PlusIcon className="w-4 h-4" /> إضافة بانر</button>
        </div>
      </div>

      {/* Form Modal */}
      {showForm && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 animate-fade-in-up" style={{ zIndex: 60, animationDuration: '0.2s' }}>
          <div className="card max-w-lg w-full max-h-[90vh] overflow-y-auto animate-scale-in">
            <h2 className="text-xl font-bold mb-4 text-gray-900 dark:text-white">
              {editBanner ? 'تعديل البانر' : 'إضافة بانر جديد'}
            </h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">العنوان (اختياري)</label>
                <input value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} className="input-field" placeholder="عنوان البانر" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">رابط الصورة *</label>
                <input value={form.image} onChange={e => setForm({ ...form, image: e.target.value })} className="input-field" placeholder="https://example.com/banner.jpg" required />
              </div>
              {form.image && (
                <div className="rounded-lg overflow-hidden border border-gray-200 dark:border-dark-border">
                  <img src={form.image} alt="معاينة" className="w-full h-40 object-cover" onError={(e) => { (e.target as HTMLImageElement).style.display = 'none'; }} />
                </div>
              )}
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">رابط الوجهة (اختياري)</label>
                <input value={form.link} onChange={e => setForm({ ...form, link: e.target.value })} className="input-field" placeholder="https://example.com/page" />
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">الترتيب</label>
                  <input type="number" value={form.sortOrder} onChange={e => setForm({ ...form, sortOrder: e.target.value })} className="input-field" />
                </div>
                <div className="flex items-end pb-1">
                  <label className="flex items-center gap-2 cursor-pointer">
                    <input type="checkbox" checked={form.isActive} onChange={e => setForm({ ...form, isActive: e.target.checked })} className="w-4 h-4 rounded accent-primary-500" />
                    <span className="text-sm text-gray-700 dark:text-gray-300">مفعّل</span>
                  </label>
                </div>
              </div>
              <div className="flex gap-3 pt-2">
                <button type="submit" className="btn-primary flex-1">حفظ</button>
                <button type="button" onClick={resetForm} className="flex-1 py-2 px-4 rounded-lg border border-gray-300 dark:border-dark-border text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-dark-card transition">إلغاء</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Banners Grid */}
      <div className="card">
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="w-10 h-10 border-[3px] border-gray-200 dark:border-dark-border border-t-primary-500 rounded-full animate-spin"></div>
          </div>
        ) : banners.length === 0 ? (
          <div className="text-center py-16">
            <PhotoIcon className="w-12 h-12 text-gray-300 dark:text-gray-600 mx-auto mb-3" />
            <p className="text-gray-400">لا توجد بانرات بعد</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {banners.map((banner, i) => (
              <div key={banner.id} className="border border-gray-200 dark:border-dark-border rounded-2xl overflow-hidden group card-hover animate-fade-in-up" style={{ animationDelay: `${i * 50}ms` }}>
                <div className="relative h-40">
                  <img src={banner.image} alt={banner.title || 'بانر'} className="w-full h-full object-cover" />
                  <div className="absolute top-2 right-2 flex gap-1">
                    <span className={`badge ${banner.isActive ? 'bg-emerald-50 text-emerald-600 dark:bg-emerald-500/10 dark:text-emerald-400' : 'bg-red-50 text-red-600 dark:bg-red-500/10 dark:text-red-400'}`}>
                      {banner.isActive ? 'مفعّل' : 'مخفي'}
                    </span>
                    <span className="badge bg-black/40 text-white backdrop-blur-sm">
                      #{banner.sortOrder}
                    </span>
                  </div>
                </div>
                <div className="p-3">
                  <p className="font-medium text-gray-900 dark:text-white truncate">{banner.title || 'بدون عنوان'}</p>
                  {banner.link && <p className="text-xs text-gray-400 truncate mt-1">{banner.link}</p>}
                  <div className="flex gap-2 mt-3">
                    <button onClick={() => handleEdit(banner)} className="inline-flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs font-medium text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-500/10 hover:bg-blue-100 dark:hover:bg-blue-500/20 transition-all duration-200">
                      <PencilSquareIcon className="w-3.5 h-3.5" /> تعديل
                    </button>
                    <button onClick={() => toggleActive(banner)} className={`inline-flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs font-medium transition-all duration-200 ${
                      banner.isActive
                        ? 'text-amber-600 dark:text-amber-400 bg-amber-50 dark:bg-amber-500/10 hover:bg-amber-100 dark:hover:bg-amber-500/20'
                        : 'text-emerald-600 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-500/10 hover:bg-emerald-100 dark:hover:bg-emerald-500/20'
                    }`}>
                      {banner.isActive ? <><EyeSlashIcon className="w-3.5 h-3.5" /> إخفاء</> : <><EyeIcon className="w-3.5 h-3.5" /> تفعيل</>}
                    </button>
                    <button onClick={() => handleDelete(banner.id)} className="inline-flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs font-medium text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-500/10 hover:bg-red-100 dark:hover:bg-red-500/20 transition-all duration-200">
                      <TrashIcon className="w-3.5 h-3.5" /> حذف
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
