
import { Routes, Route, Navigate } from "react-router-dom";

import AuthPage from "./pages/auth/AuthPage";
import DashboardLayout from "./components/layout/DashboardLayout";
import Dashboard from "./pages/Dashboard";

function isAuthenticated() {
  return Boolean(localStorage.getItem("token"));
}

function ProtectedRoute({ children }) {
  if (!isAuthenticated()) {
    return <Navigate to="/" replace />;
  }

  return children;
}

function ModulePlaceholder({ title }) {
  return (
    <div className="sb-content-card">
      <h3>{title}</h3>
      <p className="text-secondary">
        This module is under development.
      </p>
    </div>
  );
}

function App() {
  return (
    <Routes>
      <Route
        path="/"
        element={
          isAuthenticated()
            ? <Navigate to="/dashboard" replace />
            : <AuthPage />
        }
      />

      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Dashboard />} />

        <Route
          path="employees"
          element={<ModulePlaceholder title="Employee Management" />}
        />

        <Route
          path="departments"
          element={<ModulePlaceholder title="Department Management" />}
        />

        <Route
          path="projects"
          element={<ModulePlaceholder title="Project Management" />}
        />

        <Route
          path="schema-migrations"
          element={<ModulePlaceholder title="Schema Migrations" />}
        />

        <Route
          path="settings"
          element={<ModulePlaceholder title="Settings" />}
        />
      </Route>

      <Route
        path="*"
        element={<Navigate to="/" replace />}
      />
    </Routes>
  );
}

export default App;
