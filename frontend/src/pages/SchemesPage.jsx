import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Form, Spinner, Button } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import { schemes as schemesApi } from '../services/api';
import { parseListResponse } from '../utils/apiHelpers';

const STATES = ['All India', 'Maharashtra', 'Punjab', 'Karnataka', 'Gujarat', 'Rajasthan', 'North East India'];

export default function SchemesPage() {
  const { t } = useTranslation();
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [state, setState] = useState('');
  const [search, setSearch] = useState('');

  useEffect(() => {
    setLoading(true);
    schemesApi.list({ state: state || undefined, search: search || undefined })
      .then((res) => {
        const data = parseListResponse(res);
        setList(Array.isArray(data) ? data : []);
      })
      .catch(() => setList([]))
      .finally(() => setLoading(false));
  }, [state, search]);

  return (
    <Container className="py-5 page-container">
      <h2 className="section-title mb-4">{t('schemes.filterByState')}</h2>
      <Row className="mb-4">
        <Col md={4} className="mb-2">
          <Form.Select value={state} onChange={(e) => setState(e.target.value)}>
            <option value="">{t('schemes.allStates')}</option>
            {STATES.map((s) => (
              <option key={s} value={s}>{s}</option>
            ))}
          </Form.Select>
        </Col>
        <Col md={6} className="mb-2">
          <Form.Control
            placeholder={t('schemes.search')}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </Col>
      </Row>

      {loading ? (
        <div className="loading-spinner">
          <Spinner animation="border" variant="primary" />
        </div>
      ) : (
        <>
          <Row xs={1} md={2} lg={3} className="g-4">
            {list.map((item) => (
              <Col key={item.id}>
                <Card className="h-100 scheme-card border-0 shadow-sm">
                  <Card.Body className="p-4">
                    <Card.Title className="text-primary fw-bold mb-2">{item.title}</Card.Title>
                    <Card.Text className="text-muted small">{item.summary}</Card.Text>
                    {item.eligibility && (
                      <p className="small mb-1">
                        <strong>{t('schemes.eligibility')}:</strong> {item.eligibility}
                      </p>
                    )}
                    {item.timeline && (
                      <p className="small mb-1">
                        <strong>{t('schemes.timeline')}:</strong> {item.timeline}
                      </p>
                    )}
                    {item.state && <span className="badge bg-primary me-1 mb-2">{item.state}</span>}
                    {item.officialUrl && (
                      <a
                        href={item.officialUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="btn btn-outline-primary btn-sm mt-2"
                      >
                        {t('schemes.officialSite')}
                      </a>
                    )}
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>

          {list.length === 0 && (
            <div className="empty-state">
              <p className="text-muted">{t('schemes.noFound')}</p>
            </div>
          )}

          <div className="text-center mt-5">
            <Button
              as="a"
              href="https://www.india.gov.in/topics/agriculture"
              target="_blank"
              rel="noopener noreferrer"
              variant="primary"
              size="lg"
              className="px-5 py-3 rounded-pill fw-bold"
            >
              {t('schemes.exploreMore')}
            </Button>
          </div>
        </>
      )}
    </Container>
  );
}
