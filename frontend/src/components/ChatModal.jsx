import React, { useState, useEffect, useRef } from 'react';
import { Modal, Form, Button, Spinner } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { chat as chatApi } from '../services/api';

// Simple REST-based chat (polling). For real-time, connect to WebSocket /app/chat/{receiverId}
export default function ChatModal({ show, onHide, otherUserId, otherUserName, relatedType, relatedId }) {
  const { t } = useTranslation();
  const { user } = useAuth();
  const [messages, setMessages] = useState([]);
  const [content, setContent] = useState('');
  const [loading, setLoading] = useState(false);
  const [sending, setSending] = useState(false);
  const bottomRef = useRef(null);

  useEffect(() => {
    if (!show || !user || !otherUserId) return;
    setLoading(true);
    chatApi.getConversation(otherUserId).then((res) => {
      setMessages(res.data?.data ?? []);
    }).catch(() => setMessages([])).finally(() => setLoading(false));
  }, [show, user, otherUserId]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const send = () => {
    if (!content.trim() || !user) return;
    setSending(true);
    chatApi
      .send({
        receiverId: otherUserId,
        content: content.trim(),
        relatedType,
        relatedId,
      })
      .then((res) => {
        if (res.data?.data) setMessages((m) => [...m, res.data.data]);
        setContent('');
      })
      .finally(() => setSending(false));
  };

  return (
    <Modal show={show} onHide={onHide} size="lg" centered>
      <Modal.Header closeButton>{t('common.chatWith')} {otherUserName}</Modal.Header>
      <Modal.Body>
        {loading ? (
          <Spinner />
        ) : (
          <div style={{ maxHeight: 350, overflowY: 'auto' }}>
            {messages.map((msg) => (
              <div
                key={msg.id}
                className={`p-2 mb-2 rounded ${msg.senderId === user?.id ? 'bg-primary text-white ms-4' : 'bg-light me-4'}`}
              >
                <small>{msg.senderName}: </small>{msg.content}
              </div>
            ))}
            <div ref={bottomRef} />
          </div>
        )}
        <Form.Group className="d-flex gap-2 mt-2">
          <Form.Control
            value={content}
            onChange={(e) => setContent(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && send()}
            placeholder={t('common.typeMessage')}
          />
          <Button onClick={send} disabled={sending || !content.trim()}>{t('ai.send')}</Button>
        </Form.Group>
      </Modal.Body>
    </Modal>
  );
}
