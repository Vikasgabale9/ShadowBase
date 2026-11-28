

import { Routes, Route, Navigate } from "react-router-dom";

import AuthPage from "./pages/auth/AuthPage";
import DashboardLayout from "./components/layout/DashboardLayout";
import Dashboard from "./pages/Dashboard";
import DepartmentPage from "./pages/DepartmentPage";
import EmployeePage from "./pages/EmployeePage";
import ProjectPage from "./pages/ProjectPage";

// Authentication check
function isAuthenticated() {
  return Boolean(localStorage.getItem("token"));
}

// Protect dashboard routes
function ProtectedRoute({ children }) {
  if (!isAuthenticated()) {
    return <Navigate to="/" replace />;
  }

  return children;
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
          element={<EmployeePage />}
        />

        {/* Department Management */}
        <Route
          path="departments"
          element={<DepartmentPage />}
        />

        {/* Project Management */}
        <Route
          path="projects"
          element={<ProjectPage />}
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
