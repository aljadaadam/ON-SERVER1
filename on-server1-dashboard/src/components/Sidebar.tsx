import { NavLink, useNavigate, useLocation } from 'react-router-dom';
import { useEffect } from 'react';
import {
  ChartBarIcon,
  CubeIcon,
  FolderIcon,
  ShoppingCartIcon,
  BanknotesIcon,
  UsersIcon,
  PhotoIcon,
  Cog6ToothIcon,
  ArrowRightStartOnRectangleIcon,
  ServerStackIcon,
  EnvelopeIcon,
  XMarkIcon,
} from '@heroicons/react/24/outline';
import {
  ChartBarIcon as ChartBarSolid,
  CubeIcon as CubeSolid,
  FolderIcon as FolderSolid,
  ShoppingCartIcon as ShoppingCartSolid,
  BanknotesIcon as BanknotesSolid,
  UsersIcon as UsersSolid,
  PhotoIcon as PhotoSolid,
  Cog6ToothIcon as Cog6ToothSolid,
  EnvelopeIcon as EnvelopeSolid,
} from '@heroicons/react/24/solid';

const menuItems = [
  { path: '/', label: 'لوحة التحكم', icon: ChartBarIcon, activeIcon: ChartBarSolid },
  { path: '/products', label: 'المنتجات', icon: CubeIcon, activeIcon: CubeSolid },
  { path: '/categories', label: 'التصنيفات', icon: FolderIcon, activeIcon: FolderSolid },
  { path: '/orders', label: 'الطلبات', icon: ShoppingCartIcon, activeIcon: ShoppingCartSolid },
  { path: '/deposits', label: 'الإيداعات', icon: BanknotesIcon, activeIcon: BanknotesSolid },
  { path: '/users', label: 'المستخدمين', icon: UsersIcon, activeIcon: UsersSolid },
  { path: '/banners', label: 'البانرات', icon: PhotoIcon, activeIcon: PhotoSolid },
  { path: '/email', label: 'البريد', icon: EnvelopeIcon, activeIcon: EnvelopeSolid },
  { path: '/settings', label: 'الإعدادات', icon: Cog6ToothIcon, activeIcon: Cog6ToothSolid },
];

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function Sidebar({ isOpen, onClose }: SidebarProps) {
  const navigate = useNavigate();
  const location = useLocation();

  // Close sidebar on route change (mobile)
  useEffect(() => {
    onClose();
  }, [location.pathname]);

  const handleLogout = () => {
    localStorage.removeItem('admin_token');
    navigate('/login');
  };

  return (
    <>
      {/* Mobile overlay */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-black/50 backdrop-blur-sm z-40 lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <aside className={`
        fixed top-0 right-0 z-50 h-full w-64 bg-gray-50 dark:bg-dark-bg border-l border-gray-200 dark:border-dark-border flex flex-col transition-transform duration-300 ease-in-out
        lg:static lg:translate-x-0 lg:z-auto
        ${isOpen ? 'translate-x-0' : 'translate-x-full'}
      `}>
        {/* Logo */}
        <div className="p-5 border-b border-gray-200 dark:border-dark-border flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center shadow-lg shadow-primary-500/20">
              <ServerStackIcon className="w-5 h-5 text-black" />
            </div>
            <div>
              <h1 className="text-lg font-bold text-gray-900 dark:text-white tracking-tight">ON-SERVER1</h1>
              <p className="text-[11px] text-gray-400 dark:text-gray-500 -mt-0.5">لوحة تحكم الإدارة</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="lg:hidden p-1.5 rounded-lg text-gray-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-200 dark:hover:bg-dark-card transition-all"
          >
            <XMarkIcon className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation */}
        <nav className="flex-1 p-3 space-y-0.5 overflow-y-auto">
          {menuItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              end={item.path === '/'}
              className={({ isActive }) =>
                `group flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all duration-200 ${
                  isActive
                    ? 'bg-primary-500/10 text-primary-600 dark:text-primary-400 shadow-sm'
                    : 'text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-dark-card hover:text-gray-900 dark:hover:text-white'
                }`
              }
            >
              {({ isActive }) => (
                <>
                  {isActive ? (
                    <item.activeIcon className="w-5 h-5 flex-shrink-0" />
                  ) : (
                    <item.icon className="w-5 h-5 flex-shrink-0 group-hover:scale-110 transition-transform duration-200" />
                  )}
                  <span className="text-sm font-medium">{item.label}</span>
                  {isActive && (
                    <div className="mr-auto w-1.5 h-1.5 rounded-full bg-primary-500" />
                  )}
                </>
              )}
            </NavLink>
          ))}
        </nav>

        {/* Logout */}
        <div className="p-3 border-t border-gray-200 dark:border-dark-border">
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-gray-400 hover:bg-red-50 dark:hover:bg-red-500/10 hover:text-red-500 transition-all duration-200"
          >
            <ArrowRightStartOnRectangleIcon className="w-5 h-5" />
            <span className="text-sm font-medium">تسجيل الخروج</span>
          </button>
        </div>
      </aside>
    </>
  );
}
