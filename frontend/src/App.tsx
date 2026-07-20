import { Navigate, Route, Routes } from 'react-router-dom';
import { AppShell } from './components/AppShell';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AgendaPage } from './pages/AgendaPage';
import { ClientsPage } from './pages/ClientsPage';
import { DashboardPage } from './pages/DashboardPage';
import { EditClientPage } from './pages/EditClientPage';
import { LoginPage } from './pages/LoginPage';
import { NewClientPage } from './pages/NewClientPage';
import { NewOrderPage } from './pages/NewOrderPage';
import { OrdersPage } from './pages/OrdersPage';

export default function App(): JSX.Element {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppShell />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/clients" element={<ClientsPage />} />
          <Route path="/clients/new" element={<NewClientPage />} />
          <Route path="/clients/:id/edit" element={<EditClientPage />} />
          <Route path="/orders" element={<OrdersPage />} />
          <Route path="/orders/new" element={<NewOrderPage />} />
          <Route path="/agenda" element={<AgendaPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
