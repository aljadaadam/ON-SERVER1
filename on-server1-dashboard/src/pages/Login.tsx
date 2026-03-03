import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/client';
import { ServerStackIcon, ArrowPathIcon, EyeIcon, EyeSlashIcon, ExclamationTriangleIcon, XMarkIcon } from '@heroicons/react/24/outline';

export default function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const response = await authApi.login(email, password);
      const { data } = response.data;
      
      if (data.user.role !== 'ADMIN') {
        setError('ليس لديك صلاحية الوصول لهذه اللوحة');
        return;
      }

      localStorage.setItem('admin_token', data.accessToken);
      navigate('/');
    } catch (error: any) {
      const msg = error.response?.data?.message;
      if (msg === 'Invalid email or password') {
        setError('البريد الإلكتروني أو كلمة المرور غير صحيحة');
      } else if (msg) {
        setError(msg);
      } else if (!navigator.onLine) {
        setError('لا يوجد اتصال بالإنترنت');
      } else {
        setError('حدث خطأ في الاتصال بالخادم، حاول مرة أخرى');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 px-4">
      {/* Background decoration */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-80 h-80 rounded-full bg-primary-500/5 blur-3xl" />
        <div className="absolute -bottom-40 -left-40 w-80 h-80 rounded-full bg-primary-400/5 blur-3xl" />
      </div>

      <div className="w-full max-w-md animate-fade-in-up relative z-10">
        <div className="bg-gray-800/60 backdrop-blur-2xl border border-gray-700/40 rounded-3xl p-8 sm:p-10 shadow-2xl shadow-black/20">
          {/* Logo & Header */}
          <div className="text-center mb-8">
            <div className="w-18 h-18 rounded-2xl bg-gradient-to-br from-primary-400 to-primary-600 flex items-center justify-center mx-auto mb-5 shadow-lg shadow-primary-500/25 w-[4.5rem] h-[4.5rem]">
              <ServerStackIcon className="w-9 h-9 text-white" />
            </div>
            <h1 className="text-3xl font-bold bg-gradient-to-r from-primary-400 to-primary-500 bg-clip-text text-transparent">ON-SERVER1</h1>
            <p className="text-gray-400 mt-2 text-sm">لوحة تحكم الإدارة</p>
          </div>

          {/* Error message card */}
          {error && (
            <div className="mb-5 bg-red-500/10 border border-red-500/30 rounded-xl p-4 flex items-start gap-3 animate-fade-in-up">
              <ExclamationTriangleIcon className="w-5 h-5 text-red-400 shrink-0 mt-0.5" />
              <p className="text-red-300 text-sm flex-1">{error}</p>
              <button onClick={() => setError('')} className="text-red-400/60 hover:text-red-300 transition-colors shrink-0">
                <XMarkIcon className="w-4 h-4" />
              </button>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Email */}
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1.5">
                البريد الإلكتروني
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => { setEmail(e.target.value); setError(''); }}
                className="w-full px-4 py-3 rounded-xl bg-gray-700/50 border border-gray-600/50 text-white placeholder-gray-500 focus:ring-2 focus:ring-primary-500/50 focus:border-primary-500/50 outline-none transition-all duration-200"
                placeholder="admin@onserver1.com"
                required
                autoComplete="email"
                dir="ltr"
              />
            </div>

            {/* Password with toggle */}
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1.5">
                كلمة المرور
              </label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => { setPassword(e.target.value); setError(''); }}
                  className="w-full px-4 py-3 pe-12 rounded-xl bg-gray-700/50 border border-gray-600/50 text-white placeholder-gray-500 focus:ring-2 focus:ring-primary-500/50 focus:border-primary-500/50 outline-none transition-all duration-200"
                  placeholder="••••••••"
                  required
                  autoComplete="current-password"
                  dir="ltr"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-200 transition-colors p-1"
                  tabIndex={-1}
                >
                  {showPassword ? <EyeSlashIcon className="w-5 h-5" /> : <EyeIcon className="w-5 h-5" />}
                </button>
              </div>
            </div>

            {/* Submit */}
            <button
              type="submit"
              disabled={loading}
              className="w-full py-3.5 rounded-xl bg-gradient-to-r from-primary-500 to-primary-600 text-black font-bold text-lg hover:shadow-lg hover:shadow-primary-500/25 active:scale-[0.98] transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed inline-flex items-center justify-center gap-2 mt-2"
            >
              {loading ? <><ArrowPathIcon className="w-5 h-5 animate-spin" /> جاري الدخول...</> : 'تسجيل الدخول'}
            </button>
          </form>

          {/* Footer */}
          <p className="text-center text-gray-600 text-xs mt-6">© 2026 ON-SERVER1</p>
        </div>
      </div>
    </div>
  );
}
