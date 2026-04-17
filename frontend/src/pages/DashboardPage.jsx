import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Container, Row, Col, Card, ListGroup, Spinner } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { equipment, land, trade, agreements } from '../services/api';
import { parseListResponse } from '../utils/apiHelpers';

export default function DashboardPage() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const [equipmentList, setEquipmentList] = useState([]);
  const [landList, setLandList] = useState([]);
  const [tradeList, setTradeList] = useState([]);
  const [agreementList, setAgreementList] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) return;
    Promise.all([
      equipment.myList().catch(() => ({ data: null })),
      land.myList().catch(() => ({ data: null })),
      trade.myList().catch(() => ({ data: null })),
      agreements.myList().catch(() => ({ data: null })),
    ]).then(([e, l, tr, a]) => {
      setEquipmentList(parseListResponse(e) ?? []);
      setLandList(parseListResponse(l) ?? []);
      setTradeList(parseListResponse(tr) ?? []);
      setAgreementList(parseListResponse(a) ?? []);
    }).finally(() => setLoading(false));
  }, [user]);

  if (!user) return null;

  const modules = [
    { title: t('dashboard.equipment'), path: '/equipment', list: equipmentList, listPath: '/dashboard/list-equipment' },
    { title: t('dashboard.land'), path: '/land', list: landList, listPath: '/dashboard/list-land' },
    { title: t('dashboard.trade'), path: '/trade', list: tradeList, listPath: '/dashboard/list-trade' },
  ];

  return (
    <Container className="py-5 page-container">
      <h2 className="section-title mb-2">{t('dashboard.welcome')}, {user.name}</h2>
      <p className="text-muted mb-4">{t('dashboard.profileCompletion')}: <Link to="/profile">{t('app.profile')}</Link></p>

      {loading ? (
        <div className="loading-spinner">
          <Spinner animation="border" variant="primary" />
        </div>
      ) : (
        <Row>
          {modules.map((m) => (
            <Col md={4} key={m.title} className="mb-4">
              <Card className="card-modern border-0">
                <Card.Header className="d-flex justify-content-between align-items-center">
                  <span>{m.title}</span>
                  <span>
                    <Link to={m.listPath} className="me-2">{t('dashboard.listNew')}</Link>
                    <Link to={m.path}>{t('dashboard.browse')}</Link>
                  </span>
                </Card.Header>
                <ListGroup variant="flush">
                  {(m.list || []).slice(0, 3).map((item) => (
                    <ListGroup.Item key={item.id}>
                      {item.title || item.cropName}
                      {item.distanceKm != null && (
                        <small className="text-muted ms-2">({item.distanceKm.toFixed(1)} km)</small>
                      )}
                    </ListGroup.Item>
                  ))}
                  {(!m.list || m.list.length === 0) && (
                    <ListGroup.Item className="text-muted">{t('dashboard.noListings')}</ListGroup.Item>
                  )}
                </ListGroup>
                <Card.Footer>
                  <Link to={m.path}>{t('dashboard.viewAll')}</Link>
                </Card.Footer>
              </Card>
            </Col>
          ))}
        </Row>
      )}

      <Card className="mt-4 card-modern border-0">
        <Card.Header>{t('dashboard.agreements')}</Card.Header>
        <ListGroup variant="flush">
          {agreementList.slice(0, 5).map((a) => (
            <ListGroup.Item key={a.id}>
              {a.agreementType} #{a.referenceId} - ₹{a.finalPrice}
              {a.downloadUrl && (
                <a
                  href={a.downloadUrl.startsWith('http') ? a.downloadUrl : `${window.location.origin}${a.downloadUrl}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="ms-2"
                >
                  {t('dashboard.downloadPdf')}
                </a>
              )}
            </ListGroup.Item>
          ))}
          {agreementList.length === 0 && (
            <ListGroup.Item className="text-muted">{t('dashboard.noAgreements')}</ListGroup.Item>
          )}
        </ListGroup>
      </Card>
    </Container>
  );
}
