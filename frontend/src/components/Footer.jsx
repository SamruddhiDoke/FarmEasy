import React from 'react';
import { Link } from 'react-router-dom';
import { Container, Row, Col } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';

export default function Footer() {
  const { t } = useTranslation();

  return (
    <footer className="footer-farm-easy mt-auto">
      <Container>
        <Row className="align-items-center">
          <Col md={6} className="text-center text-md-start mb-2 mb-md-0">
            <Link to="/" className="fw-bold text-white text-decoration-none fs-5">
              {t('app.title')}
            </Link>
            <span className="ms-2 opacity-75">— Farming Made Easy for Everyone</span>
          </Col>
          <Col md={6} className="text-center text-md-end">
            <Link to="/schemes" className="me-3">{t('app.schemes')}</Link>
            <Link to="/equipment" className="me-3">{t('app.products')}</Link>
            <Link to="/trade" className="me-3">{t('app.trade')}</Link>
          </Col>
        </Row>
      </Container>
    </footer>
  );
}
