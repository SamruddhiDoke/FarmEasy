import React, { useMemo, useState } from 'react';
import { Container, Card, Form, Button, Alert } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { orders as ordersApi } from '../services/api';

export default function CheckoutPage() {
  const { user } = useAuth();
  const { items, totals, clear } = useCart();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    fullName: user?.name || '',
    address: '',
    city: '',
    state: '',
    pincode: '',
    phone: '',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const canCheckout = useMemo(() => items.length > 0, [items.length]);

  if (!user) return null;

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    if (!canCheckout) return;
    setSaving(true);
    try {
      const payload = {
        address: form,
        items: items.map((x) => ({
          type: x.type,
          referenceId: x.id,
          title: x.title,
          unitPrice: Number(x.price),
          qty: Number(x.qty),
        })),
      };
      const res = await ordersApi.create(payload);
      if (res.data?.success) {
        clear();
        navigate('/dashboard', { state: { orderPlaced: true } });
      } else {
        setError(res.data?.message || 'Checkout failed');
      }
    } catch (err) {
      setError(err?.response?.data?.message || err.message || 'Checkout failed');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Container className="py-4">
      <h2 className="mb-3">Checkout</h2>
      {error && <Alert variant="danger">{error}</Alert>}

      <Card className="card-modern border-0">
        <Card.Body>
          <div className="mb-3 fw-bold">Total: ₹{totals.subtotal.toFixed(2)}</div>
          <Form onSubmit={submit}>
            <Form.Group className="mb-2">
              <Form.Label>Full Name</Form.Label>
              <Form.Control value={form.fullName} onChange={(e) => setForm((f) => ({ ...f, fullName: e.target.value }))} required />
            </Form.Group>
            <Form.Group className="mb-2">
              <Form.Label>Address</Form.Label>
              <Form.Control value={form.address} onChange={(e) => setForm((f) => ({ ...f, address: e.target.value }))} required />
            </Form.Group>
            <Form.Group className="mb-2">
              <Form.Label>City</Form.Label>
              <Form.Control value={form.city} onChange={(e) => setForm((f) => ({ ...f, city: e.target.value }))} required />
            </Form.Group>
            <Form.Group className="mb-2">
              <Form.Label>State</Form.Label>
              <Form.Control value={form.state} onChange={(e) => setForm((f) => ({ ...f, state: e.target.value }))} required />
            </Form.Group>
            <Form.Group className="mb-2">
              <Form.Label>Pincode</Form.Label>
              <Form.Control value={form.pincode} onChange={(e) => setForm((f) => ({ ...f, pincode: e.target.value }))} required />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Phone</Form.Label>
              <Form.Control value={form.phone} onChange={(e) => setForm((f) => ({ ...f, phone: e.target.value }))} required />
            </Form.Group>

            <Button type="submit" disabled={saving || !canCheckout}>
              {saving ? '...' : 'Proceed to Payment (Demo)'}
            </Button>
          </Form>
        </Card.Body>
      </Card>
    </Container>
  );
}

