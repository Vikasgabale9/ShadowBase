
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
      <h1>Welcome to ShadowBase</h1>
      <p>Welcome, {user?.username}</p>
      <p>Role: {user?.role}</p>
      <button className="btn btn-danger" onClick={logout}>
        Logout
      </button>
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
