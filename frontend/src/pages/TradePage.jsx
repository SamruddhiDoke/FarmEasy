import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Container, Row, Col, Card, Form, Spinner, InputGroup, Alert } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import { useGeolocation } from '../hooks/useGeolocation';
import { trade as tradeApi } from '../services/api';
import { parseListResponse } from '../utils/apiHelpers';

const DEFAULT_RADIUS_KM = 100;
const DEMO_TRADES = [
  {
    id: 1,
    cropName: 'Wheat (Demo)',
    pricePerUnit: 25,
    unit: 'kg',
    quantity: '500 kg',
    location: 'Demo Market',
    imageUrl: 'https://via.placeholder.com/400x200?text=Wheat',
  },
  {
    id: 2,
    cropName: 'Rice (Demo)',
    pricePerUnit: 30,
    unit: 'kg',
    quantity: '300 kg',
    location: 'Demo Market',
    imageUrl: 'https://via.placeholder.com/400x200?text=Rice',
  },
];
const isDev = import.meta.env.DEV;

export default function TradePage() {
  const { t } = useTranslation();
  const { lat, lon } = useGeolocation();
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [radiusKm, setRadiusKm] = useState('');
  const [fetchError, setFetchError] = useState(null);

  const hasLocation = typeof lat === 'number' && typeof lon === 'number' && !Number.isNaN(lat) && !Number.isNaN(lon);
  const radiusValue = radiusKm.trim() === '' ? null : parseFloat(radiusKm);
  const effectiveRadius = hasLocation
    ? (radiusValue != null && !Number.isNaN(radiusValue) && radiusValue > 0 ? radiusValue : DEFAULT_RADIUS_KM)
    : null;

  useEffect(() => {
    setLoading(true);
    setFetchError(null);

    const params = { search: search.trim() || undefined };
    if (hasLocation) {
      params.lat = lat;
      params.lon = lon;
      params.radiusKm = effectiveRadius;
    }

    if (isDev) {
      console.log('Trade fetch — Lat:', lat, 'Lng:', lon, 'Radius:', effectiveRadius, 'Params:', params);
    }

    tradeApi
      .listPublic(params)
      .then((res) => {
        if (isDev) console.log('Trade API response:', res.data);
        const items = parseListResponse(res);
        setList(items);
        setFetchError(null);
      })
      .catch((err) => {
        console.error('Trade API error:', err?.response?.data ?? err.message);
        // Fallback to demo data so the page works at testing level
        setList(DEMO_TRADES);
        setFetchError(err?.response?.data?.message ?? err.message ?? 'Failed to load crop listings, showing demo data');
      })
      .finally(() => setLoading(false));
  }, [lat, lon, hasLocation, search, effectiveRadius]);

  return (
    <Container className="py-5 page-container">
      <h2 className="section-title mb-4">{t('trade.title')}</h2>
      <Row className="mb-3">
        <Col md={6}>
          <Form.Control
            placeholder={t('schemes.search')}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </Col>
        <Col md={3}>
          <InputGroup>
            <Form.Control
              type="number"
              min="0"
              step="1"
              placeholder={t('equipment.radiusKm')}
              value={radiusKm}
              onChange={(e) => setRadiusKm(e.target.value)}
            />
            <InputGroup.Text>km</InputGroup.Text>
          </InputGroup>
          {hasLocation && (
            <Form.Text className="text-muted">{t('equipment.radiusKm')} (default {DEFAULT_RADIUS_KM} km)</Form.Text>
          )}
        </Col>
        <Col md={3}>
          {hasLocation && <span className="text-success">{t('equipment.nearby')}</span>}
        </Col>
      </Row>

      {!hasLocation && (
        <Alert variant="info" className="mb-3 small">
          {t('equipment.showingAllListings')}
        </Alert>
      )}
      {fetchError && (
        <Alert variant="warning" className="mb-3">
          {fetchError}
        </Alert>
      )}

      {loading ? (
        <div className="loading-spinner">
          <Spinner animation="border" variant="primary" />
        </div>
      ) : (
        <Row xs={1} md={2} lg={3} className="g-4">
          {list.map((item) => (
            <Col key={item.id}>
              <Card className="h-100 card-modern border-0">
                {item.imageUrl && (
                  <Card.Img variant="top" src={item.imageUrl} style={{ height: 160, objectFit: 'cover' }} alt="" />
                )}
                <Card.Body>
                  <Card.Title>{item.cropName}</Card.Title>
                  <Card.Text>₹{item.pricePerUnit}/{item.unit} • {item.quantity}</Card.Text>
                  <Card.Text className="small">{item.location}</Card.Text>
                  {item.distanceKm != null && (
                    <Card.Text className="small">{item.distanceKm.toFixed(1)} km away</Card.Text>
                  )}
                  <Link to={`/trade/${item.id}`} className="btn btn-primary btn-sm">
                    {t('trade.viewAndContact')}
                  </Link>
                </Card.Body>
              </Card>
            </Col>
          ))}
          {list.length === 0 && (
            <Col>
              <p className="text-muted">{t('trade.noListed')}</p>
            </Col>
          )}
        </Row>
      )}
    </Container>
  );
}
