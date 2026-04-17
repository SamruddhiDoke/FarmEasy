import React, { useState, useEffect, useCallback } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Form, Button, Card, Container, Alert, Row, Col, Spinner } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import { auth } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { normalizeEmail, normalizeOtpInput } from '../utils/inputNormalize';

const RESEND_COOLDOWN_SEC = 60;

export default function LoginPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const [step, setStep] = useState('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [otp, setOtp] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [resendSec, setResendSec] = useState(0);

  useEffect(() => {
    if (step !== 'otp' || resendSec <= 0) return undefined;
    const id = setInterval(() => setResendSec((s) => (s <= 1 ? 0 : s - 1)), 1000);
    return () => clearInterval(id);
  }, [step, resendSec]);

  const startResendCooldown = useCallback(() => {
    setResendSec(RESEND_COOLDOWN_SEC);
  }, []);

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    const normalizedEmail = normalizeEmail(email);
    try {
      const res = await auth.login({ email: normalizedEmail, password });
      const payload = res.data;
      const data = payload?.data;
      if (payload?.success && data?.requiresOtp) {
        if (data.email) setEmail(data.email);
        else setEmail(normalizedEmail);
        setOtp('');
        setStep('otp');
        startResendCooldown();
      } else if (payload?.success && data?.token) {
        login(data.token, data);
        const roles = data.roles || [];
        const isAdmin = roles.includes('ROLE_ADMIN');
        const redirectTo = location.state?.from || (isAdmin ? '/admin/dashboard' : '/dashboard');
        navigate(redirectTo, { replace: true });
      } else {
        setError(payload?.message || 'Login failed');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    const digits = normalizeOtpInput(otp);
    if (digits.length !== 6) {
      setError(t('auth.enterOtp'));
      setLoading(false);
      return;
    }
    try {
      const res = await auth.verifyLogin({
        email: normalizeEmail(email),
        otp: digits,
        purpose: 'LOGIN',
      });
      if (res.data?.success && res.data?.data?.token) {
        const data = res.data.data;
        login(data.token, data);
        const redirectTo =
          location.state?.from || (data.roles?.includes('ROLE_ADMIN') ? '/admin/dashboard' : '/dashboard');
        navigate(redirectTo, { replace: true });
      } else {
        setError(res.data?.message || 'Invalid OTP');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid OTP');
    } finally {
      setLoading(false);
    }
  };

  const handleResendOtp = async () => {
    if (resendSec > 0 || loading) return;
    setError('');
    setLoading(true);
    try {
      await auth.resendOtp(normalizeEmail(email), 'LOGIN');
      startResendCooldown();
    } catch {
      setError(t('auth.otpSent'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <Container>
        <Row className="justify-content-center">
          <Col md={6} lg={5}>
            <Card className="auth-card card-modern">
              <Card.Body className="p-4 p-md-5">
                <div className="auth-card-header">
                  <h1 className="auth-card-title mb-1">{t('app.login')}</h1>
                  <p className="auth-card-subtitle mb-0">
                    {step === 'login' ? t('auth.loginSubtitle') : t('auth.secureStep')}
                  </p>
                </div>

                {error && (
                  <Alert variant="danger" className="rounded-3 border-0 py-2">
                    {error}
                  </Alert>
                )}

                {step === 'login' ? (
                  <Form onSubmit={handleLogin}>
                    <Form.Group className="mb-3">
                      <Form.Label className="auth-form-label">{t('auth.email')}</Form.Label>
                      <Form.Control
                        className="auth-form-control"
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        autoComplete="email"
                        required
                      />
                    </Form.Group>
                    <Form.Group className="mb-4">
                      <Form.Label className="auth-form-label">{t('auth.password')}</Form.Label>
                      <Form.Control
                        className="auth-form-control"
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        autoComplete="current-password"
                        required
                      />
                    </Form.Group>
                    <Button type="submit" className="w-100 auth-btn-primary btn-primary" disabled={loading}>
                      {loading ? (
                        <>
                          <Spinner animation="border" size="sm" className="me-2" />
                          {t('common.loading')}
                        </>
                      ) : (
                        t('app.login')
                      )}
                    </Button>
                  </Form>
                ) : (
                  <Form onSubmit={handleVerifyOtp}>
                    <p className="text-center text-muted small mb-2">{t('auth.otpSent')}</p>
                    <div className="text-center">
                      <span className="auth-email-pill">{email}</span>
                    </div>
                    <p className="small text-muted text-center mb-3">{t('auth.otpHint')}</p>
                    <Form.Group className="mb-3 text-center">
                      <Form.Label className="auth-form-label d-block">{t('auth.enterOtp')}</Form.Label>
                      <Form.Control
                        className="auth-form-control otp-input"
                        type="text"
                        inputMode="numeric"
                        autoComplete="one-time-code"
                        value={otp}
                        onChange={(e) => setOtp(normalizeOtpInput(e.target.value))}
                        placeholder="••••••"
                        maxLength={6}
                        required
                        aria-label={t('auth.enterOtp')}
                      />
                    </Form.Group>
                    <Button type="submit" className="w-100 auth-btn-primary btn-primary mb-2" disabled={loading}>
                      {loading ? (
                        <>
                          <Spinner animation="border" size="sm" className="me-2" />
                          {t('common.loading')}
                        </>
                      ) : (
                        t('auth.verifyOtp')
                      )}
                    </Button>
                    <div className="d-flex flex-column align-items-center gap-2 mt-3">
                      <Button
                        type="button"
                        variant="outline-secondary"
                        size="sm"
                        className="rounded-pill px-3"
                        disabled={resendSec > 0 || loading}
                        onClick={handleResendOtp}
                      >
                        {resendSec > 0 ? t('auth.resendIn', { seconds: resendSec }) : t('auth.resendOtp')}
                      </Button>
                      <Button type="button" variant="link" className="auth-link-muted py-0" onClick={() => setStep('login')}>
                        {t('auth.back')}
                      </Button>
                    </div>
                  </Form>
                )}

                <p className="text-center mt-4 mb-0">
                  <Link to="/register" className="text-decoration-none fw-semibold" style={{ color: 'var(--primary)' }}>
                    {t('app.register')}
                  </Link>
                </p>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </div>
  );
}
