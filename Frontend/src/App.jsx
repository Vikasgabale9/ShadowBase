
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import AuthPage from './pages/auth/AuthPage';

function Dashboard() {
  const user = JSON.parse(localStorage.getItem('user') || 'null');

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/';
  };

  if (!localStorage.getItem('token')) {
    return <Navigate to="/" replace />;
  }

  return (
    <div className="container py-5">
      <nav className="d-flex justify-content-end align-items-center gap-4 mb-5">
  <a href="#departments" className="text-decoration-none text-primary">
    Departments
  </a>

  <a href="#employees" className="text-decoration-none text-primary">
    Employees
  </a>

  <a href="#projects" className="text-decoration-none text-primary">
    Projects
  </a>
</nav>
      <h1>Welcome to ShadowBase</h1>
      <p>Welcome, {user?.username}</p>
      <p>Role: {user?.role}</p>
      <button className="btn btn-danger" onClick={logout}>
        Logout
      </button>

    <div className="row g-4 mt-3">
  <div className="col-md-4" id="departments">
    <div className="card border-0 shadow-sm p-3">
      <h5>Departments</h5>
      <p className="text-muted mb-0">
        Manage your departments
      </p>
    </div>
  </div>

  <div className="col-md-4" id="employees">
    <div className="card border-0 shadow-sm p-3">
      <h5>Employees</h5>
      <p className="text-muted mb-0">
        Manage employee records
      </p>
    </div>
  </div>

  <div className="col-md-4" id="projects">
    <div className="card border-0 shadow-sm p-3">
      <h5>Projects</h5>
      <p className="text-muted mb-0">
        Manage your projects
      </p>
    </div>
  </div>
</div>

    </div>
  );
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<AuthPage />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
