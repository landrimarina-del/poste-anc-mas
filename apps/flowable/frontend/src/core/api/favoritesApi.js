import { httpClient } from './httpClient';

export const favoritesApi = {
  list: () =>
    httpClient.request('/api/v1/favorites', {
      method: 'GET'
    }),
  create: (payload) =>
    httpClient.request('/api/v1/favorites', {
      method: 'POST',
      body: JSON.stringify(payload)
    }),
  update: (favoriteId, payload) =>
    httpClient.request(`/api/v1/favorites/${favoriteId}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    }),
  remove: (favoriteId) =>
    httpClient.request(`/api/v1/favorites/${favoriteId}`, {
      method: 'DELETE'
    })
};
