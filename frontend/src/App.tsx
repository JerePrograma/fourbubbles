import { Navigate, Route, Routes } from 'react-router-dom';
import { AppShell } from './components/AppShell';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AgendaPage } from './pages/AgendaPage';
import { AuditPage } from './pages/AuditPage';
import { ClientsPage } from './pages/ClientsPage';
import { CompatibilityPage } from './pages/CompatibilityPage';
import { DashboardPage } from './pages/DashboardPage';
import { EditClientPage } from './pages/EditClientPage';
import { LoginPage } from './pages/LoginPage';
import { NewClientPage } from './pages/NewClientPage';
import { NewOrderPage } from './pages/NewOrderPage';
import { OrderDetailPage } from './pages/OrderDetailPage';
import { OrdersPage } from './pages/OrdersPage';
import { ProductionConfigurationPage } from './pages/ProductionConfigurationPage';
import { ProductionPage } from './pages/ProductionPage';
import { ProductionMetricsPage } from './pages/ProductionMetricsPage';
import { ProductionSeparationPage } from './pages/ProductionSeparationPage';
import { ReceptionPage } from './pages/ReceptionPage';

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
          <Route path="/orders/:id" element={<OrderDetailPage />} />
          <Route path="/orders/:id/reception" element={<ReceptionPage />} />
          <Route path="/orders/:id/compatibility" element={<CompatibilityPage />} />
          <Route path="/production" element={<ProductionPage />} />
          <Route path="/production/configuration" element={<ProductionConfigurationPage />} />
          <Route path="/production/metrics" element={<ProductionMetricsPage />} />
          <Route path="/production/separation" element={<ProductionSeparationPage />} />
          <Route path="/agenda" element={<AgendaPage />} />
          <Route path="/audit" element={<AuditPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
