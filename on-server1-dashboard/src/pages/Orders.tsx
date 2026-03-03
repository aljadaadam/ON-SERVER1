import { useEffect, useState, Fragment } from 'react';
import toast from 'react-hot-toast';
import { ordersApi } from '../api/client';
import { ArrowPathIcon, ShoppingCartIcon, ChevronDownIcon, XMarkIcon, CheckCircleIcon, XCircleIcon, EyeIcon } from '@heroicons/react/24/outline';
import PageBanner from '../components/PageBanner';

interface OrderItem {
  id: string;
  productName: string | null;
  product: { name: string } | null;
  quantity: number;
  price: number;
  metadata: string | null;
  imei: string | null;
}

interface Order {
  id: string;
  orderNumber: string;
  totalAmount: number;
  status: string;
  notes: string | null;
  responseData: string | null;
  resultCodes: string | null;
  createdAt: string;
  user?: { name: string; email: string };
  items?: OrderItem[];
}

// Modal component
function Modal({ open, onClose, children }: { open: boolean; onClose: () => void; children: React.ReactNode }) {
  if (!open) return null;
  return (
    <div className="fixed inset-0 flex items-center justify-center p-4" style={{ zIndex: 60 }}>
      <div className="fixed inset-0 bg-black/50 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-white dark:bg-dark-card rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto animate-fade-in-up">
        {children}
      </div>
    </div>
  );
}

export default function Orders() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');
  const [expandedOrder, setExpandedOrder] = useState<string | null>(null);
  // Action modal state
  const [actionModal, setActionModal] = useState<{ type: 'REJECTED' | 'COMPLETED' | null; order: Order | null }>({ type: null, order: null });
  const [actionNotes, setActionNotes] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  // Detail modal
  const [detailOrder, setDetailOrder] = useState<Order | null>(null);

  useEffect(() => {
    loadOrders();
  }, [statusFilter]);

  const loadOrders = async () => {
    setLoading(true);
    try {
      const params: any = { limit: 100 };
      if (statusFilter) params.status = statusFilter;
      const response = await ordersApi.getAll(params);
      setOrders(response.data.data.orders || []);
    } catch (error) {
      toast.error('فشل تحميل الطلبات');
    } finally {
      setLoading(false);
    }
  };

  const handleAction = async () => {
    if (!actionModal.order || !actionModal.type) return;
    setActionLoading(true);
    try {
      await ordersApi.updateStatus(actionModal.order.id, actionModal.type, actionNotes || undefined);
      toast.success(actionModal.type === 'REJECTED' ? 'تم رفض الطلب وإرجاع المبلغ' : 'تم إكمال الطلب');
      setActionModal({ type: null, order: null });
      setActionNotes('');
      loadOrders();
    } catch (error) {
      toast.error('فشل التحديث');
    } finally {
      setActionLoading(false);
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

  const parseMetadata = (metadata: string | null): Record<string, any> | null => {
    if (!metadata) return null;
    try { return JSON.parse(metadata); } catch { return null; }
  };

  const parseResponseData = (data: string | null): Record<string, any> | null => {
    if (!data) return null;
    try { return JSON.parse(data); } catch { return null; }
  };

  const getFieldsFromItem = (item: OrderItem): { label: string; value: string }[] => {
    const meta = parseMetadata(item.metadata);
    if (!meta) return [];
    const fields: { label: string; value: string }[] = [];
    if (meta.imei) fields.push({ label: 'IMEI', value: meta.imei });
    if (meta.fieldValues && typeof meta.fieldValues === 'object') {
      for (const [key, val] of Object.entries(meta.fieldValues)) {
        // Skip imei if already added
        if (key.toLowerCase() === 'imei' && meta.imei) continue;
        fields.push({ label: key, value: String(val) });
      }
    }
    return fields;
  };

  const getOrderSummaryFields = (order: Order): string => {
    if (!order.items || order.items.length === 0) return '-';
    const allFields: string[] = [];
    order.items.forEach(item => {
      const fields = getFieldsFromItem(item);
      fields.forEach(f => allFields.push(`${f.value}`));
    });
    return allFields.length > 0 ? allFields.join(', ') : '-';
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
      <div className="flex flex-col sm:flex-row sm:items-center justify-between mb-6 gap-3">
        <h1 className="page-title">الطلبات ({orders.length})</h1>
        <div className="flex items-center gap-2">
          <button onClick={loadOrders} className="p-2 rounded-xl text-gray-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-dark-card transition-all duration-200">
            <ArrowPathIcon className={`w-5 h-5 ${loading ? 'animate-spin' : ''}`} />
          </button>
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
                  <th className="table-header w-8"></th>
                  <th className="table-header">رقم الطلب</th>
                  <th className="table-header">المستخدم</th>
                  <th className="table-header">المنتج</th>
                  <th className="table-header">البيانات المقدمة</th>
                  <th className="table-header">المبلغ</th>
                  <th className="table-header">الحالة</th>
                  <th className="table-header">التاريخ</th>
                  <th className="table-header">إجراءات</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((order, i) => {
                  const isExpanded = expandedOrder === order.id;
                  const summaryFields = getOrderSummaryFields(order);
                  const prodName = order.items?.[0]?.product?.name || order.items?.[0]?.productName || '-';
                  const responseInfo = parseResponseData(order.responseData);
                  return (
                    <Fragment key={order.id}>
                      <tr className="table-row animate-fade-in-up border-b border-gray-50 dark:border-dark-border/50" style={{ animationDelay: `${i * 20}ms` }}>
                        {/* Expand toggle */}
                        <td className="table-cell px-2">
                          <button
                            onClick={() => setExpandedOrder(isExpanded ? null : order.id)}
                            className="p-1 rounded-lg hover:bg-gray-100 dark:hover:bg-dark-surface transition"
                          >
                            <ChevronDownIcon className={`w-4 h-4 text-gray-400 transition-transform duration-200 ${isExpanded ? 'rotate-180' : ''}`} />
                          </button>
                        </td>
                        <td className="table-cell font-mono text-xs font-bold text-gray-900 dark:text-white">{order.orderNumber}</td>
                        <td className="table-cell">
                          <div className="text-gray-900 dark:text-white text-xs font-medium">{order.user?.name || '-'}</div>
                          <div className="text-gray-400 text-[10px]">{order.user?.email || ''}</div>
                        </td>
                        <td className="table-cell text-xs text-gray-600 dark:text-gray-300 max-w-[150px] truncate" title={prodName}>{prodName}</td>
                        <td className="table-cell text-xs text-gray-600 dark:text-gray-300 max-w-[180px]">
                          <span className="truncate block" title={summaryFields}>{summaryFields}</span>
                        </td>
                        <td className="table-cell font-semibold text-gray-900 dark:text-white text-xs">${order.totalAmount}</td>
                        <td className="table-cell">
                          <span className={`badge text-[10px] px-2 py-0.5 ${statusColors[order.status] || ''}`}>
                            {statusOptions.find(s => s.value === order.status)?.label || order.status}
                          </span>
                        </td>
                        <td className="table-cell text-gray-400 text-[10px]">{new Date(order.createdAt).toLocaleDateString('ar')}</td>
                        <td className="table-cell">
                          <div className="flex items-center gap-1">
                            {/* Detail btn */}
                            <button
                              onClick={() => setDetailOrder(order)}
                              className="p-1.5 rounded-lg text-gray-400 hover:text-blue-500 hover:bg-blue-50 dark:hover:bg-blue-500/10 transition"
                              title="تفاصيل"
                            >
                              <EyeIcon className="w-4 h-4" />
                            </button>
                            {/* Complete btn */}
                            {order.status !== 'COMPLETED' && order.status !== 'REJECTED' && (
                              <button
                                onClick={() => { setActionModal({ type: 'COMPLETED', order }); setActionNotes(''); }}
                                className="p-1.5 rounded-lg text-gray-400 hover:text-emerald-500 hover:bg-emerald-50 dark:hover:bg-emerald-500/10 transition"
                                title="إكمال"
                              >
                                <CheckCircleIcon className="w-4 h-4" />
                              </button>
                            )}
                            {/* Reject btn */}
                            {order.status !== 'REJECTED' && order.status !== 'COMPLETED' && (
                              <button
                                onClick={() => { setActionModal({ type: 'REJECTED', order }); setActionNotes(''); }}
                                className="p-1.5 rounded-lg text-gray-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10 transition"
                                title="رفض"
                              >
                                <XCircleIcon className="w-4 h-4" />
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                      {/* Expanded detail row */}
                      {isExpanded && (
                        <tr className="animate-fade-in-up">
                          <td colSpan={9} className="px-4 py-3 bg-gray-50/50 dark:bg-dark-surface/50">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                              {/* Items & Fields */}
                              <div>
                                <h4 className="text-xs font-bold text-gray-900 dark:text-white mb-2">عناصر الطلب والحقول المقدمة</h4>
                                {order.items?.map((item, idx) => {
                                  const fields = getFieldsFromItem(item);
                                  return (
                                    <div key={idx} className="mb-2 p-2 bg-white dark:bg-dark-card rounded-lg border border-gray-100 dark:border-dark-border">
                                      <div className="text-xs font-medium text-gray-800 dark:text-gray-200 mb-1">
                                        {item.product?.name || item.productName || 'منتج'}
                                        <span className="text-gray-400 mr-2">× {item.quantity}</span>
                                        <span className="text-gray-400 mr-2">${item.price}</span>
                                      </div>
                                      {item.imei && (
                                        <div className="text-[10px] text-gray-500">
                                          <span className="font-medium">IMEI:</span> <span className="font-mono">{item.imei}</span>
                                        </div>
                                      )}
                                      {fields.length > 0 && (
                                        <div className="mt-1 flex flex-wrap gap-1">
                                          {fields.map((f, fi) => (
                                            <span key={fi} className="inline-flex items-center text-[10px] bg-blue-50 dark:bg-blue-500/10 text-blue-600 dark:text-blue-400 px-2 py-0.5 rounded">
                                              <span className="font-medium ml-1">{f.label}:</span> {f.value}
                                            </span>
                                          ))}
                                        </div>
                                      )}
                                    </div>
                                  );
                                })}
                              </div>
                              {/* Notes & Response */}
                              <div className="space-y-2">
                                {order.notes && (
                                  <div className="p-2 bg-white dark:bg-dark-card rounded-lg border border-gray-100 dark:border-dark-border">
                                    <h4 className="text-[10px] font-bold text-gray-500 mb-1">ملاحظات</h4>
                                    <p className="text-xs text-gray-700 dark:text-gray-300">{order.notes}</p>
                                  </div>
                                )}
                                {responseInfo && (
                                  <div className="p-2 bg-white dark:bg-dark-card rounded-lg border border-gray-100 dark:border-dark-border">
                                    <h4 className="text-[10px] font-bold text-gray-500 mb-1">رد المزود</h4>
                                    {responseInfo.error ? (
                                      <p className="text-xs text-red-500">{responseInfo.error}</p>
                                    ) : (
                                      <pre className="text-[10px] text-gray-600 dark:text-gray-400 whitespace-pre-wrap font-mono">{JSON.stringify(responseInfo, null, 2)}</pre>
                                    )}
                                  </div>
                                )}
                                {order.resultCodes && (
                                  <div className="p-2 bg-white dark:bg-dark-card rounded-lg border border-emerald-200 dark:border-emerald-500/30">
                                    <h4 className="text-[10px] font-bold text-emerald-600 mb-1">نتيجة الطلب</h4>
                                    <pre className="text-[10px] text-gray-600 dark:text-gray-400 whitespace-pre-wrap font-mono">{order.resultCodes}</pre>
                                  </div>
                                )}
                              </div>
                            </div>
                          </td>
                        </tr>
                      )}
                    </Fragment>
                  );
                })}
                {orders.length === 0 && (
                  <tr>
                    <td colSpan={9} className="py-16 text-center">
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

      {/* Action Modal (Reject / Complete) */}
      <Modal open={!!actionModal.type} onClose={() => setActionModal({ type: null, order: null })}>
        <div className="p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-bold text-gray-900 dark:text-white">
              {actionModal.type === 'REJECTED' ? '🚫 رفض الطلب' : '✅ إكمال الطلب'}
            </h3>
            <button onClick={() => setActionModal({ type: null, order: null })} className="p-1 rounded-lg hover:bg-gray-100 dark:hover:bg-dark-surface">
              <XMarkIcon className="w-5 h-5 text-gray-400" />
            </button>
          </div>
          {actionModal.order && (
            <div className="mb-4 p-3 bg-gray-50 dark:bg-dark-surface rounded-xl">
              <div className="flex items-center justify-between text-sm">
                <span className="text-gray-500">طلب رقم</span>
                <span className="font-mono font-bold text-gray-900 dark:text-white">{actionModal.order.orderNumber}</span>
              </div>
              <div className="flex items-center justify-between text-sm mt-1">
                <span className="text-gray-500">المستخدم</span>
                <span className="text-gray-700 dark:text-gray-300">{actionModal.order.user?.name || '-'}</span>
              </div>
              <div className="flex items-center justify-between text-sm mt-1">
                <span className="text-gray-500">المبلغ</span>
                <span className="font-bold text-gray-900 dark:text-white">${actionModal.order.totalAmount}</span>
              </div>
            </div>
          )}
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              {actionModal.type === 'REJECTED' ? 'سبب الرفض' : 'ملاحظة الإكمال'}
              <span className="text-gray-400 text-xs mr-1">(اختياري — سيظهر للمستخدم)</span>
            </label>
            <textarea
              value={actionNotes}
              onChange={(e) => setActionNotes(e.target.value)}
              placeholder={actionModal.type === 'REJECTED' ? 'اكتب سبب الرفض هنا...' : 'اكتب ملاحظة للمستخدم...'}
              rows={3}
              className="w-full px-3 py-2 rounded-xl border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-surface text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-primary-500/30 focus:border-primary-500 outline-none resize-none transition"
            />
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={handleAction}
              disabled={actionLoading}
              className={`flex-1 py-2.5 rounded-xl text-white text-sm font-medium transition-all duration-200 ${
                actionModal.type === 'REJECTED'
                  ? 'bg-red-500 hover:bg-red-600'
                  : 'bg-emerald-500 hover:bg-emerald-600'
              } disabled:opacity-50`}
            >
              {actionLoading ? 'جاري المعالجة...' : actionModal.type === 'REJECTED' ? 'تأكيد الرفض وإرجاع المبلغ' : 'تأكيد الإكمال'}
            </button>
            <button
              onClick={() => setActionModal({ type: null, order: null })}
              className="px-4 py-2.5 rounded-xl text-gray-500 text-sm font-medium hover:bg-gray-100 dark:hover:bg-dark-surface transition"
            >
              إلغاء
            </button>
          </div>
        </div>
      </Modal>

      {/* Detail Modal */}
      <Modal open={!!detailOrder} onClose={() => setDetailOrder(null)}>
        {detailOrder && (
          <div className="p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-bold text-gray-900 dark:text-white">تفاصيل الطلب #{detailOrder.orderNumber}</h3>
              <button onClick={() => setDetailOrder(null)} className="p-1 rounded-lg hover:bg-gray-100 dark:hover:bg-dark-surface">
                <XMarkIcon className="w-5 h-5 text-gray-400" />
              </button>
            </div>
            {/* Info */}
            <div className="grid grid-cols-2 gap-3 mb-4">
              <div className="p-3 bg-gray-50 dark:bg-dark-surface rounded-xl">
                <div className="text-[10px] text-gray-400 mb-1">المستخدم</div>
                <div className="text-sm font-medium text-gray-900 dark:text-white">{detailOrder.user?.name || '-'}</div>
                <div className="text-[10px] text-gray-400">{detailOrder.user?.email || ''}</div>
              </div>
              <div className="p-3 bg-gray-50 dark:bg-dark-surface rounded-xl">
                <div className="text-[10px] text-gray-400 mb-1">المبلغ</div>
                <div className="text-sm font-bold text-gray-900 dark:text-white">${detailOrder.totalAmount}</div>
              </div>
              <div className="p-3 bg-gray-50 dark:bg-dark-surface rounded-xl">
                <div className="text-[10px] text-gray-400 mb-1">الحالة</div>
                <span className={`badge text-[10px] ${statusColors[detailOrder.status] || ''}`}>
                  {statusOptions.find(s => s.value === detailOrder.status)?.label || detailOrder.status}
                </span>
              </div>
              <div className="p-3 bg-gray-50 dark:bg-dark-surface rounded-xl">
                <div className="text-[10px] text-gray-400 mb-1">التاريخ</div>
                <div className="text-sm text-gray-700 dark:text-gray-300">{new Date(detailOrder.createdAt).toLocaleString('ar')}</div>
              </div>
            </div>
            {/* Items */}
            <div className="mb-4">
              <h4 className="text-xs font-bold text-gray-900 dark:text-white mb-2">عناصر الطلب</h4>
              {detailOrder.items?.map((item, idx) => {
                const fields = getFieldsFromItem(item);
                return (
                  <div key={idx} className="mb-2 p-3 bg-gray-50 dark:bg-dark-surface rounded-xl">
                    <div className="flex items-center justify-between mb-1">
                      <span className="text-xs font-medium text-gray-800 dark:text-gray-200">{item.product?.name || item.productName || 'منتج'}</span>
                      <span className="text-xs text-gray-500">× {item.quantity} — ${item.price}</span>
                    </div>
                    {item.imei && (
                      <div className="text-[10px] text-gray-500 mb-1">
                        <span className="font-medium">IMEI:</span> <span className="font-mono select-all">{item.imei}</span>
                      </div>
                    )}
                    {fields.length > 0 && (
                      <div className="flex flex-wrap gap-1 mt-1">
                        {fields.map((f, fi) => (
                          <span key={fi} className="inline-flex items-center text-[10px] bg-blue-50 dark:bg-blue-500/10 text-blue-600 dark:text-blue-400 px-2 py-0.5 rounded">
                            <span className="font-medium ml-1">{f.label}:</span> <span className="select-all">{f.value}</span>
                          </span>
                        ))}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
            {/* Notes */}
            {detailOrder.notes && (
              <div className="mb-4 p-3 bg-amber-50 dark:bg-amber-500/10 rounded-xl border border-amber-200 dark:border-amber-500/30">
                <h4 className="text-[10px] font-bold text-amber-600 mb-1">ملاحظات</h4>
                <p className="text-xs text-gray-700 dark:text-gray-300">{detailOrder.notes}</p>
              </div>
            )}
            {/* Response */}
            {detailOrder.responseData && (() => {
              const resp = parseResponseData(detailOrder.responseData);
              return resp ? (
                <div className="mb-4 p-3 bg-gray-50 dark:bg-dark-surface rounded-xl">
                  <h4 className="text-[10px] font-bold text-gray-500 mb-1">رد المزود</h4>
                  {resp.error ? (
                    <p className="text-xs text-red-500">{resp.error}</p>
                  ) : (
                    <pre className="text-[10px] text-gray-600 dark:text-gray-400 whitespace-pre-wrap font-mono">{JSON.stringify(resp, null, 2)}</pre>
                  )}
                </div>
              ) : null;
            })()}
            {/* Result */}
            {detailOrder.resultCodes && (
              <div className="p-3 bg-emerald-50 dark:bg-emerald-500/10 rounded-xl border border-emerald-200 dark:border-emerald-500/30">
                <h4 className="text-[10px] font-bold text-emerald-600 mb-1">نتيجة الطلب</h4>
                <pre className="text-[10px] text-gray-600 dark:text-gray-400 whitespace-pre-wrap font-mono select-all">{detailOrder.resultCodes}</pre>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
}
