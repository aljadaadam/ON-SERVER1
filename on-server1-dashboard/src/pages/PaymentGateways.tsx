import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { paymentGatewaysApi } from '../api/client';
import {
  ArrowPathIcon, PlusIcon, PencilSquareIcon, TrashIcon,
  EyeSlashIcon, EyeIcon, CreditCardIcon,
} from '@heroicons/react/24/outline';
import PageBanner from '../components/PageBanner';
import Modal from '../components/Modal';

interface Gateway {
  id: string;
  name: string;
  nameEn: string | null;
  type: string;
  icon: string | null;
  color: string | null;
  config: string | null;
  sortOrder: number;
  isActive: boolean;
  createdAt: string;
}

const GATEWAY_TYPES = [
  { value: 'CRYPTO', label: 'عملة رقمية' },
  { value: 'BANK', label: 'بنك / تحويل' },
  { value: 'MOBILE_WALLET', label: 'محفظة إلكترونية' },
  { value: 'CARD', label: 'بطاقة دفع' },
];

export default function PaymentGateways() {
  const [gateways, setGateways] = useState<Gateway[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editGateway, setEditGateway] = useState<Gateway | null>(null);
  const [deleting, setDeleting] = useState<string | null>(null);

  const [form, setForm] = useState({
    name: '',
    nameEn: '',
    type: 'BANK',
    icon: '',
    color: '#26A17B',
    sortOrder: '0',
    isActive: true,
  });

  useEffect(() => { loadGateways(); }, []);

  const loadGateways = async () => {
    try {
      const response = await paymentGatewaysApi.getAll();
      setGateways(response.data.data || []);
    } catch {
      toast.error('فشل تحميل بوابات الدفع');
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setForm({ name: '', nameEn: '', type: 'BANK', icon: '', color: '#26A17B', sortOrder: '0', isActive: true });
    setEditGateway(null);
    setShowForm(false);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.name.trim()) {
      toast.error('اسم البوابة مطلوب');
      return;
    }
    try {
      const data = {
        name: form.name.trim(),
        nameEn: form.nameEn.trim() || null,
        type: form.type,
        icon: form.icon.trim() || null,
        color: form.color || null,
        sortOrder: parseInt(form.sortOrder) || 0,
        isActive: form.isActive,
      };
      if (editGateway) {
        await paymentGatewaysApi.update(editGateway.id, data);
        toast.success('تم تحديث بوابة الدفع');
      } else {
        await paymentGatewaysApi.create(data);
        toast.success('تم إضافة بوابة الدفع');
      }
      resetForm();
      loadGateways();
    } catch {
      toast.error('فشلت العملية');
    }
  };

  const handleToggle = async (gateway: Gateway) => {
    try {
      await paymentGatewaysApi.update(gateway.id, { isActive: !gateway.isActive });
      toast.success(gateway.isActive ? 'تم إخفاء البوابة' : 'تم تفعيل البوابة');
      loadGateways();
    } catch {
      toast.error('فشل التحديث');
    }
  };

  const handleDelete = async (id: string) => {
    try {
      setDeleting(id);
      await paymentGatewaysApi.delete(id);
      toast.success('تم حذف بوابة الدفع');
      loadGateways();
    } catch {
      toast.error('فشل الحذف');
    } finally {
      setDeleting(null);
    }
  };

  const handleEdit = (gw: Gateway) => {
    setForm({
      name: gw.name,
      nameEn: gw.nameEn || '',
      type: gw.type,
      icon: gw.icon || '',
      color: gw.color || '#26A17B',
      sortOrder: gw.sortOrder.toString(),
      isActive: gw.isActive,
    });
    setEditGateway(gw);
    setShowForm(true);
  };

  const typeLabel = (type: string) => GATEWAY_TYPES.find(t => t.value === type)?.label || type;

  return (
    <div className="flex flex-col h-full min-h-0">
      <PageBanner
        title="بوابات الدفع"
        subtitle="إدارة طرق الدفع المتاحة للمستخدمين"
        icon={CreditCardIcon}
        gradient="from-emerald-600 to-teal-700"
      />

      <div className="flex items-center justify-between mb-4 mt-2">
        <div className="flex items-center gap-2">
          <CreditCardIcon className="w-5 h-5 text-emerald-400" />
          <span className="text-sm text-gray-400">{gateways.length} بوابة</span>
        </div>
        <div className="flex gap-2">
          <button
            onClick={loadGateways}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-gray-100 dark:bg-dark-card text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-dark-border text-sm transition-all"
          >
            <ArrowPathIcon className="w-4 h-4" />
            تحديث
          </button>
          <button
            onClick={() => { resetForm(); setShowForm(true); }}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-emerald-500 text-white hover:bg-emerald-600 text-sm font-medium transition-all shadow-lg shadow-emerald-500/20"
          >
            <PlusIcon className="w-4 h-4" />
            إضافة بوابة
          </button>
        </div>
      </div>

      {/* Gateways List */}
      <div className="flex-1 overflow-y-auto min-h-0">
        {loading ? (
          <div className="flex items-center justify-center py-20">
            <div className="w-8 h-8 border-2 border-emerald-500 border-t-transparent rounded-full animate-spin" />
          </div>
        ) : gateways.length === 0 ? (
          <div className="text-center py-20 text-gray-400">
            <CreditCardIcon className="w-12 h-12 mx-auto mb-3 opacity-30" />
            <p>لا توجد بوابات دفع</p>
          </div>
        ) : (
          <div className="grid gap-3">
            {gateways.map((gw) => (
              <div
                key={gw.id}
                className={`relative rounded-xl border transition-all duration-200 ${
                  gw.isActive
                    ? 'bg-white dark:bg-dark-card border-gray-200 dark:border-dark-border'
                    : 'bg-gray-50 dark:bg-dark-bg/50 border-gray-200/50 dark:border-dark-border/50 opacity-60'
                }`}
              >
                <div className="flex items-center gap-4 p-4">
                  {/* Color indicator */}
                  <div
                    className="w-12 h-12 rounded-xl flex items-center justify-center text-white font-bold text-lg shrink-0"
                    style={{ backgroundColor: gw.color || '#6B7280' }}
                  >
                    {gw.name.charAt(0)}
                  </div>

                  {/* Info */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <h3 className="text-sm font-bold text-gray-900 dark:text-white">{gw.name}</h3>
                      {gw.nameEn && (
                        <span className="text-xs text-gray-400">({gw.nameEn})</span>
                      )}
                      {!gw.isActive && (
                        <span className="px-1.5 py-0.5 text-[10px] rounded bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400 font-medium">
                          مخفية
                        </span>
                      )}
                    </div>
                    <div className="flex items-center gap-3 mt-1">
                      <span className="text-xs px-2 py-0.5 rounded-full bg-gray-100 dark:bg-dark-border text-gray-500 dark:text-gray-400">
                        {typeLabel(gw.type)}
                      </span>
                      <span className="text-xs text-gray-400">
                        الترتيب: {gw.sortOrder}
                      </span>
                    </div>
                  </div>

                  {/* Actions */}
                  <div className="flex items-center gap-1 shrink-0">
                    <button
                      onClick={() => handleToggle(gw)}
                      className={`p-2 rounded-lg transition-all ${
                        gw.isActive
                          ? 'text-green-500 hover:bg-green-50 dark:hover:bg-green-900/20'
                          : 'text-gray-400 hover:bg-gray-100 dark:hover:bg-dark-border'
                      }`}
                      title={gw.isActive ? 'إخفاء' : 'تفعيل'}
                    >
                      {gw.isActive ? <EyeIcon className="w-4 h-4" /> : <EyeSlashIcon className="w-4 h-4" />}
                    </button>
                    <button
                      onClick={() => handleEdit(gw)}
                      className="p-2 rounded-lg text-blue-500 hover:bg-blue-50 dark:hover:bg-blue-900/20 transition-all"
                      title="تعديل"
                    >
                      <PencilSquareIcon className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => handleDelete(gw.id)}
                      disabled={deleting === gw.id}
                      className="p-2 rounded-lg text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition-all disabled:opacity-50"
                      title="حذف"
                    >
                      <TrashIcon className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Add/Edit Modal */}
      <Modal
        open={showForm}
        onClose={resetForm}
        title={editGateway ? 'تعديل بوابة الدفع' : 'إضافة بوابة دفع'}
        icon={<CreditCardIcon className="w-5 h-5" />}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">الاسم (عربي) *</label>
              <input
                type="text"
                value={form.name}
                onChange={e => setForm({ ...form, name: e.target.value })}
                className="w-full px-3 py-2 rounded-lg bg-gray-50 dark:bg-dark-bg border border-gray-200 dark:border-dark-border text-sm text-gray-900 dark:text-white focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none"
                placeholder="بنكك"
                required
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">الاسم (إنجليزي)</label>
              <input
                type="text"
                value={form.nameEn}
                onChange={e => setForm({ ...form, nameEn: e.target.value })}
                className="w-full px-3 py-2 rounded-lg bg-gray-50 dark:bg-dark-bg border border-gray-200 dark:border-dark-border text-sm text-gray-900 dark:text-white focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none"
                placeholder="Bankak"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">النوع</label>
              <select
                value={form.type}
                onChange={e => setForm({ ...form, type: e.target.value })}
                className="w-full px-3 py-2 rounded-lg bg-gray-50 dark:bg-dark-bg border border-gray-200 dark:border-dark-border text-sm text-gray-900 dark:text-white focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none"
              >
                {GATEWAY_TYPES.map(t => (
                  <option key={t.value} value={t.value}>{t.label}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">الترتيب</label>
              <input
                type="number"
                value={form.sortOrder}
                onChange={e => setForm({ ...form, sortOrder: e.target.value })}
                className="w-full px-3 py-2 rounded-lg bg-gray-50 dark:bg-dark-bg border border-gray-200 dark:border-dark-border text-sm text-gray-900 dark:text-white focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">الأيقونة</label>
              <input
                type="text"
                value={form.icon}
                onChange={e => setForm({ ...form, icon: e.target.value })}
                className="w-full px-3 py-2 rounded-lg bg-gray-50 dark:bg-dark-bg border border-gray-200 dark:border-dark-border text-sm text-gray-900 dark:text-white focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none"
                placeholder="AccountBalance"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">اللون</label>
              <div className="flex gap-2">
                <input
                  type="color"
                  value={form.color}
                  onChange={e => setForm({ ...form, color: e.target.value })}
                  className="w-10 h-10 rounded-lg border border-gray-200 dark:border-dark-border cursor-pointer"
                />
                <input
                  type="text"
                  value={form.color}
                  onChange={e => setForm({ ...form, color: e.target.value })}
                  className="flex-1 px-3 py-2 rounded-lg bg-gray-50 dark:bg-dark-bg border border-gray-200 dark:border-dark-border text-sm text-gray-900 dark:text-white focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none font-mono"
                  placeholder="#26A17B"
                />
              </div>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <label className="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                checked={form.isActive}
                onChange={e => setForm({ ...form, isActive: e.target.checked })}
                className="sr-only peer"
              />
              <div className="w-9 h-5 bg-gray-300 dark:bg-dark-border rounded-full peer peer-checked:bg-emerald-500 after:content-[''] after:absolute after:top-0.5 after:start-[2px] after:bg-white after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:after:translate-x-full"></div>
            </label>
            <span className="text-sm text-gray-600 dark:text-gray-300">مفعّلة (تظهر للمستخدمين)</span>
          </div>

          <div className="flex gap-2 pt-2">
            <button
              type="submit"
              className="flex-1 py-2 rounded-lg bg-emerald-500 text-white font-medium text-sm hover:bg-emerald-600 transition-all shadow-lg shadow-emerald-500/20"
            >
              {editGateway ? 'تحديث' : 'إضافة'}
            </button>
            <button
              type="button"
              onClick={resetForm}
              className="px-4 py-2 rounded-lg bg-gray-100 dark:bg-dark-border text-gray-600 dark:text-gray-300 text-sm hover:bg-gray-200 dark:hover:bg-dark-card transition-all"
            >
              إلغاء
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
