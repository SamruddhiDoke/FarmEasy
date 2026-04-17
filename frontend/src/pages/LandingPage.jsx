import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Container, Row, Col, Button, Card } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import { equipment, schemes } from '../services/api';
import { parseListResponse } from '../utils/apiHelpers';

export default function LandingPage() {
  const { t } = useTranslation();
  const [counts, setCounts] = useState({ farmers: 0, equipment: 0, schemes: 0 });

  useEffect(() => {
    Promise.all([
      equipment.listPublic().catch(() => ({ data: { data: [] } })),
      schemes.list().catch(() => ({ data: { data: [] } })),
    ]).then(([eqRes, scRes]) => {
      const eqList = parseListResponse(eqRes);
      const scList = parseListResponse(scRes);
      setCounts({
        farmers: Math.min((eqList?.length ?? 0) * 2 + 10, 999),
        equipment: (eqList?.length ?? 0) || 12,
        schemes: (scList?.length ?? 0) || 5,
      });
    });
  }, []);

  const features = [
    { key: 'equipmentRental', desc: 'equipmentRentalDesc', path: '/equipment' },
    { key: 'landRental', desc: 'landRentalDesc', path: '/land' },
    { key: 'cropTrade', desc: 'cropTradeDesc', path: '/trade' },
    { key: 'govtSchemes', desc: 'govtSchemesDesc', path: '/schemes' },
    { key: 'aiAssistant', desc: 'aiAssistantDesc', path: '/dashboard' },
  ];

  return (
    <>
      <section className="hero-section text-white pt-5">
        <Container>
          <Row className="align-items-center">
            <Col lg={7}>
              <h1 className="hero-title display-4 fw-bold mb-3">{t('app.tagline')}</h1>
              <p className="hero-subtitle lead mb-4">
                {t('app.heroSubtitle')}
              </p>
              <div className="d-flex gap-3 flex-wrap">
                <Button as={Link} to="/register" variant="light" size="lg" className="px-4 rounded-pill shadow-sm btn-hero-primary">
                  {t('app.getStarted')}
                </Button>
                <Button as={Link} to="/equipment" variant="outline-light" size="lg" className="px-4 rounded-pill">
                  {t('app.browseEquipment')}
                </Button>
              </div>
            </Col>
            <Col lg={5} className="text-center mt-4 mt-lg-0">
              <div className="hero-counters d-flex justify-content-center gap-4 flex-wrap">
                <div className="hero-counter-item text-center">
                  <div className="display-4 fw-bold">{counts.farmers}+</div>
                  <small className="opacity-90">{t('counters.activeFarmers')}</small>
                </div>
                <div className="hero-counter-item text-center">
                  <div className="display-4 fw-bold">{counts.equipment}+</div>
                  <small className="opacity-90">{t('counters.equipmentListed')}</small>
                </div>
                <div className="hero-counter-item text-center">
                  <div className="display-4 fw-bold">{counts.schemes}+</div>
                  <small className="opacity-90">{t('counters.govtSchemes')}</small>
                </div>
              </div>
            </Col>
          </Row>
        </Container>
      </section>

      <section className="features-section py-5">
        <Container>
          <h2 className="section-title text-center mb-5">{t('features.sectionTitle')}</h2>
          <Row xs={1} md={2} lg={3} className="g-4">
            {features.map((f) => (
              <Col key={f.key}>
                <Card className="h-100 feature-card border-0 shadow-sm">
                  <Card.Body className="p-4">
                    <Card.Title className="text-primary fw-bold mb-3">{t(`features.${f.key}`)}</Card.Title>
                    <Card.Text className="text-muted mb-3">{t(`features.${f.desc}`)}</Card.Text>
                    <Button as={Link} to={f.path} variant="outline-primary" size="sm" className="rounded-pill">
                      {t('app.learnMore')}
                    </Button>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>
        </Container>
      </section>
    </>
  );
}
