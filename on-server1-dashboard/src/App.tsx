import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Products from './pages/Products';
import Orders from './pages/Orders';
import Users from './pages/Users';
import Banners from './pages/Banners';
import Categories from './pages/Categories';
import Settings from './pages/Settings';
import Deposits from './pages/Deposits';
import Sidebar from './components/Sidebar';

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
  return (
    <div className="flex min-h-screen bg-white dark:bg-dark-bg">
      <Sidebar />
      <main className="flex-1 p-6 overflow-auto bg-white dark:bg-dark-bg">
        <PageTransition>{children}</PageTransition>
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
    <BrowserRouter>
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
        <Route path="/settings" element={<WrappedRoute><Settings /></WrappedRoute>} />
      </Routes>
    </BrowserRouter>
  );
}
