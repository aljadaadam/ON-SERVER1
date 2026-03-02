import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { adminApi } from '../api/client';
import {
  EnvelopeIcon, CheckBadgeIcon, ArrowPathIcon,
  PaperAirplaneIcon, EyeIcon, EyeSlashIcon,
} from '@heroicons/react/24/outline';
import PageBanner from '../components/PageBanner';

interface EmailForm {
  smtp_host: string;
  smtp_port: string;
  smtp_user: string;
  smtp_pass: string;
  smtp_from_email: string;
  smtp_from_name: string;
  smtp_secure: string;
}

const defaultEmail: EmailForm = {
  smtp_host: '',
  smtp_port: '587',
  smtp_user: '',
  smtp_pass: '',
  smtp_from_email: '',
  smtp_from_name: 'ON-SERVER1',
  smtp_secure: 'true',
};

const fieldLabels: Record<string, string> = {
  smtp_host: 'سيرفر SMTP',
  smtp_port: 'المنفذ (Port)',
  smtp_user: 'البريد الإلكتروني',
  smtp_pass: 'كلمة المرور / App Password',
  smtp_from_email: 'البريد المرسل',
  smtp_from_name: 'اسم المرسل',
  smtp_secure: 'تشفير SSL/TLS',
};

const fieldPlaceholders: Record<string, string> = {
  smtp_host: 'smtp.gmail.com',
  smtp_port: '587',
  smtp_user: 'your-email@gmail.com',
  smtp_pass: '••••••••••••••••',
  smtp_from_email: 'noreply@your-domain.com',
  smtp_from_name: 'ON-SERVER1',
  smtp_secure: '',
};

const fieldHelp: Record<string, string> = {
  smtp_host: 'مثال: smtp.gmail.com أو smtp.zoho.com',
  smtp_port: '587 لـ TLS أو 465 لـ SSL',
  smtp_user: 'البريد المستخدم للمصادقة',
  smtp_pass: 'لـ Gmail استخدم App Password من إعدادات الحساب',
  smtp_from_email: 'البريد الذي يظهر للمستلم، اتركه فارغاً لاستخدام smtp_user',
  smtp_from_name: 'الاسم الذي يظهر في صندوق الوارد',
  smtp_secure: '',
};

export default function EmailSettings() {
  const [settings, setSettings] = useState<EmailForm>(defaultEmail);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [testing, setTesting] = useState(false);
  const [testEmail, setTestEmail] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  useEffect(() => { loadSettings(); }, []);

  const loadSettings = async () => {
    try {
      const response = await adminApi.getSettings();
      const data = response.data.data || {};
      const emailData: Partial<EmailForm> = {};
      for (const key of Object.keys(defaultEmail)) {
        if (data[key]) emailData[key as keyof EmailForm] = data[key];
      }
      setSettings({ ...defaultEmail, ...emailData });
    } catch {
      toast.error('فشل تحميل الإعدادات');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    if (!settings.smtp_host || !settings.smtp_user || !settings.smtp_pass) {
      toast.error('الرجاء ملء الحقول المطلوبة: السيرفر، البريد، كلمة المرور');
      return;
    }
    setSaving(true);
    try {
      await adminApi.updateSettings(settings as unknown as Record<string, string>);
      toast.success('تم حفظ إعدادات البريد');
    } catch {
      toast.error('فشل حفظ الإعدادات');
    } finally {
      setSaving(false);
    }
  };

  const handleTestEmail = async () => {
    if (!testEmail) {
      toast.error('أدخل بريد إلكتروني للاختبار');
      return;
    }
    if (!settings.smtp_host || !settings.smtp_user || !settings.smtp_pass) {
      toast.error('الرجاء حفظ إعدادات SMTP أولاً');
      return;
    }
    setTesting(true);
    try {
      await adminApi.testEmail(testEmail);
      toast.success('تم إرسال بريد الاختبار بنجاح!');
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'فشل إرسال بريد الاختبار');
    } finally {
      setTesting(false);
    }
  };

  const updateSetting = (key: string, value: string) => {
    setSettings(prev => ({ ...prev, [key]: value }));
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-8 h-8 border-2 border-gray-200 dark:border-dark-border border-t-primary-500 rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div>
      <PageBanner
        title="البريد الإلكتروني"
        subtitle="إعدادات SMTP لإرسال الإشعارات والتأكيدات"
        icon={EnvelopeIcon}
        gradient="from-blue-600 via-indigo-500 to-purple-500"
        pattern="dots"
      />

      <div className="flex items-center justify-between mb-4">
        <h1 className="text-base font-bold text-gray-900 dark:text-white">إعدادات البريد</h1>
        <div className="flex items-center gap-1.5">
          <button onClick={loadSettings} className="p-1.5 rounded-lg text-gray-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-dark-card transition-all">
            <ArrowPathIcon className="w-4 h-4" />
          </button>
          <button
            onClick={handleSave}
            disabled={saving}
            className="text-xs px-3 py-1.5 rounded-lg bg-primary-500 hover:bg-primary-600 text-white font-medium disabled:opacity-50 inline-flex items-center gap-1"
          >
            {saving ? <><ArrowPathIcon className="w-3.5 h-3.5 animate-spin" /> حفظ...</> : <><CheckBadgeIcon className="w-3.5 h-3.5" /> حفظ</>}
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-3">
        {/* SMTP Connection */}
        <div className="card !p-4 animate-fade-in-up lg:col-span-2">
          <h2 className="text-xs font-bold text-gray-900 dark:text-white mb-3 flex items-center gap-2">
            <div className="w-6 h-6 rounded-lg bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center">
              <EnvelopeIcon className="w-3.5 h-3.5 text-white" />
            </div>
            اتصال SMTP
          </h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {/* Host */}
            <div>
              <label className="block text-[11px] font-medium text-gray-500 dark:text-gray-400 mb-0.5">
                {fieldLabels.smtp_host} <span className="text-red-400">*</span>
              </label>
              <input
                type="text"
                value={settings.smtp_host}
                onChange={e => updateSetting('smtp_host', e.target.value)}
                className="w-full text-xs px-2.5 py-1.5 rounded-lg border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-surface text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary-500"
                placeholder={fieldPlaceholders.smtp_host}
                dir="ltr"
              />
              <p className="text-[10px] text-gray-400 mt-0.5">{fieldHelp.smtp_host}</p>
            </div>

            {/* Port */}
            <div>
              <label className="block text-[11px] font-medium text-gray-500 dark:text-gray-400 mb-0.5">
                {fieldLabels.smtp_port}
              </label>
              <input
                type="number"
                value={settings.smtp_port}
                onChange={e => updateSetting('smtp_port', e.target.value)}
                className="w-full text-xs px-2.5 py-1.5 rounded-lg border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-surface text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary-500"
                placeholder={fieldPlaceholders.smtp_port}
                dir="ltr"
              />
              <p className="text-[10px] text-gray-400 mt-0.5">{fieldHelp.smtp_port}</p>
            </div>

            {/* User */}
            <div>
              <label className="block text-[11px] font-medium text-gray-500 dark:text-gray-400 mb-0.5">
                {fieldLabels.smtp_user} <span className="text-red-400">*</span>
              </label>
              <input
                type="email"
                value={settings.smtp_user}
                onChange={e => updateSetting('smtp_user', e.target.value)}
                className="w-full text-xs px-2.5 py-1.5 rounded-lg border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-surface text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary-500"
                placeholder={fieldPlaceholders.smtp_user}
                dir="ltr"
              />
              <p className="text-[10px] text-gray-400 mt-0.5">{fieldHelp.smtp_user}</p>
            </div>

            {/* Password */}
            <div>
              <label className="block text-[11px] font-medium text-gray-500 dark:text-gray-400 mb-0.5">
                {fieldLabels.smtp_pass} <span className="text-red-400">*</span>
              </label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={settings.smtp_pass}
                  onChange={e => updateSetting('smtp_pass', e.target.value)}
                  className="w-full text-xs px-2.5 py-1.5 pr-8 rounded-lg border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-surface text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary-500"
                  placeholder={fieldPlaceholders.smtp_pass}
                  dir="ltr"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute left-2 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                >
                  {showPassword ? <EyeSlashIcon className="w-3.5 h-3.5" /> : <EyeIcon className="w-3.5 h-3.5" />}
                </button>
              </div>
              <p className="text-[10px] text-gray-400 mt-0.5">{fieldHelp.smtp_pass}</p>
            </div>

            {/* From Email */}
            <div>
              <label className="block text-[11px] font-medium text-gray-500 dark:text-gray-400 mb-0.5">
                {fieldLabels.smtp_from_email}
              </label>
              <input
                type="email"
                value={settings.smtp_from_email}
                onChange={e => updateSetting('smtp_from_email', e.target.value)}
                className="w-full text-xs px-2.5 py-1.5 rounded-lg border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-surface text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary-500"
                placeholder={fieldPlaceholders.smtp_from_email}
                dir="ltr"
              />
              <p className="text-[10px] text-gray-400 mt-0.5">{fieldHelp.smtp_from_email}</p>
            </div>

            {/* From Name */}
            <div>
              <label className="block text-[11px] font-medium text-gray-500 dark:text-gray-400 mb-0.5">
                {fieldLabels.smtp_from_name}
              </label>
              <input
                type="text"
                value={settings.smtp_from_name}
                onChange={e => updateSetting('smtp_from_name', e.target.value)}
                className="w-full text-xs px-2.5 py-1.5 rounded-lg border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-surface text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary-500"
                placeholder={fieldPlaceholders.smtp_from_name}
              />
              <p className="text-[10px] text-gray-400 mt-0.5">{fieldHelp.smtp_from_name}</p>
            </div>

            {/* Secure */}
            <div className="flex items-center gap-3 sm:col-span-2">
              <label className="flex items-center gap-2 cursor-pointer">
                <div className={`relative w-10 h-5 rounded-full transition-colors ${settings.smtp_secure === 'true' ? 'bg-green-500' : 'bg-gray-300 dark:bg-dark-border'}`}>
                  <div className={`absolute top-0.5 w-4 h-4 bg-white rounded-full shadow transition-transform ${settings.smtp_secure === 'true' ? 'translate-x-5' : 'translate-x-0.5'}`} />
                </div>
                <input
                  type="checkbox"
                  className="hidden"
                  checked={settings.smtp_secure === 'true'}
                  onChange={e => updateSetting('smtp_secure', e.target.checked ? 'true' : 'false')}
                />
                <span className="text-xs font-medium text-gray-600 dark:text-gray-300">
                  تشفير SSL/TLS {settings.smtp_secure === 'true' ? '(مفعّل)' : '(معطّل)'}
                </span>
              </label>
            </div>
          </div>
        </div>

        {/* Test Email */}
        <div className="card !p-4 animate-fade-in-up lg:col-span-2" style={{ animationDelay: '60ms' }}>
          <h2 className="text-xs font-bold text-gray-900 dark:text-white mb-3 flex items-center gap-2">
            <div className="w-6 h-6 rounded-lg bg-gradient-to-br from-green-500 to-emerald-600 flex items-center justify-center">
              <PaperAirplaneIcon className="w-3.5 h-3.5 text-white" />
            </div>
            إرسال بريد تجريبي
          </h2>
          <div className="flex gap-2">
            <input
              type="email"
              value={testEmail}
              onChange={e => setTestEmail(e.target.value)}
              className="flex-1 text-xs px-2.5 py-1.5 rounded-lg border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-surface text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary-500"
              placeholder="أدخل بريد إلكتروني للاختبار"
              dir="ltr"
            />
            <button
              onClick={handleTestEmail}
              disabled={testing}
              className="text-xs px-4 py-1.5 rounded-lg bg-green-500 hover:bg-green-600 text-white font-medium disabled:opacity-50 inline-flex items-center gap-1 whitespace-nowrap"
            >
              {testing ? <><ArrowPathIcon className="w-3.5 h-3.5 animate-spin" /> إرسال...</> : <><PaperAirplaneIcon className="w-3.5 h-3.5" /> إرسال اختبار</>}
            </button>
          </div>
          <p className="text-[10px] text-gray-400 mt-1.5">سيتم إرسال رسالة تجريبية للتحقق من صحة الإعدادات</p>
        </div>
      </div>
    </div>
  );
}
