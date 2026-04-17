import './i18n';
import React from 'react';
import { useTranslation } from 'react-i18next';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import LanguageSwitcher from './components/LanguageSwitcher';
import AiAssistant from './components/AiAssistant';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import EquipmentPage from './pages/EquipmentPage';
import EquipmentDetailPage from './pages/EquipmentDetailPage';
import LandPage from './pages/LandPage';
import LandDetailPage from './pages/LandDetailPage';
import TradePage from './pages/TradePage';
import TradeDetailPage from './pages/TradeDetailPage';
import SchemesPage from './pages/SchemesPage';
import ProfilePage from './pages/ProfilePage';
import AdminPage from './pages/AdminPage';
import CartPage from './pages/CartPage';
import CheckoutPage from './pages/CheckoutPage';
import ListEquipmentPage from './pages/ListEquipmentPage';
import ListLandPage from './pages/ListLandPage';
import ListTradePage from './pages/ListTradePage';

function PrivateRoute({ children }) {
  const { user, loading } = useAuth();
  const { t } = useTranslation();
  if (loading) return <div className="text-center p-5">{t('common.loading')}</div>;
  return user ? children : <Navigate to="/login" replace state={{ from: window.location.pathname }} />;
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/schemes" element={<SchemesPage />} />
      <Route path="/equipment" element={<EquipmentPage />} />
      <Route path="/equipment/:id" element={<EquipmentDetailPage />} />
      <Route path="/land" element={<LandPage />} />
      <Route path="/land/:id" element={<LandDetailPage />} />
      <Route path="/trade" element={<TradePage />} />
      <Route path="/trade/:id" element={<TradeDetailPage />} />
      <Route path="/dashboard" element={<PrivateRoute><DashboardPage /></PrivateRoute>} />
      <Route path="/dashboard/list-equipment" element={<PrivateRoute><ListEquipmentPage /></PrivateRoute>} />
      <Route path="/dashboard/list-land" element={<PrivateRoute><ListLandPage /></PrivateRoute>} />
      <Route path="/dashboard/list-trade" element={<PrivateRoute><ListTradePage /></PrivateRoute>} />
      <Route path="/profile" element={<PrivateRoute><ProfilePage /></PrivateRoute>} />
      <Route path="/admin" element={<PrivateRoute><AdminPage /></PrivateRoute>} />
      <Route path="/admin/dashboard" element={<PrivateRoute><AdminPage /></PrivateRoute>} />
      <Route path="/cart" element={<PrivateRoute><CartPage /></PrivateRoute>} />
      <Route path="/checkout" element={<PrivateRoute><CheckoutPage /></PrivateRoute>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <Navbar />
        <main className="min-vh-100 d-flex flex-column">
          <div className="flex-grow-1" style={{ paddingTop: '56px' }}>
            <AppRoutes />
          </div>
          <Footer />
        </main>
        <LanguageSwitcher />
        <AiAssistant />
      </CartProvider>
    </AuthProvider>
  );
}
