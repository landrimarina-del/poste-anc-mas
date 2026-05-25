import { httpClient } from './httpClient';

export const authApi = {
  login: (username, password) =>
    httpClient.request('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password })
    }),
  logout: () =>
    httpClient.request('/api/v1/auth/logout', {
      method: 'POST'
    }),
  me: () =>
    httpClient.request('/api/v1/auth/me', {
      method: 'GET'
    })
};
