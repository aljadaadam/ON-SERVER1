import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { adminApi } from '../api/client';
import { ArrowPathIcon, BanknotesIcon, MagnifyingGlassIcon, ShieldCheckIcon, UserIcon, UsersIcon } from '@heroicons/react/24/outline';
import PageBanner from '../components/PageBanner';

interface User {
  id: string;
  email: string;
  phone: string | null;
  name: string;
  balance: number;
  role: string;
  isVerified: boolean;
  isActive: boolean;
  createdAt: string;
  _count: { orders: number };
}

export default function Users() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [balanceModal, setBalanceModal] = useState<{ userId: string; name: string } | null>(null);
  const [balanceAmount, setBalanceAmount] = useState('');
  const [searchEmail, setSearchEmail] = useState('');

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    setLoading(true);
    try {
      const response = await adminApi.getUsers();
      setUsers(response.data.data.users || []);
    } catch (error) {
      toast.error('فشل تحميل المستخدمين');
    } finally {
      setLoading(false);
    }
  };

  const toggleStatus = async (userId: string, isActive: boolean) => {
    try {
      await adminApi.toggleUserStatus(userId, !isActive);
      toast.success(isActive ? 'تم تعطيل الحساب' : 'تم تفعيل الحساب');
      loadUsers();
    } catch (error) {
      toast.error('فشلت العملية');
    }
  };

  const addBalance = async () => {
    if (!balanceModal || !balanceAmount) return;
    try {
      await adminApi.updateUserBalance(balanceModal.userId, parseFloat(balanceAmount), 'Admin deposit');
      toast.success('تم إضافة الرصيد');
      setBalanceModal(null);
      setBalanceAmount('');
      loadUsers();
    } catch (error) {
      toast.error('فشلت العملية');
    }
  };

  return (
    <div>
      <PageBanner
        title="إدارة المستخدمين"
        subtitle="عرض وإدارة حسابات المستخدمين والأرصدة"
        icon={UsersIcon}
        gradient="from-violet-600 via-purple-600 to-fuchsia-500"
        pattern="circles"
      />
      <div className="flex flex-col sm:flex-row sm:items-center justify-between mb-6 gap-3">
        <h1 className="page-title">المستخدمين</h1>
        <div className="flex items-center gap-2">
          <div className="relative">
            <MagnifyingGlassIcon className="w-4 h-4 text-gray-400 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
            <input
              type="text"
              value={searchEmail}
              onChange={(e) => setSearchEmail(e.target.value)}
              placeholder="بحث بالبريد الإلكتروني..."
              className="pr-9 pl-3 py-2 w-56 rounded-xl border border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-surface text-sm text-gray-900 dark:text-white placeholder-gray-400 focus:ring-2 focus:ring-primary-500/30 focus:border-primary-500 outline-none transition"
            />
          </div>
          <button onClick={loadUsers} className="p-2 rounded-xl text-gray-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-dark-card transition-all duration-200">
            <ArrowPathIcon className={`w-5 h-5 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {/* Balance Modal */}
      {balanceModal && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-[60] animate-fade-in-up" style={{ animationDuration: '0.2s' }}>
          <div className="card max-w-sm w-full mx-4 animate-scale-in">
            <div className="flex items-center gap-3 mb-4">
              <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-amber-400 to-amber-500 flex items-center justify-center">
                <BanknotesIcon className="w-5 h-5 text-white" />
              </div>
              <h2 className="text-lg font-bold text-gray-900 dark:text-white">
                إضافة رصيد لـ {balanceModal.name}
              </h2>
            </div>
            <input
              type="number"
              step="0.01"
              value={balanceAmount}
              onChange={(e) => setBalanceAmount(e.target.value)}
              placeholder="المبلغ"
              className="input-field mb-4"
            />
            <div className="flex gap-3">
              <button onClick={addBalance} className="btn-primary flex-1">إضافة</button>
              <button onClick={() => setBalanceModal(null)} className="flex-1 py-2.5 px-4 rounded-xl border border-gray-200 dark:border-dark-border text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-dark-surface transition-all duration-200">
                إلغاء
              </button>
            </div>
          </div>
        </div>
      )}

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
                  <th className="table-header">الاسم</th>
                  <th className="table-header">البريد</th>
                  <th className="table-header">الرصيد</th>
                  <th className="table-header">الطلبات</th>
                  <th className="table-header">الحالة</th>
                  <th className="table-header">إجراءات</th>
                </tr>
              </thead>
              <tbody>
                {users.filter(u => !searchEmail || u.email.toLowerCase().includes(searchEmail.toLowerCase())).map((user, i) => (
                  <tr key={user.id} className="table-row animate-fade-in-up" style={{ animationDelay: `${i * 30}ms` }}>
                    <td className="table-cell">
                      <div className="flex items-center gap-3">
                        <div className={`w-8 h-8 rounded-lg flex items-center justify-center ${
                          user.role === 'ADMIN'
                            ? 'bg-gradient-to-br from-amber-400 to-amber-500'
                            : 'bg-gradient-to-br from-gray-400 to-gray-500'
                        }`}>
                          {user.role === 'ADMIN'
                            ? <ShieldCheckIcon className="w-4 h-4 text-white" />
                            : <UserIcon className="w-4 h-4 text-white" />
                          }
                        </div>
                        <div>
                          <p className="font-medium text-gray-900 dark:text-white">{user.name}</p>
                          <p className="text-xs text-gray-400">{user.role === 'ADMIN' ? 'مدير' : 'مستخدم'}</p>
                        </div>
                      </div>
                    </td>
                    <td className="table-cell text-gray-600 dark:text-gray-300">{user.email}</td>
                    <td className="table-cell font-semibold text-gray-900 dark:text-white">${user.balance.toFixed(2)}</td>
                    <td className="table-cell text-gray-500 dark:text-gray-400">{user._count.orders}</td>
                    <td className="table-cell">
                      <span className={`badge ${
                        user.isActive
                          ? 'bg-emerald-50 text-emerald-600 dark:bg-emerald-500/10 dark:text-emerald-400'
                          : 'bg-red-50 text-red-600 dark:bg-red-500/10 dark:text-red-400'
                      }`}>
                        {user.isActive ? 'نشط' : 'معطل'}
                      </span>
                    </td>
                    <td className="table-cell">
                      <div className="flex gap-2">
                        <button
                          onClick={() => setBalanceModal({ userId: user.id, name: user.name })}
                          className="inline-flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs font-medium text-amber-600 dark:text-amber-400 bg-amber-50 dark:bg-amber-500/10 hover:bg-amber-100 dark:hover:bg-amber-500/20 transition-all duration-200"
                        >
                          <BanknotesIcon className="w-3.5 h-3.5" />
                          رصيد
                        </button>
                        <button
                          onClick={() => toggleStatus(user.id, user.isActive)}
                          className={`px-2.5 py-1.5 rounded-lg text-xs font-medium transition-all duration-200 ${
                            user.isActive
                              ? 'text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-500/10 hover:bg-red-100 dark:hover:bg-red-500/20'
                              : 'text-emerald-600 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-500/10 hover:bg-emerald-100 dark:hover:bg-emerald-500/20'
                          }`}
                        >
                          {user.isActive ? 'تعطيل' : 'تفعيل'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
                {users.length === 0 && (
                  <tr>
                    <td colSpan={6} className="py-16 text-center">
                      <UsersIcon className="w-12 h-12 text-gray-300 dark:text-gray-600 mx-auto mb-3" />
                      <p className="text-gray-400">لا يوجد مستخدمين</p>
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
