import React, { useState, useContext } from 'react';
import { Calendar } from 'lucide-react';
import { AuthContext } from '../../context/AuthContext';

export const AuthPage = () => {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const { login, register } = useContext(AuthContext);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      if (isLogin) {
        await login(email, password);
      } else {
        await register(email, password, name);
        setSuccess('Registration successful! Please login with your credentials.');
        setIsLogin(true);
        setPassword('');
        setName('');
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center bg-light">
      <div className="container">
        <div className="row justify-content-center">
          <div className="col-md-6 col-lg-5">
            <div className="card shadow">
              <div className="card-body p-5">
                <div className="text-center mb-4">
                  <Calendar size={48} className="text-primary mb-3" />
                  <h2 className="fw-bold">Meeting Room Booking</h2>
                  <p className="text-muted">
                    {isLogin ? 'Sign in to your account' : 'Create a new account'}
                  </p>
                </div>

                {error && (
                  <div className="alert alert-danger alert-dismissible fade show" role="alert">
                    {error}
                    <button type="button" className="btn-close" onClick={() => setError('')}></button>
                  </div>
                )}

                {success && (
                  <div className="alert alert-success alert-dismissible fade show" role="alert">
                    {success}
                    <button type="button" className="btn-close" onClick={() => setSuccess('')}></button>
                  </div>
                )}

                <form onSubmit={handleSubmit}>
                  {!isLogin && (
                    <div className="mb-3">
                      <label className="form-label">Full Name</label>
                      <input
                        type="text"
                        className="form-control"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        placeholder="Enter your full name"
                        required
                      />
                    </div>
                  )}

                  <div className="mb-3">
                    <label className="form-label">Email Address</label>
                    <input
                      type="email"
                      className="form-control"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      placeholder="Enter your email"
                      required
                    />
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Password</label>
                    <input
                      type="password"
                      className="form-control"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="Enter your password"
                      required
                      minLength={6}
                    />
                    {!isLogin && (
                      <small className="form-text text-muted">
                        Password must be at least 6 characters
                      </small>
                    )}
                  </div>

                  <button type="submit" className="btn btn-primary w-100 py-2" disabled={loading}>
                    {loading ? (
                      <>
                        <span
                          className="spinner-border spinner-border-sm me-2"
                          role="status"
                          aria-hidden="true"
                        ></span>
                        Please wait...
                      </>
                    ) : (
                      isLogin ? 'Sign In' : 'Sign Up'
                    )}
                  </button>
                </form>

                <div className="text-center mt-3">
                  <button
                    className="btn btn-link text-decoration-none"
                    onClick={() => {
                      setIsLogin(!isLogin);
                      setError('');
                      setSuccess('');
                    }}
                  >
                    {isLogin ? "Don't have an account? Sign up" : 'Already have an account? Sign in'}
                  </button>
                </div>

                <div className="mt-4 pt-4 border-top">
                  <p className="text-center text-muted small mb-2">
                    <strong>Authentication powered by Keycloak</strong>
                  </p>
                  <p className="text-center text-muted small mb-0">
                    Secure JWT-based authentication with role-based access control
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
