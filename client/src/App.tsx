import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/layout/Layout';
import HomePage from './pages/HomePage';
import AuthPage from './pages/AuthPage';
import ProductPage from './pages/ProductPage';
import BidPage from './pages/BidPage';
import PaymentPage from './pages/PaymentPage';
import MyPage from './pages/MyPage';
import NotificationPage from './pages/NotificationPage';
import AdminPage from './pages/AdminPage';
import ProtectedRoute from './components/auth/ProtectedRoute';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/auth" element={<AuthPage />} />

          {/* Product Routes */}
          <Route path="/products" element={<ProductPage />} />
          <Route path="/products/:id" element={<ProductPage />} />

          {/* Protected Routes (Login Required) */}
          <Route element={<ProtectedRoute />}>
            <Route path="/bids/buy" element={<BidPage />} />
            <Route path="/bids/sell" element={<BidPage />} />
            <Route path="/payment" element={<PaymentPage />} />
            <Route path="/me" element={<MyPage />} />
            <Route path="/notifications" element={<NotificationPage />} />
            <Route path="/admin" element={<AdminPage />} /> {/* Added Admin Route */}
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
