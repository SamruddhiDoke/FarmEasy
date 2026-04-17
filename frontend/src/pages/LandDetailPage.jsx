import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Container, Button, Card, Modal, Form, Spinner, Alert } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { land as landApi, agreements } from '../services/api';
import ChatModal from '../components/ChatModal';

export default function LandDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const { user } = useAuth();
  const [item, setItem] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showChat, setShowChat] = useState(false);
  const [showDeal, setShowDeal] = useState(false);
  const [dealForm, setDealForm] = useState({ buyerName: '', finalPrice: '', dueDate: '', terms: '', otp: '' });
  const [otpSent, setOtpSent] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    landApi.getById(id).then((res) => setItem(res.data?.data)).catch(() => setItem(null)).finally(() => setLoading(false));
  }, [id]);

  const handleSendOtp = () => {
    if (!user?.email) return;
    setError('');
    agreements.sendOtp(user.email).then(() => setOtpSent(true)).catch(() => setError('Failed to send OTP'));
  };

  const handleConfirmDeal = (e) => {
    e.preventDefault();
    if (!user || !item) return;
    setError('');
    setSubmitting(true);
    agreements.create({
      agreementType: 'LAND',
      referenceId: item.id,
      buyerName: dealForm.buyerName,
      finalPrice: parseFloat(dealForm.finalPrice),
      dueDate: dealForm.dueDate || null,
      terms: dealForm.terms,
      otp: dealForm.otp,
    }).then((res) => {
      if (res.data?.success) { setShowDeal(false); navigate('/dashboard'); }
      else setError(res.data?.message || 'Failed');
    }).catch((err) => setError(err.response?.data?.message || 'Failed')).finally(() => setSubmitting(false));
  };

  if (loading || !item) return <Container className="py-4">{loading ? <Spinner /> : <p>{t('land.notFound')}</p>}</Container>;
  const isOwner = user && user.id === item.userId;

  return (
    <Container className="py-4">
      <Card>
        {item.imageUrl && <Card.Img variant="top" src={item.imageUrl} style={{ maxHeight: 300, objectFit: 'cover' }} />}
        <Card.Body>
          <Card.Title>{item.title}</Card.Title>
          <Card.Text>{item.description}</Card.Text>
          <p><strong>₹{item.pricePerMonth}</strong> / month • {item.area}</p>
          <p className="text-muted">{item.location}</p>
          <p className="small">Owner: {item.ownerName}</p>
          {user && !isOwner && (
            <>
              <Button className="me-2" onClick={() => setShowChat(true)}>{t('equipment.contact')}</Button>
              <Button variant="success" onClick={() => { setShowDeal(true); setOtpSent(false); setDealForm({ buyerName: user.name, finalPrice: item.pricePerMonth, dueDate: '', terms: '', otp: '' }); }}>
                {t('equipment.confirmDeal')}
              </Button>
            </>
          )}
        </Card.Body>
      </Card>
      <ChatModal show={showChat} onHide={() => setShowChat(false)} otherUserId={item.userId} otherUserName={item.ownerName} relatedType="LAND" relatedId={item.id} />
      <Modal show={showDeal} onHide={() => setShowDeal(false)}>
        <Modal.Header closeButton>{t('equipment.confirmDeal')}</Modal.Header>
        <Modal.Body>
          {error && <Alert variant="danger">{error}</Alert>}
          <Form onSubmit={handleConfirmDeal}>
            <Form.Group className="mb-2"><Form.Label>{t('agreement.buyerName')}</Form.Label><Form.Control value={dealForm.buyerName} onChange={(e) => setDealForm((f) => ({ ...f, buyerName: e.target.value }))} required /></Form.Group>
            <Form.Group className="mb-2"><Form.Label>{t('agreement.finalPrice')}</Form.Label><Form.Control type="number" step="0.01" value={dealForm.finalPrice} onChange={(e) => setDealForm((f) => ({ ...f, finalPrice: e.target.value }))} required /></Form.Group>
            <Form.Group className="mb-2"><Form.Label>{t('agreement.dueDate')}</Form.Label><Form.Control type="date" value={dealForm.dueDate} onChange={(e) => setDealForm((f) => ({ ...f, dueDate: e.target.value }))} /></Form.Group>
            <Form.Group className="mb-2"><Form.Label>{t('agreement.terms')}</Form.Label><Form.Control as="textarea" value={dealForm.terms} onChange={(e) => setDealForm((f) => ({ ...f, terms: e.target.value }))} /></Form.Group>
            {!otpSent ? <Button type="button" onClick={handleSendOtp}>{t('equipment.sendOtp')}</Button> : <>
              <Form.Group className="mb-2"><Form.Label>{t('auth.enterOtp')}</Form.Label><Form.Control value={dealForm.otp} onChange={(e) => setDealForm((f) => ({ ...f, otp: e.target.value }))} maxLength={6} required /></Form.Group>
              <Button type="submit" disabled={submitting}>{submitting ? t('common.loading') : t('agreement.proceed')}</Button>
            </>}
          </Form>
        </Modal.Body>
      </Modal>
    </Container>
  );
}
