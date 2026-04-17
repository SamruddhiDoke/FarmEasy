import React, { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Form, Button, Card, Container, Alert, Row, Col, Spinner } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import { auth } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { normalizeEmail, normalizeOtpInput } from '../utils/inputNormalize';

const GEOLOCATION_OPTIONS = { enableHighAccuracy: true, timeout: 10000, maximumAge: 300000 };
const RESEND_COOLDOWN_SEC = 60;

export default function RegisterPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { login } = useAuth();
  const [step, setStep] = useState('register');
  const [latitude, setLatitude] = useState(null);
  const [longitude, setLongitude] = useState(null);
  const [locationPermissionDenied, setLocationPermissionDenied] = useState(false);
  const [form, setForm] = useState({
    name: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    location: '',
    preferredLanguage: 'en',
    role: 'FARMER',
  });
  const [otp, setOtp] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [resendSec, setResendSec] = useState(0);

  useEffect(() => {
    if (typeof navigator === 'undefined' || !navigator.geolocation) {
      setLocationPermissionDenied(true);
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setLatitude(position.coords.latitude);
        setLongitude(position.coords.longitude);
        setLocationPermissionDenied(false);
      },
      (err) => {
        if (import.meta.env.DEV) {
          console.log('Location permission denied or error:', err.code, err.message);
        }
        setLocationPermissionDenied(true);
        setLatitude(null);
        setLongitude(null);
      },
      GEOLOCATION_OPTIONS
    );
  }, []);

  useEffect(() => {
    if (step !== 'otp' || resendSec <= 0) return undefined;
    const id = setInterval(() => setResendSec((s) => (s <= 1 ? 0 : s - 1)), 1000);
    return () => clearInterval(id);
  }, [step, resendSec]);

  const startResendCooldown = useCallback(() => {
    setResendSec(RESEND_COOLDOWN_SEC);
  }, []);

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    setLoading(true);
    try {
      const normalizedEmail = normalizeEmail(form.email);
      const payload = {
        ...form,
        email: normalizedEmail,
        latitude: latitude ?? undefined,
        longitude: longitude ?? undefined,
      };
      const res = await auth.register(payload);
      const data = res.data?.data;
      if (res.data?.success) {
        if (data?.email) {
          setForm((f) => ({ ...f, email: data.email }));
        }
        setOtp('');
        setStep('otp');
        startResendCooldown();
      } else {
        setError(res.data?.message || 'Registration failed');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
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
      const res = await auth.verifyRegistration({
        email: normalizeEmail(form.email),
        otp: digits,
        purpose: 'REGISTRATION',
      });
      if (res.data?.success && res.data?.data?.token) {
        login(res.data.data.token, res.data.data);
        navigate('/dashboard');
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
      await auth.resendOtp(normalizeEmail(form.email), 'REGISTRATION');
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
          <Col md={9} lg={7}>
            <Card className="auth-card card-modern">
              <Card.Body className="p-4 p-md-5">
                <div className="auth-card-header">
                  <h1 className="auth-card-title mb-1">{t('app.register')}</h1>
                  <p className="auth-card-subtitle mb-0">
                    {step === 'register' ? t('auth.registerSubtitle') : t('auth.secureStep')}
                  </p>
                </div>

                {error && (
                  <Alert variant="danger" className="rounded-3 border-0 py-2">
                    {error}
                  </Alert>
                )}

                {step === 'register' ? (
                  <Form onSubmit={handleRegister}>
                    <Row>
                      <Col md={6}>
                        <Form.Group className="mb-3">
                          <Form.Label className="auth-form-label">{t('auth.name')}</Form.Label>
                          <Form.Control
                            className="auth-form-control"
                            value={form.name}
                            onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
                            required
                          />
                        </Form.Group>
                      </Col>
                      <Col md={6}>
                        <Form.Group className="mb-3">
                          <Form.Label className="auth-form-label">{t('auth.email')}</Form.Label>
                          <Form.Control
                            className="auth-form-control"
                            type="email"
                            value={form.email}
                            onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
                            autoComplete="email"
                            required
                          />
                        </Form.Group>
                      </Col>
                    </Row>
                    <Form.Group className="mb-3">
                      <Form.Label className="auth-form-label">{t('auth.phone')}</Form.Label>
                      <Form.Control
                        className="auth-form-control"
                        value={form.phone}
                        onChange={(e) => setForm((f) => ({ ...f, phone: e.target.value }))}
                      />
                    </Form.Group>
                    <Form.Group className="mb-3">
                      <Form.Label className="auth-form-label">{t('auth.location')}</Form.Label>
                      <Form.Control
                        className="auth-form-control"
                        value={form.location}
                        onChange={(e) => setForm((f) => ({ ...f, location: e.target.value }))}
                        placeholder="e.g. Mumbai, Pune"
                      />
                      {latitude != null && longitude != null && (
                        <Form.Text className="text-success d-block small">{t('auth.locationDetected')}</Form.Text>
                      )}
                      {locationPermissionDenied && (
                        <Alert variant="warning" className="mt-2 small mb-0 rounded-3">
                          {t('auth.locationPermissionDenied')}
                        </Alert>
                      )}
                    </Form.Group>
                    <Row>
                      <Col md={6}>
                        <Form.Group className="mb-3">
                          <Form.Label className="auth-form-label">{t('auth.language')}</Form.Label>
                          <Form.Select
                            className="auth-form-control"
                            value={form.preferredLanguage}
                            onChange={(e) => setForm((f) => ({ ...f, preferredLanguage: e.target.value }))}
                          >
                            <option value="en">English</option>
                            <option value="hi">हिन्दी</option>
                            <option value="mr">मराठी</option>
                          </Form.Select>
                        </Form.Group>
                      </Col>
                      <Col md={6}>
                        <Form.Group className="mb-3">
                          <Form.Label className="auth-form-label">{t('auth.role')}</Form.Label>
                          <Form.Select
                            className="auth-form-control"
                            value={form.role}
                            onChange={(e) => setForm((f) => ({ ...f, role: e.target.value }))}
                          >
                            <option value="FARMER">{t('auth.farmer')}</option>
                            <option value="CUSTOMER">{t('auth.customer')}</option>
                          </Form.Select>
                        </Form.Group>
                      </Col>
                    </Row>
                    <Row>
                      <Col md={6}>
                        <Form.Group className="mb-3">
                          <Form.Label className="auth-form-label">{t('auth.password')}</Form.Label>
                          <Form.Control
                            className="auth-form-control"
                            type="password"
                            value={form.password}
                            onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))}
                            minLength={6}
                            autoComplete="new-password"
                            required
                          />
                        </Form.Group>
                      </Col>
                      <Col md={6}>
                        <Form.Group className="mb-4">
                          <Form.Label className="auth-form-label">{t('auth.confirmPassword')}</Form.Label>
                          <Form.Control
                            className="auth-form-control"
                            type="password"
                            value={form.confirmPassword}
                            onChange={(e) => setForm((f) => ({ ...f, confirmPassword: e.target.value }))}
                            autoComplete="new-password"
                            required
                          />
                        </Form.Group>
                      </Col>
                    </Row>
                    <Button type="submit" className="w-100 auth-btn-primary btn-primary" disabled={loading}>
                      {loading ? (
                        <>
                          <Spinner animation="border" size="sm" className="me-2" />
                          {t('common.loading')}
                        </>
                      ) : (
                        t('app.register')
                      )}
                    </Button>
                  </Form>
                ) : (
                  <Form onSubmit={handleVerifyOtp}>
                    <p className="text-center text-muted small mb-2">{t('auth.otpSent')}</p>
                    <div className="text-center">
                      <span className="auth-email-pill">{form.email}</span>
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
                      <Button type="button" variant="link" className="auth-link-muted py-0" onClick={() => setStep('register')}>
                        {t('auth.back')}
                      </Button>
                    </div>
                  </Form>
                )}

                <p className="text-center mt-4 mb-0">
                  <Link to="/login" className="text-decoration-none fw-semibold" style={{ color: 'var(--primary)' }}>
                    {t('app.login')}
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
