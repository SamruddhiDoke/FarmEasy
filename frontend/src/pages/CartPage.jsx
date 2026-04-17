import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Container, Table, Button, Form, Alert } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export default function CartPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { items, updateQty, removeItem, totals, clear } = useCart();

  if (!user) return null;

  return (
    <Container className="py-4">
      <h2 className="mb-3">Cart</h2>

      {items.length === 0 ? (
        <Alert variant="info">
          Cart is empty. <Link to="/trade">Browse Trade</Link>
        </Alert>
      ) : (
        <>
          <Table responsive striped>
            <thead>
              <tr>
                <th>Item</th>
                <th>Price</th>
                <th style={{ width: 120 }}>Qty</th>
                <th>Total</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {items.map((x) => (
                <tr key={`${x.type}-${x.id}`}>
                  <td>
                    <div className="fw-semibold">{x.title}</div>
                    <div className="text-muted small">{x.type}</div>
                  </td>
                  <td>₹{x.price}</td>
                  <td>
                    <Form.Control
                      type="number"
                      min="1"
                      value={x.qty}
                      onChange={(e) => updateQty(x.type, x.id, e.target.value)}
                    />
                  </td>
                  <td>₹{(Number(x.price) * Number(x.qty)).toFixed(2)}</td>
                  <td className="text-end">
                    <Button variant="outline-danger" size="sm" onClick={() => removeItem(x.type, x.id)}>Remove</Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>

          <div className="d-flex justify-content-between align-items-center">
            <div className="fw-bold">Subtotal: ₹{totals.subtotal.toFixed(2)}</div>
            <div>
              <Button variant="outline-secondary" className="me-2" onClick={clear}>Clear</Button>
              <Button onClick={() => navigate('/checkout')}>Checkout</Button>
            </div>
          </div>
        </>
      )}
    </Container>
  );
}

