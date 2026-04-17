import React, { useState, useEffect } from 'react';
import { Container, Card, Form, Button, Row, Col } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { user as userApi } from '../services/api';

export default function ProfilePage() {
  const { t } = useTranslation();
  const { user, updateUser } = useAuth();
  const [form, setForm] = useState({
    name: '', phone: '', location: '', address: '', farmSize: '', equipmentOwned: '', landDetails: '', preferredLanguage: 'en',
  });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    userApi.getProfile().then((res) => {
      const d = res.data?.data;
      if (d) setForm({
        name: d.name || '',
        phone: d.phone || '',
        location: d.location || '',
        address: d.address || '',
        farmSize: d.farmSize || '',
        equipmentOwned: d.equipmentOwned || '',
        landDetails: d.landDetails || '',
        preferredLanguage: d.preferredLanguage || 'en',
      });
    }).catch(() => {});
  }, []);

  const handleSubmit = (e) => {
    e.preventDefault();
    setSaving(true);
    userApi.updateProfile(form).then((res) => {
      if (res.data?.data) updateUser(res.data.data);
    }).finally(() => setSaving(false));
  };

  if (!user) return null;

  return (
    <Container className="py-4">
      <h2>{t('app.profile')}</h2>
      <Card>
        <Card.Body>
          <Form onSubmit={handleSubmit}>
            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>{t('auth.name')}</Form.Label>
                  <Form.Control value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>{t('auth.phone')}</Form.Label>
                  <Form.Control value={form.phone} onChange={(e) => setForm((f) => ({ ...f, phone: e.target.value }))} />
                </Form.Group>
              </Col>
            </Row>
            <Form.Group className="mb-3">
              <Form.Label>{t('auth.location')}</Form.Label>
              <Form.Control value={form.location} onChange={(e) => setForm((f) => ({ ...f, location: e.target.value }))} />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>{t('profile.address')}</Form.Label>
              <Form.Control as="textarea" value={form.address} onChange={(e) => setForm((f) => ({ ...f, address: e.target.value }))} />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>{t('profile.farmSize')}</Form.Label>
              <Form.Control value={form.farmSize} onChange={(e) => setForm((f) => ({ ...f, farmSize: e.target.value }))} />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>{t('profile.equipmentOwned')}</Form.Label>
              <Form.Control as="textarea" value={form.equipmentOwned} onChange={(e) => setForm((f) => ({ ...f, equipmentOwned: e.target.value }))} />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>{t('profile.landDetails')}</Form.Label>
              <Form.Control as="textarea" value={form.landDetails} onChange={(e) => setForm((f) => ({ ...f, landDetails: e.target.value }))} />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>{t('auth.language')}</Form.Label>
              <Form.Select value={form.preferredLanguage} onChange={(e) => setForm((f) => ({ ...f, preferredLanguage: e.target.value }))}>
                <option value="en">English</option>
                <option value="hi">हिन्दी</option>
                <option value="mr">मराठी</option>
              </Form.Select>
            </Form.Group>
            <Button type="submit" disabled={saving}>{saving ? t('profile.saving') : t('profile.save')}</Button>
          </Form>
        </Card.Body>
      </Card>
    </Container>
  );
}
