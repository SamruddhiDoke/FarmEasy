import { useState, useEffect } from 'react';

const defaultOptions = {
  enableHighAccuracy: true,
  timeout: 10000,
  maximumAge: 300000,
};

export function useGeolocation(options = defaultOptions) {
  const [position, setPosition] = useState({ lat: null, lon: null, error: null });

  useEffect(() => {
    if (typeof navigator === 'undefined' || !navigator.geolocation) {
      setPosition((p) => ({ ...p, error: 'Geolocation not supported' }));
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setPosition({
          lat: pos.coords.latitude,
          lon: pos.coords.longitude,
          error: null,
        });
      },
      (err) => {
        console.log('Location error:', err.code, err.message);
        setPosition((p) => ({ ...p, error: err.message }));
      },
      options
    );
  }, []);

  return position;
}
