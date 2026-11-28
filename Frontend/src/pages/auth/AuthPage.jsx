
import { useState } from 'react';
import {
  Container,
  Row,
  Col,
  Card,
  Form,
  Button,
  Alert,
  Spinner,
} from 'react-bootstrap';
import { useNavigate } from "react-router-dom";
import { ShieldCheck, LockKeyhole, Users } from 'lucide-react';
import { loginUser, registerUser } from '../../services/authService';
import '../../App.css';

function AuthPage() {
  const [isLogin, setIsLogin] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
  });

  const handleChange = (event) => {
    setFormData({
      ...formData,
      [event.target.name]: event.target.value,
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    setError('');
    setMessage('');
    setLoading(true);

    try {
      const response = isLogin
        ? await loginUser({
            email: formData.email,
            password: formData.password,
          })
        : await registerUser(formData);

      if (!response.success || !response.token) {
        throw new Error(response.message || 'Authentication failed');
      }

      // Store authentication details
      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify({
        userId: response.userId,
        username: response.username,
        email: response.email,
        role: response.role,
      }));

      setMessage(response.message);

      // Navigate to dashboard
     navigate("/dashboard", { replace: true });

    } catch (err) {
      const responseData = err.response?.data;

      setError(
        responseData?.message ||
        err.message ||
        'Something went wrong. Please try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <Container fluid>
        <Row className="min-vh-100">

          {/* Left branding section */}
          <Col
            lg={6}
            className="auth-branding d-none d-lg-flex"
          >
            <div className="branding-content">

              <div className="brand-logo">
                <ShieldCheck size={38} />
                <span>ShadowBase</span>
              </div>

              <div className="branding-message">
                <h1>
                  Manage your workforce.
                  <br />
                  Simplify your operations.
                </h1>

                <p>
                  A centralized employee management platform
                  built for secure, reliable and efficient
                  workforce operations.
                </p>
              </div>

              <div className="branding-footer">
                <Users size={18} />
                Employee Management System
              </div>

            </div>
          </Col>

          {/* Authentication section */}
          <Col
            lg={6}
            className="d-flex align-items-center justify-content-center"
          >
            <div className="auth-form-container">

              <div className="mobile-brand d-lg-none">
                <ShieldCheck size={32} />
                <span>ShadowBase</span>
              </div>

              <div className="mb-4">
                <h2>
                  {isLogin ? 'Welcome back' : 'Create your account'}
                </h2>

                <p className="text-secondary">
                  {isLogin
                    ? 'Enter your credentials to access your account.'
                    : 'Register to get started with ShadowBase.'}
                </p>
              </div>

              {error && (
                <Alert variant="danger">
                  {error}
                </Alert>
              )}

              {message && (
                <Alert variant="success">
                  {message}
                </Alert>
              )}

              <Form onSubmit={handleSubmit}>

                {!isLogin && (
                  <Form.Group className="mb-3">
                    <Form.Label>Username</Form.Label>
                    <Form.Control
                      type="text"
                      name="username"
                      placeholder="Enter username"
                      value={formData.username}
                      onChange={handleChange}
                      required
                      minLength={3}
                      maxLength={80}
                    />
                  </Form.Group>
                )}

                <Form.Group className="mb-3">
                  <Form.Label>Email address</Form.Label>
                  <Form.Control
                    type="email"
                    name="email"
                    placeholder="Enter your email"
                    value={formData.email}
                    onChange={handleChange}
                    required
                  />
                </Form.Group>

                <Form.Group className="mb-4">
                  <Form.Label>Password</Form.Label>
                  <Form.Control
                    type="password"
                    name="password"
                    placeholder="Enter your password"
                    value={formData.password}
                    onChange={handleChange}
                    required
                    minLength={8}
                  />
                </Form.Group>

                <Button
                  type="submit"
                  className="auth-submit-btn w-100"
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <Spinner
                        size="sm"
                        animation="border"
                        className="me-2"
                      />
                      Please wait...
                    </>
                  ) : (
                    isLogin ? 'Sign In' : 'Create Account'
                  )}
                </Button>

              </Form>

              <div className="auth-switch">
                {isLogin
                  ? "Don't have an account?"
                  : 'Already have an account?'}

                <button
                  type="button"
                  onClick={() => {
                    setIsLogin(!isLogin);
                    setError('');
                    setMessage('');
                  }}
                >
                  {isLogin ? 'Create account' : 'Sign in'}
                </button>
              </div>

              <div className="auth-security">
                <LockKeyhole size={15} />
                Secured with JWT authentication
              </div>

            </div>
          </Col>

        </Row>
      </Container>
    </div>
  );
}

export default AuthPage;
