import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Card, Form, Button } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { useGeolocation } from '../hooks/useGeolocation';
import { land as landApi } from '../services/api';

export default function ListLandPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { lat, lon } = useGeolocation();
  const [form, setForm] = useState({
    title: '', description: '', pricePerMonth: '', area: '', location: '', imageUrl: '',
    latitude: lat, longitude: lon,
  });
  const [saving, setSaving] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    setSaving(true);
    landApi.create({
      ...form,
      pricePerMonth: parseFloat(form.pricePerMonth),
      latitude: form.latitude || lat,
      longitude: form.longitude || lon,
    }).then(() => navigate('/dashboard')).catch(() => {}).finally(() => setSaving(false));
  };

  if (!user) return null;

  return (
    <Container className="py-4">
      <h2>{t('land.listLand')}</h2>
      <Card>
        <Card.Body>
          <Form onSubmit={handleSubmit}>
            <Form.Group className="mb-3"><Form.Label>{t('equipment.titleLabel')}</Form.Label><Form.Control value={form.title} onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))} required /></Form.Group>
            <Form.Group className="mb-3"><Form.Label>{t('equipment.description')}</Form.Label><Form.Control as="textarea" value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} /></Form.Group>
            <Form.Group className="mb-3"><Form.Label>{t('land.pricePerMonth')}</Form.Label><Form.Control type="number" step="0.01" value={form.pricePerMonth} onChange={(e) => setForm((f) => ({ ...f, pricePerMonth: e.target.value }))} required /></Form.Group>
            <Form.Group className="mb-3"><Form.Label>{t('land.area')}</Form.Label><Form.Control value={form.area} onChange={(e) => setForm((f) => ({ ...f, area: e.target.value }))} /></Form.Group>
            <Form.Group className="mb-3"><Form.Label>{t('equipment.location')}</Form.Label><Form.Control value={form.location} onChange={(e) => setForm((f) => ({ ...f, location: e.target.value }))} /></Form.Group>
            <Form.Group className="mb-3"><Form.Label>{t('equipment.imageUrl')}</Form.Label><Form.Control value={form.imageUrl} onChange={(e) => setForm((f) => ({ ...f, imageUrl: e.target.value }))} /></Form.Group>
            <Button type="submit" disabled={saving}>{saving ? t('common.loading') : t('equipment.submit')}</Button>
          </Form>
        </Card.Body>
      </Card>
    </Container>
  );
}
