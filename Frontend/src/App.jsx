
import { Routes, Route, Navigate } from "react-router-dom";

import AuthPage from "./pages/auth/AuthPage";
import DashboardLayout from "./components/layout/DashboardLayout";
import Dashboard from "./pages/Dashboard";

// Authentication check
function isAuthenticated() {
  return Boolean(localStorage.getItem("token"));
}

// Protect dashboard routes
function ProtectedRoute({ children }) {
  return isAuthenticated()
    ? children
    : <Navigate to="/" replace />;
}

// Placeholder for modules under development
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
      {/* Login / Registration */}
      <Route
        path="/"
        element={
          isAuthenticated()
            ? <Navigate to="/dashboard" replace />
            : <AuthPage />
        }
      />

      {/* Protected Dashboard */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        {/* Dashboard Home */}
        <Route index element={<Dashboard />} />

        {/* Employee Management */}
        <Route
          path="employees"
          element={
            <ModulePlaceholder title="Employee Management" />
          }
        />

        {/* Department Management */}
        <Route
          path="departments"
          element={
            <ModulePlaceholder title="Department Management" />
          }
        />

        {/* Project Management */}
        <Route
          path="projects"
          element={
            <ModulePlaceholder title="Project Management" />
          }
        />

        {/* Schema Migration */}
        <Route
          path="schema-migrations"
          element={
            <ModulePlaceholder title="Schema Migrations" />
          }
        />

        {/* Settings */}
        <Route
          path="settings"
          element={
            <ModulePlaceholder title="Settings" />
          }
        />
      </Route>

      {/* Invalid routes */}
      <Route
        path="*"
        element={<Navigate to="/" replace />}
      />
    </Routes>
  );
}

export default App;
s