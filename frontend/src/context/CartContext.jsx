import React, { createContext, useContext, useMemo, useState } from 'react';

const CartContext = createContext(null);

const STORAGE_KEY = 'frameasy_cart_v1';

function safeParse(json) {
  try {
    return JSON.parse(json);
  } catch {
    return null;
  }
}

export function CartProvider({ children }) {
  const [items, setItems] = useState(() => safeParse(localStorage.getItem(STORAGE_KEY)) || []);

  const persist = (next) => {
    setItems(next);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
  };

  const addItem = (item, qty = 1) => {
    const q = Math.max(1, Number(qty) || 1);
    const next = [...items];
    const idx = next.findIndex((x) => x.type === item.type && x.id === item.id);
    if (idx >= 0) next[idx] = { ...next[idx], qty: next[idx].qty + q };
    else next.push({ ...item, qty: q });
    persist(next);
  };

  const updateQty = (type, id, qty) => {
    const q = Number(qty);
    const next = items
      .map((x) => (x.type === type && x.id === id ? { ...x, qty: q } : x))
      .filter((x) => Number.isFinite(x.qty) && x.qty > 0);
    persist(next);
  };

  const removeItem = (type, id) => {
    persist(items.filter((x) => !(x.type === type && x.id === id)));
  };

  const clear = () => persist([]);

  const totals = useMemo(() => {
    const subtotal = items.reduce((sum, x) => sum + (Number(x.price) || 0) * (Number(x.qty) || 0), 0);
    return { subtotal };
  }, [items]);

  return (
    <CartContext.Provider value={{ items, addItem, updateQty, removeItem, clear, totals }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error('useCart must be used within CartProvider');
  return ctx;
}

