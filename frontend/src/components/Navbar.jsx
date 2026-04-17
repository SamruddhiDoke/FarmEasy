import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Navbar as BSNavbar, Nav, Container, Dropdown } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export default function Navbar() {
  const { t } = useTranslation();
  const { user, logout } = useAuth();
  const { items } = useCart();
  const navigate = useNavigate();
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener('scroll', onScroll);
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <BSNavbar
      bg="primary-green"
      variant="dark"
      expand="lg"
      fixed="top"
      className={`navbar-modern ${scrolled ? 'shadow' : ''}`}
    >
      <Container>
        <BSNavbar.Brand as={Link} to="/" className="fw-bold">{t('app.title')}</BSNavbar.Brand>
        <BSNavbar.Toggle aria-controls="navbar-nav" />
        <BSNavbar.Collapse id="navbar-nav">
          <Nav className="me-auto">
            <Nav.Link as={Link} to="/">{t('app.home')}</Nav.Link>
            <Nav.Link as={Link} to="/schemes">{t('app.schemes')}</Nav.Link>
            <Nav.Link as={Link} to="/equipment">{t('app.products')}</Nav.Link>
            <Nav.Link as={Link} to="/trade">{t('app.trade')}</Nav.Link>
          </Nav>
          <Nav>
            {user ? (
              <>
                <Nav.Link as={Link} to="/dashboard">{t('dashboard.welcome')}, {user.name}</Nav.Link>
                <Nav.Link as={Link} to="/cart">Cart ({items.length})</Nav.Link>
                {user.roles?.includes('ROLE_ADMIN') && (
                  <Nav.Link as={Link} to="/admin">{t('admin.panel')}</Nav.Link>
                )}
                <Dropdown align="end">
                  <Dropdown.Toggle variant="outline-light" id="profile-dropdown">
                    {t('app.profile')}
                  </Dropdown.Toggle>
                  <Dropdown.Menu>
                    <Dropdown.Item as={Link} to="/dashboard">{t('dashboard.welcome')}</Dropdown.Item>
                    <Dropdown.Item as={Link} to="/profile">{t('app.profile')}</Dropdown.Item>
                    <Dropdown.Divider />
                    <Dropdown.Item onClick={handleLogout}>{t('app.logout')}</Dropdown.Item>
                  </Dropdown.Menu>
                </Dropdown>
              </>
            ) : (
              <>
                <Nav.Link as={Link} to="/login">{t('app.login')}</Nav.Link>
                <Nav.Link as={Link} to="/register" className="btn btn-accent btn-sm ms-1 px-3">
                  {t('app.register')}
                </Nav.Link>
              </>
            )}
          </Nav>
        </BSNavbar.Collapse>
      </Container>
    </BSNavbar>
  );
}
