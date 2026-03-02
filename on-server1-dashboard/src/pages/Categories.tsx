import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { productsApi } from '../api/client';
import { ArrowPathIcon, PlusIcon, FolderIcon, FolderOpenIcon, PencilSquareIcon, DocumentIcon } from '@heroicons/react/24/outline';
import PageBanner from '../components/PageBanner';

interface Category {
  id: string;
  name: string;
  nameAr: string | null;
  icon: string | null;
  image: string | null;
  sortOrder: number;
  isActive: boolean;
  parentId: string | null;
  children?: Category[];
  _count?: { products: number };
}

export default function Categories() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editCategory, setEditCategory] = useState<Category | null>(null);

  const [form, setForm] = useState({
    name: '',
    nameAr: '',
    icon: '',
    image: '',
    sortOrder: '0',
    parentId: '',
  });

  useEffect(() => { loadCategories(); }, []);

  const loadCategories = async () => {
    try {
      const response = await productsApi.getCategories();
      setCategories(response.data.data || []);
    } catch (error) {
      toast.error('فشل تحميل التصنيفات');
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setForm({ name: '', nameAr: '', icon: '', image: '', sortOrder: '0', parentId: '' });
    setEditCategory(null);
    setShowForm(false);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const data: any = {
        name: form.name,
        nameAr: form.nameAr || null,
        icon: form.icon || null,
        image: form.image || null,
        sortOrder: parseInt(form.sortOrder) || 0,
      };
      if (form.parentId) data.parentId = form.parentId;

      if (editCategory) {
        await productsApi.updateCategory(editCategory.id, data);
        toast.success('تم تحديث التصنيف');
      } else {
        await productsApi.createCategory(data);
        toast.success('تم إضافة التصنيف');
      }
      resetForm();
      loadCategories();
    } catch (error) {
      toast.error('فشلت العملية');
    }
  };

  const handleEdit = (category: Category) => {
    setEditCategory(category);
    setForm({
      name: category.name,
      nameAr: category.nameAr || '',
      icon: category.icon || '',
      image: category.image || '',
      sortOrder: String(category.sortOrder),
      parentId: category.parentId || '',
    });
    setShowForm(true);
  };

  // Flatten categories for parent selection (exclude current edit)
  const flatCategories = categories.filter(c => !editCategory || c.id !== editCategory.id);

  return (
    <div>
      <PageBanner
        title="إدارة التصنيفات"
        subtitle="تنظيم المنتجات في تصنيفات وفئات منظمة"
        icon={FolderOpenIcon}
        gradient="from-sky-600 via-blue-600 to-indigo-500"
        pattern="triangles"
      />
      <div className="flex items-center justify-between mb-6">
        <h1 className="page-title">التصنيفات</h1>
        <div className="flex items-center gap-2">
          <button onClick={loadCategories} className="p-2 rounded-xl text-gray-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-dark-card transition-all duration-200">
            <ArrowPathIcon className={`w-5 h-5 ${loading ? 'animate-spin' : ''}`} />
          </button>
          <button onClick={() => { resetForm(); setShowForm(true); }} className="btn-primary inline-flex items-center gap-1.5"><PlusIcon className="w-4 h-4" /> إضافة تصنيف</button>
        </div>
      </div>

      {/* Form Modal */}
      {showForm && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4 animate-fade-in-up" style={{ animationDuration: '0.2s' }}>
          <div className="card max-w-lg w-full max-h-[90vh] overflow-y-auto animate-scale-in">
            <h2 className="text-xl font-bold mb-4 text-gray-900 dark:text-white">
              {editCategory ? 'تعديل التصنيف' : 'إضافة تصنيف جديد'}
            </h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">الاسم (EN) *</label>
                  <input value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} className="input-field" placeholder="Games" required />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">الاسم (AR)</label>
                  <input value={form.nameAr} onChange={e => setForm({ ...form, nameAr: e.target.value })} className="input-field" placeholder="الألعاب" />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">الأيقونة (إيموجي)</label>
                  <input value={form.icon} onChange={e => setForm({ ...form, icon: e.target.value })} className="input-field" placeholder="🎮" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">الترتيب</label>
                  <input type="number" value={form.sortOrder} onChange={e => setForm({ ...form, sortOrder: e.target.value })} className="input-field" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">رابط الصورة</label>
                <input value={form.image} onChange={e => setForm({ ...form, image: e.target.value })} className="input-field" placeholder="https://..." />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">التصنيف الأب (اختياري)</label>
                <select value={form.parentId} onChange={e => setForm({ ...form, parentId: e.target.value })} className="input-field">
                  <option value="">بدون (تصنيف رئيسي)</option>
                  {flatCategories.map((cat) => (
                    <option key={cat.id} value={cat.id}>{cat.icon} {cat.nameAr || cat.name}</option>
                  ))}
                </select>
              </div>
              <div className="flex gap-3 pt-2">
                <button type="submit" className="btn-primary flex-1">حفظ</button>
                <button type="button" onClick={resetForm} className="flex-1 py-2 px-4 rounded-lg border border-gray-300 dark:border-dark-border text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-dark-card transition">إلغاء</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Categories Table */}
      <div className="card">
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="w-10 h-10 border-[3px] border-gray-200 dark:border-dark-border border-t-primary-500 rounded-full animate-spin"></div>
          </div>
        ) : categories.length === 0 ? (
          <div className="text-center py-16">
            <FolderIcon className="w-12 h-12 text-gray-300 dark:text-gray-600 mx-auto mb-3" />
            <p className="text-gray-400">لا توجد تصنيفات بعد</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-100 dark:border-dark-border">
                  <th className="table-header">التصنيف</th>
                  <th className="table-header">الاسم EN</th>
                  <th className="table-header">المنتجات</th>
                  <th className="table-header">الترتيب</th>
                  <th className="table-header">التصنيفات الفرعية</th>
                  <th className="table-header">إجراءات</th>
                </tr>
              </thead>
              <tbody>
                {categories.map((category) => (
                  <>
                    <tr key={category.id} className="table-row animate-fade-in-up">
                      <td className="table-cell">
                        <div className="flex items-center gap-3">
                          <div className="w-9 h-9 rounded-lg bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center">
                            {category.icon ? <span className="text-lg">{category.icon}</span> : <FolderOpenIcon className="w-5 h-5 text-white" />}
                          </div>
                          <span className="font-medium text-gray-900 dark:text-white">{category.nameAr || category.name}</span>
                        </div>
                      </td>
                      <td className="table-cell text-gray-500 dark:text-gray-400">{category.name}</td>
                      <td className="table-cell">
                        <span className="badge bg-primary-500/10 text-primary-600 dark:text-primary-400">
                          {category._count?.products || 0} منتج
                        </span>
                      </td>
                      <td className="table-cell text-gray-400">{category.sortOrder}</td>
                      <td className="table-cell text-gray-400">{category.children?.length || 0} فرعي</td>
                      <td className="table-cell">
                        <button onClick={() => handleEdit(category)} className="inline-flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs font-medium text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-500/10 hover:bg-blue-100 dark:hover:bg-blue-500/20 transition-all duration-200">
                          <PencilSquareIcon className="w-3.5 h-3.5" /> تعديل
                        </button>
                      </td>
                    </tr>
                    {/* Children */}
                    {category.children?.map((child) => (
                      <tr key={child.id} className="border-b border-gray-100 dark:border-dark-border bg-gray-50/50 dark:bg-dark-surface/30">
                        <td className="py-2.5 px-4 pr-10">
                          <div className="flex items-center gap-2">
                            <span className="text-gray-300 dark:text-gray-600">↳</span>
                            {child.icon ? <span className="text-lg">{child.icon}</span> : <DocumentIcon className="w-4 h-4 text-gray-400" />}
                            <span className="text-gray-700 dark:text-gray-300">{child.nameAr || child.name}</span>
                          </div>
                        </td>
                        <td className="py-2.5 px-4 text-gray-500">{child.name}</td>
                        <td className="py-2.5 px-4 text-gray-500">-</td>
                        <td className="py-2.5 px-4 text-gray-400">{child.sortOrder}</td>
                        <td className="py-2.5 px-4"></td>
                        <td className="py-2.5 px-4">
                          <button onClick={() => handleEdit(child)} className="inline-flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs font-medium text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-500/10 hover:bg-blue-100 dark:hover:bg-blue-500/20 transition-all duration-200">
                            <PencilSquareIcon className="w-3.5 h-3.5" /> تعديل
                          </button>
                        </td>
                      </tr>
                    ))}
                  </>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
