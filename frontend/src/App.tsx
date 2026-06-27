import { Navigate, Route, Routes } from 'react-router-dom';
import type { ReactNode } from 'react';
import Layout from './components/Layout';
import AgentChatPage from './pages/AgentChatPage';
import AgentsPage from './pages/AgentsPage';
import DashboardPage from './pages/DashboardPage';
import DocumentsPage from './pages/DocumentsPage';
import LoginPage from './pages/LoginPage';
import WorkflowApprovalPage from './pages/WorkflowApprovalPage';
import WorkflowsPage from './pages/WorkflowsPage';
import { getToken } from './api/apiClient';

function RequireAuth({ children }: { children: ReactNode }) {
  if (!getToken()) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <RequireAuth>
            <Layout />
          </RequireAuth>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="documents" element={<DocumentsPage />} />
        <Route path="agents" element={<AgentsPage />} />
        <Route path="chat" element={<AgentChatPage />} />
        <Route path="workflows" element={<WorkflowsPage />} />
        <Route path="approvals" element={<WorkflowApprovalPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
