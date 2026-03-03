import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { ordersApi } from '../api/client';
import { ArrowPathIcon, ShoppingCartIcon } from '@heroicons/react/24/outline';
import PageBanner from '../components/PageBanner';

interface Order {
  id: string;
  orderNumber: string;
  totalAmount: number;
  status: string;
  createdAt: string;
  user?: { name: string; email: string };
  items?: { product: { name: string } | null; productName: string | null; quantity: number; price: number }[];
}

export default function Orders() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');

  useEffect(() => {
    loadOrders();
  }, [statusFilter]);

  const loadOrders = async () => {
    setLoading(true);
    try {
      const params: any = { limit: 50 };
      if (statusFilter) params.status = statusFilter;
      const response = await ordersApi.getAll(params);
      setOrders(response.data.data.orders || []);
    } catch (error) {
      toast.error('فشل تحميل الطلبات');
    } finally {
      setLoading(false);
    }
  };

  const updateStatus = async (orderId: string, status: string) => {
    try {
      await ordersApi.updateStatus(orderId, status);
      toast.success('تم تحديث حالة الطلب');
      loadOrders();
    } catch (error) {
      toast.error('فشل التحديث');
    }
  };

  const statusOptions = [
    { value: '', label: 'الكل' },
    { value: 'PENDING', label: 'في الانتظار' },
    { value: 'WAITING', label: 'في الطابور' },
    { value: 'PROCESSING', label: 'قيد المعالجة' },
    { value: 'COMPLETED', label: 'مكتمل' },
    { value: 'REJECTED', label: 'مرفوض' },
  ];

  const statusColors: Record<string, string> = {
    PENDING: 'bg-amber-50 text-amber-600 dark:bg-amber-500/10 dark:text-amber-400',
    WAITING: 'bg-orange-50 text-orange-600 dark:bg-orange-500/10 dark:text-orange-400',
    PROCESSING: 'bg-blue-50 text-blue-600 dark:bg-blue-500/10 dark:text-blue-400',
    COMPLETED: 'bg-emerald-50 text-emerald-600 dark:bg-emerald-500/10 dark:text-emerald-400',
    REJECTED: 'bg-red-50 text-red-600 dark:bg-red-500/10 dark:text-red-400',
  };

  return (
    <div>
      <PageBanner
        title="إدارة الطلبات"
        subtitle="تتبع ومعالجة جميع طلبات العملاء"
        icon={ShoppingCartIcon}
        gradient="from-emerald-600 via-teal-600 to-cyan-500"
        pattern="waves"
      />
      <div className="flex items-center justify-between mb-6">
        <h1 className="page-title">الطلبات</h1>
        <div className="flex items-center gap-2">
          <button onClick={loadOrders} className="p-2 rounded-xl text-gray-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-dark-card transition-all duration-200">
            <ArrowPathIcon className={`w-5 h-5 ${loading ? 'animate-spin' : ''}`} />
          </button>
          <div className="flex gap-1 bg-gray-100 dark:bg-dark-card rounded-xl p-1">
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
      </div>

      <div className="card">
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="w-10 h-10 border-[3px] border-gray-200 dark:border-dark-border border-t-primary-500 rounded-full animate-spin"></div>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-100 dark:border-dark-border">
                  <th className="table-header">رقم الطلب</th>
                  <th className="table-header">المستخدم</th>
                  <th className="table-header">المبلغ</th>
                  <th className="table-header">الحالة</th>
                  <th className="table-header">التاريخ</th>
                  <th className="table-header">إجراءات</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((order, i) => (
                  <tr key={order.id} className="table-row animate-fade-in-up" style={{ animationDelay: `${i * 30}ms` }}>
                    <td className="table-cell font-mono text-sm text-gray-900 dark:text-white">{order.orderNumber}</td>
                    <td className="table-cell text-gray-600 dark:text-gray-300">{order.user?.name || '-'}</td>
                    <td className="table-cell font-semibold text-gray-900 dark:text-white">${order.totalAmount}</td>
                    <td className="table-cell">
                      <span className={`badge ${statusColors[order.status] || ''}`}>
                        {statusOptions.find(s => s.value === order.status)?.label || order.status}
                      </span>
                    </td>
                    <td className="table-cell text-gray-400">{new Date(order.createdAt).toLocaleDateString('ar')}</td>
                    <td className="table-cell">
                      <select
                        value={order.status}
                        onChange={(e) => updateStatus(order.id, e.target.value)}
                        className="text-xs border border-gray-200 dark:border-dark-border rounded-lg px-2 py-1.5 bg-gray-50 dark:bg-dark-surface dark:text-white focus:ring-2 focus:ring-primary-500/30 outline-none transition"
                      >
                        {statusOptions.filter(s => s.value).map((opt) => (
                          <option key={opt.value} value={opt.value}>{opt.label}</option>
                        ))}
                      </select>
                    </td>
                  </tr>
                ))}
                {orders.length === 0 && (
                  <tr>
                    <td colSpan={6} className="py-16 text-center">
                      <ShoppingCartIcon className="w-12 h-12 text-gray-300 dark:text-gray-600 mx-auto mb-3" />
                      <p className="text-gray-400">لا توجد طلبات</p>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
