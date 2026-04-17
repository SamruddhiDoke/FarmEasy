import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Card, Form, Button, Alert } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { useGeolocation } from '../hooks/useGeolocation';
import { trade as tradeApi } from '../services/api';

export default function ListTradePage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { lat, lon } = useGeolocation();
  const [form, setForm] = useState({
    cropName: '', description: '', pricePerUnit: '', unit: 'kg', quantity: '', location: '', imageUrl: '',
    latitude: null, longitude: null,
  });

  useEffect(() => {
    setForm((f) => ({ ...f, latitude: lat ?? f.latitude, longitude: lon ?? f.longitude }));
  }, [lat, lon]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = (e) => {
    e.preventDefault();
    setError(null);
    setSaving(true);
    tradeApi.create({
      ...form,
      pricePerUnit: parseFloat(form.pricePerUnit),
      latitude: form.latitude ?? lat ?? null,
      longitude: form.longitude ?? lon ?? null,
    })
      .then(() => navigate('/dashboard', { state: { tradeCreated: true } }))
      .catch((err) => {
        setError(err?.response?.data?.message ?? err.message ?? 'Failed to create crop listing');
      })
      .finally(() => setSaving(false));
  };

  if (!user) return null;

  return (
    <Container className="py-5 page-container">
      <h2 className="section-title mb-4">{t('trade.listCrop')}</h2>
      {error && <Alert variant="danger" dismissible onClose={() => setError(null)}>{error}</Alert>}
      <Card className="card-modern border-0">
        <Card.Body>
          <Form onSubmit={handleSubmit}>
            <Form.Group className="mb-3"><Form.Label>{t('trade.cropName')}</Form.Label><Form.Control value={form.cropName} onChange={(e) => setForm((f) => ({ ...f, cropName: e.target.value }))} required /></Form.Group>
            <Form.Group className="mb-3"><Form.Label>{t('equipment.description')}</Form.Label><Form.Control as="textarea" value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} /></Form.Group>
            <Form.Group className="mb-3"><Form.Label>{t('trade.pricePerUnit')}</Form.Label><Form.Control type="number" step="0.01" value={form.pricePerUnit} onChange={(e) => setForm((f) => ({ ...f, pricePerUnit: e.target.value }))} required /></Form.Group>
            <Form.Group className="mb-3"><Form.Label>{t('trade.unit')}</Form.Label><Form.Control value={form.unit} onChange={(e) => setForm((f) => ({ ...f, unit: e.target.value }))} /></Form.Group>
            <Form.Group className="mb-3"><Form.Label>{t('trade.quantity')}</Form.Label><Form.Control value={form.quantity} onChange={(e) => setForm((f) => ({ ...f, quantity: e.target.value }))} /></Form.Group>
            <Form.Group className="mb-3"><Form.Label>{t('equipment.location')}</Form.Label><Form.Control value={form.location} onChange={(e) => setForm((f) => ({ ...f, location: e.target.value }))} /></Form.Group>
            <Form.Group className="mb-3"><Form.Label>{t('equipment.imageUrl')}</Form.Label><Form.Control value={form.imageUrl} onChange={(e) => setForm((f) => ({ ...f, imageUrl: e.target.value }))} /></Form.Group>
            <Button type="submit" disabled={saving}>{saving ? t('common.loading') : t('equipment.submit')}</Button>
          </Form>
        </Card.Body>
      </Card>
    </Container>
  );
}
