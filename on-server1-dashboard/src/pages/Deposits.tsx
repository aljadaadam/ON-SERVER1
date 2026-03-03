import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { depositsApi } from '../api/client';
import {
  ArrowPathIcon, ChartBarIcon, ClockIcon, CheckCircleIcon,
  XCircleIcon, CurrencyDollarIcon, PhotoIcon, BanknotesIcon
} from '@heroicons/react/24/outline';
import PageBanner from '../components/PageBanner';

interface Deposit {
  id: string;
  depositNumber: number;
  amount: number;
  amountLocal: number | null;
  exchangeRate: number | null;
  gateway: string;
  status: string;
  txHash: string | null;
  receiptImage: string | null;
  adminNote: string | null;
  createdAt: string;
  user?: { name: string; email: string };
}

interface DepositStats {
  total: number;
  pending: number;
  confirmed: number;
  rejected: number;
  totalAmount: number;
}

const API_BASE = import.meta.env.VITE_API_URL || '/api';

export default function Deposits() {
  const [deposits, setDeposits] = useState<Deposit[]>([]);
  const [stats, setStats] = useState<DepositStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');
  const [selectedDeposit, setSelectedDeposit] = useState<Deposit | null>(null);
  const [adminNote, setAdminNote] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [modalAction, setModalAction] = useState<'approve' | 'reject'>('approve');
  const [showImageModal, setShowImageModal] = useState(false);
  const [imageUrl, setImageUrl] = useState('');

  useEffect(() => {
    loadDeposits();
    loadStats();
  }, [statusFilter]);

  const loadDeposits = async () => {
    try {
      const params: any = { limit: 50 };
      if (statusFilter) params.status = statusFilter;
      const response = await depositsApi.getAll(params);
      setDeposits(response.data.data.deposits || []);
    } catch (error) {
      toast.error('فشل تحميل الإيداعات');
    } finally {
      setLoading(false);
    }
  };

  const loadStats = async () => {
    try {
      const response = await depositsApi.getStats();
      setStats(response.data.data);
    } catch (error) {
      console.error('Failed to load stats');
    }
  };

  const openActionModal = (deposit: Deposit, action: 'approve' | 'reject') => {
    setSelectedDeposit(deposit);
    setModalAction(action);
    setAdminNote('');
    setShowModal(true);
  };

  const handleAction = async () => {
    if (!selectedDeposit) return;
    try {
      if (modalAction === 'approve') {
        await depositsApi.approve(selectedDeposit.id, adminNote || undefined);
        toast.success('تم قبول الإيداع بنجاح');
      } else {
        await depositsApi.reject(selectedDeposit.id, adminNote || undefined);
        toast.success('تم رفض الإيداع');
      }
      setShowModal(false);
      loadDeposits();
      loadStats();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'فشلت العملية');
    }
  };

  const openReceiptImage = (imagePath: string) => {
    const baseUrl = API_BASE.replace('/api', '');
    setImageUrl(`${baseUrl}${imagePath}`);
    setShowImageModal(true);
  };

  const statusColors: Record<string, string> = {
    PENDING: 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400',
    CONFIRMED: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400',
    REJECTED: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
    EXPIRED: 'bg-gray-100 text-gray-700 dark:bg-gray-900/30 dark:text-gray-400',
  };

  const statusLabels: Record<string, string> = {
    PENDING: 'معلق',
    CONFIRMED: 'مؤكد',
    REJECTED: 'مرفوض',
    EXPIRED: 'منتهي',
  };

  const statusOptions = [
    { value: '', label: 'الكل' },
    { value: 'PENDING', label: 'معلق' },
    { value: 'CONFIRMED', label: 'مؤكد' },
    { value: 'REJECTED', label: 'مرفوض' },
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-10 h-10 border-[3px] border-gray-200 dark:border-dark-border border-t-primary-500 rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <PageBanner
        title="إدارة الإيداعات"
        subtitle="مراجعة وإدارة عمليات الإيداع والتحويلات المالية"
        icon={BanknotesIcon}
        gradient="from-green-600 via-emerald-600 to-teal-500"
        pattern="diamonds"
      />
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <h1 className="page-title">إدارة الإيداعات</h1>
        <button onClick={() => { loadDeposits(); loadStats(); }} className="p-2 rounded-xl text-gray-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-dark-card transition-all duration-200 self-start sm:self-auto">
          <ArrowPathIcon className="w-5 h-5" />
        </button>
      </div>

      {/* Stats Cards */}
      {stats && (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3 sm:gap-4">
          <StatCard label="إجمالي الإيداعات" value={stats.total} icon={ChartBarIcon} color="blue" />
          <StatCard label="معلق" value={stats.pending} icon={ClockIcon} color="yellow" />
          <StatCard label="مؤكد" value={stats.confirmed} icon={CheckCircleIcon} color="green" />
          <StatCard label="مرفوض" value={stats.rejected} icon={XCircleIcon} color="red" />
          <StatCard
            label="إجمالي المبلغ"
            value={`$${stats.totalAmount.toFixed(2)}`}
            icon={CurrencyDollarIcon}
            color="purple"
          />
        </div>
      )}

      {/* Filter */}
      <div className="flex flex-col sm:flex-row sm:items-center gap-3 sm:gap-4">
        <label className="text-sm font-medium text-gray-500 dark:text-gray-400">تصفية:</label>
        <div className="flex flex-wrap gap-1 bg-gray-100 dark:bg-dark-card rounded-xl p-1">
          {statusOptions.map((opt) => (
            <button
              key={opt.value}
              onClick={() => setStatusFilter(opt.value)}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all duration-200 ${
                statusFilter === opt.value
                  ? 'bg-white dark:bg-dark-surface text-gray-900 dark:text-white shadow-sm'
                  : 'text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
              }`}
            >
              {opt.label}
            </button>
          ))}
        </div>
      </div>

      {/* Table */}
      <div className="card">
        <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100 dark:border-dark-border">
              <th className="table-header">رقم الإيداع</th>
              <th className="table-header">المستخدم</th>
              <th className="table-header">المبلغ</th>
              <th className="table-header">البوابة</th>
              <th className="table-header">الحالة</th>
              <th className="table-header">التاريخ</th>
              <th className="table-header">التفاصيل</th>
              <th className="table-header">الإجراءات</th>
            </tr>
          </thead>
          <tbody>
            {deposits.map((deposit) => (
              <tr key={deposit.id} className="table-row animate-fade-in-up">
                <td className="table-cell font-mono font-bold text-primary-500">
                  #{deposit.depositNumber}
                </td>
                <td className="table-cell">
                  <div className="text-sm font-medium text-gray-800 dark:text-white">
                    {deposit.user?.name || '-'}
                  </div>
                  <div className="text-xs text-gray-500">{deposit.user?.email || '-'}</div>
                </td>
                <td className="table-cell">
                  <div className="text-sm font-bold text-gray-800 dark:text-white">
                    ${deposit.amount.toFixed(2)}
                  </div>
                  {deposit.amountLocal && (
                    <div className="text-xs text-gray-500">
                      {deposit.amountLocal.toLocaleString()} SDG
                    </div>
                  )}
                </td>
                <td className="table-cell">
                  <span
                    className={`badge ${
                      deposit.gateway === 'USDT'
                        ? 'bg-emerald-50 text-emerald-600 dark:bg-emerald-500/10 dark:text-emerald-400'
                        : 'bg-blue-50 text-blue-600 dark:bg-blue-500/10 dark:text-blue-400'
                    }`}
                  >
                    {deposit.gateway === 'USDT' ? 'USDT' : 'بنكك'}
                  </span>
                </td>
                <td className="table-cell">
                  <span className={`badge ${statusColors[deposit.status] || ''}`}>
                    {statusLabels[deposit.status] || deposit.status}
                  </span>
                </td>
                <td className="table-cell text-gray-400">
                  {new Date(deposit.createdAt).toLocaleDateString('ar-SA', {
                    year: 'numeric',
                    month: 'short',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                  })}
                </td>
                <td className="table-cell">
                  {deposit.gateway === 'USDT' && deposit.txHash && (
                    <a
                      href={`https://bscscan.com/tx/${deposit.txHash}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-xs text-primary-500 hover:underline font-mono"
                      title={deposit.txHash}
                    >
                      {deposit.txHash.slice(0, 10)}...
                    </a>
                  )}
                  {deposit.gateway === 'BANKAK' && deposit.receiptImage && (
                    <button
                      onClick={() => openReceiptImage(deposit.receiptImage!)}
                      className="text-xs text-primary-500 hover:underline inline-flex items-center gap-1"
                    >
                      <PhotoIcon className="w-3.5 h-3.5" /> عرض الإيصال
                    </button>
                  )}
                  {deposit.adminNote && (
                    <div className="text-xs text-gray-400 mt-1" title={deposit.adminNote}>
                      ملاحظة: {deposit.adminNote.slice(0, 20)}...
                    </div>
                  )}
                </td>
                <td className="table-cell">
                  {deposit.status === 'PENDING' ? (
                    <div className="flex gap-2">
                      <button
                        onClick={() => openActionModal(deposit, 'approve')}
                        className="inline-flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs font-medium text-emerald-600 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-500/10 hover:bg-emerald-100 dark:hover:bg-emerald-500/20 transition-all duration-200"
                      >
                        <CheckCircleIcon className="w-3.5 h-3.5" /> قبول
                      </button>
                      <button
                        onClick={() => openActionModal(deposit, 'reject')}
                        className="inline-flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs font-medium text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-500/10 hover:bg-red-100 dark:hover:bg-red-500/20 transition-all duration-200"
                      >
                        <XCircleIcon className="w-3.5 h-3.5" /> رفض
                      </button>
                    </div>
                  ) : (
                    <span className="text-xs text-gray-400">—</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        </div>

        {deposits.length === 0 && (
          <div className="text-center py-16">
            <BanknotesIcon className="w-12 h-12 text-gray-300 dark:text-gray-600 mx-auto mb-3" />
            <p className="text-gray-400">لا توجد إيداعات</p>
          </div>
        )}
      </div>

      {/* Action Modal */}
      {showModal && selectedDeposit && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 animate-fade-in-up" style={{ zIndex: 60, animationDuration: '0.2s' }}>
          <div className="bg-white dark:bg-dark-surface rounded-2xl p-6 w-full max-w-md shadow-2xl animate-scale-in">
            <h3 className="text-lg font-bold text-gray-800 dark:text-white mb-4 flex items-center gap-2">
              {modalAction === 'approve'
                ? <><CheckCircleIcon className="w-5 h-5 text-emerald-500" /> قبول الإيداع</>
                : <><XCircleIcon className="w-5 h-5 text-red-500" /> رفض الإيداع</>}
            </h3>

            <div className="bg-gray-50 dark:bg-dark-card rounded-lg p-4 mb-4 space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">رقم الإيداع:</span>
                <span className="font-mono font-bold text-primary-500">#{selectedDeposit.depositNumber}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">المبلغ:</span>
                <span className="font-bold text-gray-800 dark:text-white">${selectedDeposit.amount.toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">البوابة:</span>
                <span className="font-medium">{selectedDeposit.gateway}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">المستخدم:</span>
                <span className="font-medium">{selectedDeposit.user?.name}</span>
              </div>
            </div>

            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-600 dark:text-gray-300 mb-2">
                ملاحظة (اختياري)
              </label>
              <textarea
                value={adminNote}
                onChange={(e) => setAdminNote(e.target.value)}
                className="w-full px-4 py-3 rounded-lg border border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-card text-gray-800 dark:text-white focus:ring-2 focus:ring-primary-500 outline-none resize-none"
                rows={3}
                placeholder="أضف ملاحظة..."
              />
            </div>

            <div className="flex gap-3">
              <button
                onClick={handleAction}
                className={`flex-1 py-2.5 rounded-lg text-white font-medium transition-colors ${
                  modalAction === 'approve'
                    ? 'bg-green-500 hover:bg-green-600'
                    : 'bg-red-500 hover:bg-red-600'
                }`}
              >
                {modalAction === 'approve' ? 'تأكيد القبول' : 'تأكيد الرفض'}
              </button>
              <button
                onClick={() => setShowModal(false)}
                className="flex-1 py-2.5 rounded-lg bg-gray-100 dark:bg-dark-card text-gray-600 dark:text-gray-300 font-medium hover:bg-gray-200 dark:hover:bg-dark-border transition-colors"
              >
                إلغاء
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Receipt Image Modal */}
      {showImageModal && (
        <div
          className="fixed inset-0 bg-black/70 flex items-center justify-center p-4"
          style={{ zIndex: 60 }}
          onClick={() => setShowImageModal(false)}
        >
          <div className="max-w-2xl max-h-[80vh] relative" onClick={(e) => e.stopPropagation()}>
            <button
              onClick={() => setShowImageModal(false)}
              className="absolute -top-3 -right-3 bg-white dark:bg-dark-surface w-8 h-8 rounded-full flex items-center justify-center shadow-lg text-gray-600 hover:text-gray-800"
            >
              ✕
            </button>
            <img
              src={imageUrl}
              alt="إيصال التحويل"
              className="max-w-full max-h-[75vh] rounded-xl shadow-2xl object-contain"
            />
          </div>
        </div>
      )}
    </div>
  );
}

function StatCard({
  label,
  value,
  icon: Icon,
  color,
}: {
  label: string;
  value: number | string;
  icon: React.ComponentType<React.SVGProps<SVGSVGElement>>;
  color: string;
}) {
  const gradients: Record<string, string> = {
    blue: 'from-blue-500 to-blue-600',
    yellow: 'from-amber-400 to-amber-500',
    green: 'from-emerald-500 to-emerald-600',
    red: 'from-red-500 to-red-600',
    purple: 'from-purple-500 to-purple-600',
  };

  const textColors: Record<string, string> = {
    blue: 'text-blue-600 dark:text-blue-400',
    yellow: 'text-amber-600 dark:text-amber-400',
    green: 'text-emerald-600 dark:text-emerald-400',
    red: 'text-red-600 dark:text-red-400',
    purple: 'text-purple-600 dark:text-purple-400',
  };

  return (
    <div className="card card-hover animate-fade-in-up">
      <div className="flex items-center gap-3 mb-3">
        <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${gradients[color]} flex items-center justify-center`}>
          <Icon className="w-5 h-5 text-white" />
        </div>
        <span className="text-sm text-gray-500 dark:text-gray-400">{label}</span>
      </div>
      <div className={`text-2xl font-bold ${textColors[color]}`}>{value}</div>
    </div>
  );
}
