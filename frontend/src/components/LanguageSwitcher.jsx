import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { ButtonGroup, Dropdown } from 'react-bootstrap';

const LANGS = [
  { code: 'en', label: 'EN' },
  { code: 'hi', label: 'HI' },
  { code: 'mr', label: 'MR' },
];

export default function LanguageSwitcher() {
  const { i18n } = useTranslation();
  const [open, setOpen] = useState(false);

  const setLang = (code) => {
    i18n.changeLanguage(code);
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem('frameasy_lang', code);
    }
    setOpen(false);
  };

  const current = LANGS.find((l) => l.code === i18n.language) || LANGS[0];

  return (
    <div className="floating-lang" aria-label="Language switcher">
      <Dropdown show={open} onToggle={setOpen} drop="up" align="end">
        <Dropdown.Toggle
          variant="success"
          id="lang-switcher-toggle"
          className="rounded-circle shadow-sm border-0 lang-toggle-btn"
          style={{ width: 52, height: 52 }}
        >
          {current.label}
        </Dropdown.Toggle>
        <Dropdown.Menu className="shadow">
          {LANGS.map((l) => (
            <Dropdown.Item
              key={l.code}
              onClick={() => setLang(l.code)}
              active={i18n.language === l.code}
            >
              {l.label}
            </Dropdown.Item>
          ))}
        </Dropdown.Menu>
      </Dropdown>
    </div>
  );
}
