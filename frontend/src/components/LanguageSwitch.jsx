import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Button, Dropdown } from 'react-bootstrap';

const LANGS = [
  { code: 'en', label: 'English' },
  { code: 'hi', label: 'हिन्दी' },
  { code: 'mr', label: 'मराठी' },
];

export default function LanguageSwitch() {
  const { i18n } = useTranslation();
  const [open, setOpen] = useState(false);

  const setLang = (code) => {
    i18n.changeLanguage(code);
    localStorage.setItem('frameasy_lang', code);
    setOpen(false);
  };

  const current = LANGS.find((l) => l.code === i18n.language) || LANGS[0];

  return (
    <div className="floating-lang">
      <Dropdown show={open} onToggle={setOpen}>
        <Dropdown.Toggle
          variant="success"
          id="lang-dropdown"
          className="rounded-circle shadow"
          style={{ width: 48, height: 48 }}
        >
          {current.code.toUpperCase()}
        </Dropdown.Toggle>
        <Dropdown.Menu>
          {LANGS.map((l) => (
            <Dropdown.Item key={l.code} onClick={() => setLang(l.code)} active={i18n.language === l.code}>
              {l.label}
            </Dropdown.Item>
          ))}
        </Dropdown.Menu>
      </Dropdown>
    </div>
  );
}
