import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { useState } from 'react';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Products from './pages/Products';
import Orders from './pages/Orders';
import Users from './pages/Users';
import Banners from './pages/Banners';
import Categories from './pages/Categories';
import Settings from './pages/Settings';
import Deposits from './pages/Deposits';
import EmailSettings from './pages/EmailSettings';
import Sidebar from './components/Sidebar';
import { Bars3Icon } from '@heroicons/react/24/outline';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const token = localStorage.getItem('admin_token');
  if (!token) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

function PageTransition({ children }: { children: React.ReactNode }) {
  const location = useLocation();
  return (
    <div key={location.pathname} className="animate-page-in">
      {children}
    </div>
  );
}

function DashboardLayout({ children }: { children: React.ReactNode }) {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  return (
    <div className="flex min-h-screen bg-white dark:bg-dark-bg">
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <main className="flex-1 overflow-auto bg-white dark:bg-dark-bg min-w-0">
        {/* Mobile header with hamburger */}
        <div className="sticky top-0 z-30 lg:hidden bg-white/80 dark:bg-dark-bg/80 backdrop-blur-md border-b border-gray-200 dark:border-dark-border px-4 py-3 flex items-center gap-3">
          <button
            onClick={() => setSidebarOpen(true)}
            className="p-2 rounded-xl text-gray-500 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-dark-card transition-all duration-200"
          >
            <Bars3Icon className="w-6 h-6" />
          </button>
          <h1 className="text-sm font-bold text-gray-900 dark:text-white">ON-SERVER1</h1>
        </div>
        <div className="p-3 sm:p-4 lg:p-6">
          <PageTransition>{children}</PageTransition>
        </div>
      </main>
    </div>
  );
}

function WrappedRoute({ children }: { children: React.ReactNode }) {
  return (
    <ProtectedRoute>
      <DashboardLayout>{children}</DashboardLayout>
    </ProtectedRoute>
  );
}

export default function App() {
  return (
    <BrowserRouter basename="/ctrl-7x9a3k">
      <Toaster position="top-left" toastOptions={{
        style: { background: '#1A1A2E', color: '#fff', borderRadius: '12px', border: '1px solid #374151' },
        success: { iconTheme: { primary: '#FFD700', secondary: '#000' } },
      }} />
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<WrappedRoute><Dashboard /></WrappedRoute>} />
        <Route path="/products" element={<WrappedRoute><Products /></WrappedRoute>} />
        <Route path="/categories" element={<WrappedRoute><Categories /></WrappedRoute>} />
        <Route path="/orders" element={<WrappedRoute><Orders /></WrappedRoute>} />
        <Route path="/deposits" element={<WrappedRoute><Deposits /></WrappedRoute>} />
        <Route path="/users" element={<WrappedRoute><Users /></WrappedRoute>} />
        <Route path="/banners" element={<WrappedRoute><Banners /></WrappedRoute>} />
        <Route path="/email" element={<WrappedRoute><EmailSettings /></WrappedRoute>} />
        <Route path="/settings" element={<WrappedRoute><Settings /></WrappedRoute>} />
      </Routes>
    </BrowserRouter>
  );
}
