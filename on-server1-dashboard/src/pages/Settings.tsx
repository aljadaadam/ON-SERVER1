import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { adminApi } from '../api/client';
import {
  GlobeAltIcon, PhoneIcon, CurrencyDollarIcon,
  Cog6ToothIcon, DocumentTextIcon, BanknotesIcon,
  CheckBadgeIcon, ArrowPathIcon, ServerStackIcon
} from '@heroicons/react/24/outline';
import PageBanner from '../components/PageBanner';

interface SettingsForm {
  site_name: string;
  site_description: string;
  support_email: string;
  support_phone: string;
  whatsapp_number: string;
  telegram_link: string;
  min_deposit: string;
  max_deposit: string;
  currency: string;
  maintenance_mode: string;
  announcement: string;
  terms_url: string;
  privacy_url: string;
  privacy_policy_text: string;
  terms_of_service_text: string;
  // Gateway settings
  usdt_wallet_address: string;
  usdt_network: string;
  usdt_min_amount: string;
  usdt_max_amount: string;
  bankak_account_name: string;
  bankak_account_number: string;
  bankak_bank_name: string;
  bankak_exchange_rate: string;
  bankak_min_amount: string;
  bankak_max_amount: string;
  bankak_transfer_note: string;
  // Provider settings
  provider_url: string;
  provider_username: string;
  provider_api_key: string;
}

const defaultSettings: SettingsForm = {
  site_name: 'ON-SERVER1',
  site_description: 'بيع بطاقات الألعاب والخدمات الرقمية',
  support_email: '',
  support_phone: '',
  whatsapp_number: '',
  telegram_link: '',
  min_deposit: '1',
  max_deposit: '1000',
  currency: 'USD',
  maintenance_mode: 'false',
  announcement: '',
  terms_url: '',
  privacy_url: '',
  privacy_policy_text: '',
  terms_of_service_text: '',
  // Gateway defaults
  usdt_wallet_address: '',
  usdt_network: 'BEP20 (BSC)',
  usdt_min_amount: '5',
  usdt_max_amount: '5000',
  bankak_account_name: '',
  bankak_account_number: '',
  bankak_bank_name: 'بنكك',
  bankak_exchange_rate: '600',
  bankak_min_amount: '1',
  bankak_max_amount: '1000',
  bankak_transfer_note: 'شحن رصيد محفظة',
  // Provider defaults
  provider_url: '',
  provider_username: '',
  provider_api_key: '',
};

const groupIcons: Record<string, React.ComponentType<React.SVGProps<SVGSVGElement>>> = {
  'معلومات الموقع': GlobeAltIcon,
  'معلومات التواصل': PhoneIcon,
  'بوابة USDT': CurrencyDollarIcon,
  'بوابة بنكك': BanknotesIcon,
  'إعدادات مالية': CurrencyDollarIcon,
  'السياسات والخصوصية': DocumentTextIcon,
  'إعدادات عامة': Cog6ToothIcon,
  'مزود الخدمات': ServerStackIcon,
};

const groupGradients: Record<string, string> = {
  'معلومات الموقع': 'from-blue-500 to-blue-600',
  'معلومات التواصل': 'from-emerald-500 to-emerald-600',
  'بوابة USDT': 'from-green-500 to-green-600',
  'بوابة بنكك': 'from-purple-500 to-purple-600',
  'إعدادات مالية': 'from-amber-500 to-amber-600',
  'السياسات والخصوصية': 'from-gray-500 to-gray-600',
  'إعدادات عامة': 'from-indigo-500 to-indigo-600',
  'مزود الخدمات': 'from-rose-500 to-rose-600',
};

const settingGroups = [
  {
    title: 'معلومات الموقع',
    keys: ['site_name', 'site_description', 'currency'],
  },
  {
    title: 'معلومات التواصل',
    keys: ['support_email', 'support_phone', 'whatsapp_number', 'telegram_link'],
  },
  {
    title: 'بوابة USDT',
    keys: ['usdt_wallet_address', 'usdt_network', 'usdt_min_amount', 'usdt_max_amount'],
  },
  {
    title: 'بوابة بنكك',
    keys: ['bankak_account_name', 'bankak_account_number', 'bankak_bank_name', 'bankak_exchange_rate', 'bankak_min_amount', 'bankak_max_amount', 'bankak_transfer_note'],
  },
  {
    title: 'إعدادات مالية',
    keys: ['min_deposit', 'max_deposit'],
  },
  {
    title: 'السياسات والخصوصية',
    keys: ['privacy_policy_text', 'terms_of_service_text'],
  },
  {
    title: 'إعدادات عامة',
    keys: ['maintenance_mode', 'announcement', 'terms_url', 'privacy_url'],
  },
  {
    title: 'مزود الخدمات',
    keys: ['provider_url', 'provider_username', 'provider_api_key'],
  },
];

const settingLabels: Record<string, string> = {
  site_name: 'اسم الموقع',
  site_description: 'وصف الموقع',
  support_email: 'بريد الدعم',
  support_phone: 'هاتف الدعم',
  whatsapp_number: 'رقم الواتساب',
  telegram_link: 'رابط التليجرام',
  min_deposit: 'الحد الأدنى للإيداع',
  max_deposit: 'الحد الأقصى للإيداع',
  currency: 'العملة',
  maintenance_mode: 'وضع الصيانة',
  announcement: 'إعلان عام',
  terms_url: 'رابط الشروط والأحكام',
  privacy_url: 'رابط سياسة الخصوصية',
  privacy_policy_text: 'نص سياسة الخصوصية',
  terms_of_service_text: 'نص الشروط والأحكام',
  // Gateway labels
  usdt_wallet_address: 'عنوان محفظة USDT',
  usdt_network: 'الشبكة',
  usdt_min_amount: 'الحد الأدنى (USDT)',
  usdt_max_amount: 'الحد الأقصى (USDT)',
  bankak_account_name: 'اسم صاحب الحساب',
  bankak_account_number: 'رقم الحساب',
  bankak_bank_name: 'اسم البنك',
  bankak_exchange_rate: 'سعر الصرف (1 USD = ? SDG)',
  bankak_min_amount: 'الحد الأدنى (USD)',
  bankak_max_amount: 'الحد الأقصى (USD)',
  bankak_transfer_note: 'تعليق التحويل',
  // Provider labels
  provider_url: 'رابط API المزود',
  provider_username: 'اسم المستخدم',
  provider_api_key: 'مفتاح API',
};

export default function Settings() {
  const [settings, setSettings] = useState<SettingsForm>(defaultSettings);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => { loadSettings(); }, []);

  const loadSettings = async () => {
    try {
      const response = await adminApi.getSettings();
      const data = response.data.data || {};
      setSettings({ ...defaultSettings, ...data });
    } catch (error) {
      toast.error('فشل تحميل الإعدادات');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      await adminApi.updateSettings(settings as unknown as Record<string, string>);
      toast.success('تم حفظ الإعدادات بنجاح');
    } catch (error) {
      toast.error('فشل حفظ الإعدادات');
    } finally {
      setSaving(false);
    }
  };

  const updateSetting = (key: string, value: string) => {
    setSettings(prev => ({ ...prev, [key]: value }));
  };

  const renderInput = (key: string) => {
    const value = settings[key as keyof SettingsForm] || '';

    if (key === 'maintenance_mode') {
      return (
        <label className="flex items-center gap-3 cursor-pointer">
          <div className={`relative w-12 h-6 rounded-full transition-colors ${value === 'true' ? 'bg-red-500' : 'bg-gray-300 dark:bg-dark-border'}`}>
            <div className={`absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform ${value === 'true' ? 'translate-x-6' : 'translate-x-0.5'}`}></div>
          </div>
          <input type="checkbox" className="hidden" checked={value === 'true'} onChange={e => updateSetting(key, e.target.checked ? 'true' : 'false')} />
          <span className={`text-sm font-medium ${value === 'true' ? 'text-red-500' : 'text-gray-500'}`}>
            {value === 'true' ? 'مفعّل - الموقع تحت الصيانة' : 'معطّل'}
          </span>
        </label>
      );
    }

    if (key === 'site_description' || key === 'announcement' || key === 'privacy_policy_text' || key === 'terms_of_service_text') {
      return (
        <textarea
          value={value}
          onChange={e => updateSetting(key, e.target.value)}
          className="input-field"
          rows={key === 'privacy_policy_text' || key === 'terms_of_service_text' ? 10 : 3}
          placeholder={settingLabels[key]}
          dir={key === 'privacy_policy_text' || key === 'terms_of_service_text' ? 'auto' : undefined}
        />
      );
    }

    // Exchange rate gets special styling
    if (key === 'bankak_exchange_rate') {
      return (
        <div className="flex items-center gap-3">
          <span className="text-sm text-gray-500 dark:text-gray-400 whitespace-nowrap">1 USD =</span>
          <input
            type="number"
            value={value}
            onChange={e => updateSetting(key, e.target.value)}
            className="input-field flex-1"
            placeholder="600"
          />
          <span className="text-sm text-gray-500 dark:text-gray-400 whitespace-nowrap">SDG</span>
        </div>
      );
    }

    return (
      <input
        type={key.includes('deposit') || key.includes('min_amount') || key.includes('max_amount') ? 'number' : key.includes('email') ? 'email' : 'text'}
        value={value}
        onChange={e => updateSetting(key, e.target.value)}
        className="input-field"
        placeholder={settingLabels[key]}
        dir={key === 'usdt_wallet_address' || key === 'provider_url' || key === 'provider_api_key' ? 'ltr' : undefined}
      />
    );
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-10 h-10 border-[3px] border-gray-200 dark:border-dark-border border-t-primary-500 rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <div>
      <PageBanner
        title="الإعدادات"
        subtitle="تخصيص إعدادات المتجر وبوابات الدفع"
        icon={Cog6ToothIcon}
        gradient="from-gray-700 via-slate-600 to-zinc-500"
        pattern="dots"
      />
      <div className="flex items-center justify-between mb-6">
        <h1 className="page-title">الإعدادات</h1>
        <div className="flex items-center gap-2">
          <button onClick={loadSettings} className="p-2 rounded-xl text-gray-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-dark-card transition-all duration-200">
            <ArrowPathIcon className="w-5 h-5" />
          </button>
          <button
            onClick={handleSave}
            disabled={saving}
            className="btn-primary disabled:opacity-50 inline-flex items-center gap-1.5"
          >
            {saving ? <><ArrowPathIcon className="w-4 h-4 animate-spin" /> جاري الحفظ...</> : <><CheckBadgeIcon className="w-4 h-4" /> حفظ الإعدادات</>}
          </button>
        </div>
      </div>

      <div className="space-y-6">
        {settingGroups.map((group, i) => {
          const Icon = groupIcons[group.title] || Cog6ToothIcon;
          const gradient = groupGradients[group.title] || 'from-gray-500 to-gray-600';
          return (
          <div key={group.title} className="card animate-fade-in-up" style={{ animationDelay: `${i * 50}ms` }}>
            <h2 className="text-lg font-bold text-gray-900 dark:text-white mb-4 flex items-center gap-3">
              <div className={`w-9 h-9 rounded-xl bg-gradient-to-br ${gradient} flex items-center justify-center`}>
                <Icon className="w-5 h-5 text-white" />
              </div>
              {group.title}
            </h2>
            <div className="space-y-4">
              {group.keys.map((key) => (
                <div key={key}>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                    {settingLabels[key]}
                  </label>
                  {renderInput(key)}
                </div>
              ))}
            </div>
          </div>
          );
        })}
      </div>
    </div>
  );
}
