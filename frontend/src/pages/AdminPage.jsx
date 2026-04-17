import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Table, Button, Spinner, Tab, Tabs } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { admin as adminApi } from '../services/api';

export default function AdminPage() {
  const { t } = useTranslation();
  const { user, isAdmin } = useAuth();
  const [stats, setStats] = useState(null);
  const [pendingEq, setPendingEq] = useState([]);
  const [pendingLand, setPendingLand] = useState([]);
  const [pendingTrade, setPendingTrade] = useState([]);
  const [users, setUsers] = useState([]);
  const [agreements, setAgreements] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) return;
    adminApi.stats().then((res) => setStats(res.data?.data)).catch(() => {});
    adminApi.pendingEquipment().then((res) => setPendingEq(res.data?.data ?? [])).catch(() => {});
    adminApi.pendingLand().then((res) => setPendingLand(res.data?.data ?? [])).catch(() => {});
    adminApi.pendingTrade().then((res) => setPendingTrade(res.data?.data ?? [])).catch(() => {});
    adminApi.users().then((res) => setUsers(res.data?.data ?? [])).catch(() => {});
    adminApi.agreements().then((res) => setAgreements(res.data?.data ?? [])).catch(() => {});
    setLoading(false);
  }, [user]);

  if (!user || !isAdmin()) {
    return <Container className="py-4"><p>{t('admin.accessDenied')}</p></Container>;
  }

  const approve = (type, id) => {
    if (type === 'equipment') adminApi.approveEquipment(id).then(() => setPendingEq((p) => p.filter((x) => x.id !== id)));
    if (type === 'land') adminApi.approveLand(id).then(() => setPendingLand((p) => p.filter((x) => x.id !== id)));
    if (type === 'trade') adminApi.approveTrade(id).then(() => setPendingTrade((p) => p.filter((x) => x.id !== id)));
  };

  const refreshSchemes = () => {
    adminApi.refreshSchemes().then((res) => alert(res.data?.data || 'Done')).catch(() => alert('Failed'));
  };

  return (
    <Container className="py-4">
      <h2>{t('admin.panel')}</h2>
      {loading ? <Spinner /> : (
        <>
          {stats && (
            <Row className="mb-4">
              {Object.entries(stats).map(([k, v]) => (
                <Col key={k} md={2}>
                  <Card>
                    <Card.Body className="text-center">
                      <div className="display-6">{v}</div>
                      <small>{k}</small>
                    </Card.Body>
                  </Card>
                </Col>
              ))}
            </Row>
          )}
          <Button className="mb-3" onClick={refreshSchemes}>{t('admin.refreshSchemes')}</Button>
          <Tabs defaultActiveKey="equipment">
            <Tab eventKey="equipment" title="Equipment">
              <Table striped>
                <thead><tr><th>ID</th><th>Title</th><th>Owner</th><th></th></tr></thead>
                <tbody>
                  {pendingEq.map((e) => (
                    <tr key={e.id}><td>{e.id}</td><td>{e.title}</td><td>{e.ownerName}</td><td><Button size="sm" onClick={() => approve('equipment', e.id)}>{t('admin.approve')}</Button></td></tr>
                  ))}
                  {pendingEq.length === 0 && <tr><td colSpan={4}>{t('admin.noPending')}</td></tr>}
                </tbody>
              </Table>
            </Tab>
            <Tab eventKey="land" title="Land">
              <Table striped>
                <thead><tr><th>ID</th><th>Title</th><th>Owner</th><th></th></tr></thead>
                <tbody>
                  {pendingLand.map((e) => (
                    <tr key={e.id}><td>{e.id}</td><td>{e.title}</td><td>{e.ownerName}</td><td><Button size="sm" onClick={() => approve('land', e.id)}>{t('admin.approve')}</Button></td></tr>
                  ))}
                  {pendingLand.length === 0 && <tr><td colSpan={4}>{t('admin.noPending')}</td></tr>}
                </tbody>
              </Table>
            </Tab>
            <Tab eventKey="trade" title="Trade">
              <Table striped>
                <thead><tr><th>ID</th><th>Crop</th><th>Seller</th><th></th></tr></thead>
                <tbody>
                  {pendingTrade.map((e) => (
                    <tr key={e.id}><td>{e.id}</td><td>{e.cropName}</td><td>{e.sellerName}</td><td><Button size="sm" onClick={() => approve('trade', e.id)}>{t('admin.approve')}</Button></td></tr>
                  ))}
                  {pendingTrade.length === 0 && <tr><td colSpan={4}>{t('admin.noPending')}</td></tr>}
                </tbody>
              </Table>
            </Tab>
            <Tab eventKey="users" title={t('admin.users')}>
              <Table striped>
                <thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Roles</th></tr></thead>
                <tbody>
                  {users.map((u) => (
                    <tr key={u.id}>
                      <td>{u.id}</td>
                      <td>{u.name}</td>
                      <td>{u.email}</td>
                      <td>{(u.roles || []).map((r) => r.name || r).join(', ')}</td>
                    </tr>
                  ))}
                  {users.length === 0 && <tr><td colSpan={4}>{t('admin.noPending')}</td></tr>}
                </tbody>
              </Table>
            </Tab>
            <Tab eventKey="agreements" title={t('admin.agreements')}>
              <Table striped>
                <thead><tr><th>ID</th><th>Type</th><th>Reference</th><th>Buyer</th><th>Final Price</th></tr></thead>
                <tbody>
                  {agreements.map((a) => (
                    <tr key={a.id}>
                      <td>{a.id}</td>
                      <td>{a.agreementType}</td>
                      <td>{a.referenceId}</td>
                      <td>{a.buyerName}</td>
                      <td>{a.finalPrice}</td>
                    </tr>
                  ))}
                  {agreements.length === 0 && <tr><td colSpan={5}>{t('admin.noPending')}</td></tr>}
                </tbody>
              </Table>
            </Tab>
          </Tabs>
        </>
      )}
    </Container>
  );
}
