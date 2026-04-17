/**
 * Safely extract list from backend API response.
 * Handles both wrapped { success, data: [...] } and plain array.
 * @param {import('axios').AxiosResponse} res - Axios response (res.data is the body).
 * @returns {Array} Never null; empty array if missing or not an array.
 */
export function parseListResponse(res) {
  const raw = res?.data;
  const data = raw?.data ?? raw;
  return Array.isArray(data) ? data : [];
}
