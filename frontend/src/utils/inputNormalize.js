/** Match backend InputNormalize for auth fields */
export function normalizeEmail(value) {
  return (value || '').trim().toLowerCase();
}

/** Strip non-digits so pasted OTPs from email still work */
export function normalizeOtpInput(value) {
  return (value || '').replace(/\D/g, '').slice(0, 6);
}
