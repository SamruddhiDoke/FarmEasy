import React, { createContext, useContext, useState, useEffect } from 'react';
import { auth as authApi } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const u = localStorage.getItem('frameasy_user');
      return u ? JSON.parse(u) : null;
    } catch {
      return null;
    }
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('frameasy_token');
    if (!token) {
      setLoading(false);
      return;
    }
    authApi.me()
      .then((res) => {
        if (res.data?.success && res.data?.data) {
          setUser(res.data.data);
          localStorage.setItem('frameasy_user', JSON.stringify(res.data.data));
        } else {
          // If backend says unauthorized/invalid, clear session.
          // Otherwise keep the last known user for demo stability.
          const message = res.data?.message || '';
          if (message.toLowerCase().includes('unauthorized')) {
            localStorage.removeItem('frameasy_token');
            localStorage.removeItem('frameasy_user');
            setUser(null);
          }
        }
      })
      .catch((err) => {
        // If token is invalid/expired (401), log out.
        // If backend is temporarily down, don't force logout; keep cached user.
        if (err?.response?.status === 401) {
          localStorage.removeItem('frameasy_token');
          localStorage.removeItem('frameasy_user');
          setUser(null);
        }
      })
      .finally(() => setLoading(false));
  }, []);

  const login = (token, userData) => {
    localStorage.setItem('frameasy_token', token);
    localStorage.setItem('frameasy_user', JSON.stringify(userData));
    setUser(userData);
  };

  const logout = () => {
    localStorage.removeItem('frameasy_token');
    localStorage.removeItem('frameasy_user');
    setUser(null);
  };

  const updateUser = (userData) => {
    setUser((prev) => ({ ...prev, ...userData }));
    localStorage.setItem('frameasy_user', JSON.stringify({ ...user, ...userData }));
  };

  const isAdmin = () => user?.roles?.includes('ROLE_ADMIN');
  const isFarmer = () => user?.roles?.includes('ROLE_FARMER');

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, updateUser, isAdmin, isFarmer }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
