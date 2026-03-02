import { useEffect, useState, useCallback } from 'react';
import { adminApi, ordersApi } from '../api/client';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
} from 'recharts';
import {
  UsersIcon,
  CubeIcon,
  ShoppingCartIcon,
  ClockIcon,
  CurrencyDollarIcon,
  ArrowPathIcon,
  ChartBarSquareIcon,
} from '@heroicons/react/24/outline';
import PageBanner from '../components/PageBanner';

interface Stats {
  totalUsers: number;
  totalProducts: number;
  totalOrders: number;
  pendingOrders: number;
  totalRevenue: number;
  recentOrders: any[];
}

const STATUS_COLORS: Record<string, string> = {
  PENDING: '#EAB308',
  PROCESSING: '#3B82F6',
  COMPLETED: '#22C55E',
  REJECTED: '#EF4444',
};

export default function Dashboard() {
  const [stats, setStats] = useState<Stats | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [ordersByStatus, setOrdersByStatus] = useState<any[]>([]);

  const loadData = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    try {
      const [statsRes, ordersRes] = await Promise.all([
        adminApi.getStats(),
        ordersApi.getAll({ limit: 200 }),
      ]);
      setStats(statsRes.data.data);

      const orders = ordersRes.data.data.orders || [];
      const statusCount: Record<string, number> = {};
      orders.forEach((o: any) => {
        statusCount[o.status] = (statusCount[o.status] || 0) + 1;
      });
      const statusLabels: Record<string, string> = {
        PENDING: 'في الانتظار', PROCESSING: 'قيد المعالجة', COMPLETED: 'مكتمل',
        REJECTED: 'مرفوض',
      };
      setOrdersByStatus(
        Object.entries(statusCount).map(([status, count]) => ({
          name: statusLabels[status] || status,
          value: count,
          status,
        }))
      );
    } catch (error) {
      console.error('Failed to load stats:', error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-10 h-10 border-[3px] border-gray-200 dark:border-dark-border border-t-primary-500 rounded-full animate-spin"></div>
      </div>
    );
  }

  const statCards = [
    { label: 'المستخدمين', value: stats?.totalUsers || 0, icon: UsersIcon, gradient: 'from-blue-500 to-blue-600' },
    { label: 'المنتجات', value: stats?.totalProducts || 0, icon: CubeIcon, gradient: 'from-emerald-500 to-emerald-600' },
    { label: 'الطلبات', value: stats?.totalOrders || 0, icon: ShoppingCartIcon, gradient: 'from-violet-500 to-violet-600' },
    { label: 'طلبات معلقة', value: stats?.pendingOrders || 0, icon: ClockIcon, gradient: 'from-amber-500 to-amber-600' },
    { label: 'إجمالي الإيرادات', value: `$${(stats?.totalRevenue || 0).toFixed(2)}`, icon: CurrencyDollarIcon, gradient: 'from-primary-500 to-primary-700' },
  ];

  const revenueByDay: Record<string, number> = {};
  stats?.recentOrders.forEach((order: any) => {
    const day = new Date(order.createdAt).toLocaleDateString('ar', { month: 'short', day: 'numeric' });
    revenueByDay[day] = (revenueByDay[day] || 0) + order.totalAmount;
  });
  const revenueChartData = Object.entries(revenueByDay).map(([day, amount]) => ({
    name: day, revenue: amount,
  })).reverse();

  return (
    <div>
      <PageBanner
        title="لوحة التحكم"
        subtitle="نظرة عامة على أداء المتجر والإحصائيات"
        icon={ChartBarSquareIcon}
        gradient="from-indigo-600 via-blue-600 to-cyan-500"
        pattern="hexagons"
      />
      <div className="flex items-center justify-between mb-6">
        <h1 className="page-title">لوحة التحكم</h1>
        <button
          onClick={() => loadData(true)}
          disabled={refreshing}
          className="flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium text-gray-500 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-dark-card transition-all duration-200"
        >
          <ArrowPathIcon className={`w-4 h-4 ${refreshing ? 'animate-spin' : ''}`} />
          تحديث
        </button>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4 mb-8">
        {statCards.map((stat, index) => (
          <div key={index} className={`card-hover animate-fade-in-up delay-${index * 75}`}>
            <div className="flex items-center gap-3">
              <div className={`w-11 h-11 rounded-xl bg-gradient-to-br ${stat.gradient} flex items-center justify-center shadow-lg`}>
                <stat.icon className="w-5 h-5 text-white" />
              </div>
              <div>
                <p className="text-xs text-gray-400 dark:text-gray-500 font-medium">{stat.label}</p>
                <p className="text-xl font-bold text-gray-900 dark:text-white">{stat.value}</p>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        <div className="card">
          <h2 className="text-lg font-bold text-gray-900 dark:text-white mb-4">إيرادات آخر الطلبات</h2>
          {revenueChartData.length > 0 ? (
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={revenueChartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                <XAxis dataKey="name" stroke="#9CA3AF" fontSize={12} />
                <YAxis stroke="#9CA3AF" fontSize={12} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#1A1A2E', border: '1px solid #374151', borderRadius: '8px', color: '#fff' }}
                  formatter={(value: number) => [`$${value.toFixed(2)}`, 'الإيرادات']}
                />
                <Bar dataKey="revenue" fill="#FFD700" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-64 text-gray-500">لا توجد بيانات</div>
          )}
        </div>

        <div className="card">
          <h2 className="text-lg font-bold text-gray-900 dark:text-white mb-4">توزيع حالات الطلبات</h2>
          {ordersByStatus.length > 0 ? (
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie
                  data={ordersByStatus}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={100}
                  paddingAngle={3}
                  dataKey="value"
                  label={({ name, value }) => `${name}: ${value}`}
                >
                  {ordersByStatus.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={STATUS_COLORS[entry.status] || '#6B7280'} />
                  ))}
                </Pie>
                <Tooltip contentStyle={{ backgroundColor: '#1A1A2E', border: '1px solid #374151', borderRadius: '8px', color: '#fff' }} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-64 text-gray-500">لا توجد طلبات</div>
          )}
        </div>
      </div>

      {/* Recent Orders */}
      <div className="card animate-fade-in-up">
        <h2 className="text-lg font-bold text-gray-900 dark:text-white mb-4">آخر الطلبات</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-100 dark:border-dark-border">
                <th className="table-header">رقم الطلب</th>
                <th className="table-header">المستخدم</th>
                <th className="table-header">المبلغ</th>
                <th className="table-header">الحالة</th>
                <th className="table-header">التاريخ</th>
              </tr>
            </thead>
            <tbody>
              {stats?.recentOrders.map((order: any) => (
                <tr key={order.id} className="table-row">
                  <td className="table-cell font-mono text-gray-900 dark:text-white">{order.orderNumber}</td>
                  <td className="table-cell text-gray-600 dark:text-gray-300">{order.user?.name}</td>
                  <td className="table-cell font-semibold text-gray-900 dark:text-white">${order.totalAmount}</td>
                  <td className="py-3 px-4">
                    <StatusBadge status={order.status} />
                  </td>
                  <td className="py-3 px-4 text-gray-500">{new Date(order.createdAt).toLocaleDateString('ar')}</td>
                </tr>
              ))}
              {(!stats?.recentOrders || stats.recentOrders.length === 0) && (
                <tr>
                  <td colSpan={5} className="py-8 text-center text-gray-500">لا توجد طلبات بعد</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function StatusBadge({ status }: { status: string }) {
  const styles: Record<string, string> = {
    PENDING: 'bg-yellow-100 text-yellow-700 dark:bg-yellow-500/20 dark:text-yellow-400',
    PROCESSING: 'bg-blue-100 text-blue-700 dark:bg-blue-500/20 dark:text-blue-400',
    COMPLETED: 'bg-green-100 text-green-700 dark:bg-green-500/20 dark:text-green-400',
    REJECTED: 'bg-red-100 text-red-700 dark:bg-red-500/20 dark:text-red-400',
  };

  const labels: Record<string, string> = {
    PENDING: 'في الانتظار',
    PROCESSING: 'قيد المعالجة',
    COMPLETED: 'مكتمل',
    REJECTED: 'مرفوض',
  };

  return (
    <span className={`px-2.5 py-1 rounded-full text-xs font-medium ${styles[status] || styles.PENDING}`}>
      {labels[status] || status}
    </span>
  );
}
