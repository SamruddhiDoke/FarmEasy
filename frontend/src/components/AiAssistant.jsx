import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Button, Modal, Form, Spinner } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';
import { ai } from '../services/api';

export default function AiAssistant() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const [show, setShow] = useState(false);
  const [message, setMessage] = useState('');
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);

  const send = async () => {
    if (!message.trim() || !user) return;
    const userMsg = { role: 'user', text: message };
    setMessages((m) => [...m, userMsg]);
    setMessage('');
    setLoading(true);
    try {
      const res = await ai.chat(message);
      const reply = res.data?.data?.reply || t('ai.farmingOnly');
      setMessages((m) => [...m, { role: 'assistant', text: reply }]);
    } catch {
      setMessages((m) => [...m, { role: 'assistant', text: t('ai.errorResponse') }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Button
        className="floating-ai rounded-circle shadow btn-accent"
        style={{ width: 56, height: 56 }}
        onClick={() => setShow(true)}
        title={t('features.aiAssistant')}
      >
        🤖
      </Button>
      <Modal show={show} onHide={() => setShow(false)} centered size="lg">
        <Modal.Header closeButton className="bg-primary-green text-white">
          <Modal.Title>{t('features.aiAssistant')}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p className="small text-muted">{t('ai.farmingOnly')}</p>
          <div className="mb-3" style={{ maxHeight: 300, overflowY: 'auto' }}>
            {messages.length === 0 && (
              <p className="text-muted">{t('ai.placeholder')}</p>
            )}
            {messages.map((msg, i) => (
              <div
                key={i}
                className={`p-2 mb-2 rounded ${msg.role === 'user' ? 'bg-light ms-4' : 'bg-primary bg-opacity-10 me-4'}`}
              >
                {msg.text}
              </div>
            ))}
            {loading && <Spinner animation="border" size="sm" />}
          </div>
          <Form.Group className="d-flex gap-2">
            <Form.Control
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && send()}
              placeholder={t('ai.placeholder')}
              disabled={!user}
            />
            <Button onClick={send} disabled={loading || !user} className="btn-accent">
              {t('ai.send')}
            </Button>
          </Form.Group>
          {!user && <p className="small text-warning mt-2">{t('ai.loginToUse')}</p>}
        </Modal.Body>
      </Modal>
    </>
  );
}
