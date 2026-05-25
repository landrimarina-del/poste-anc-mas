import { httpClient } from './httpClient';

function buildQueryString(params = {}) {
  const search = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return;
    }
    search.set(key, String(value));
  });

  const query = search.toString();
  return query ? `?${query}` : '';
}

export const practicesApi = {
  list: (params) =>
    httpClient.request(`/api/v1/practices${buildQueryString(params)}`, {
      method: 'GET'
    }),
  exportExcel: async (params = {}) => {
    const response = await fetch(`/api/v1/practices/export${buildQueryString(params)}`, {
      method: 'GET',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error(`Errore export Excel (${response.status})`);
    }

    return response.blob();
  },
  detail: (practiceId) =>
    httpClient.request(`/api/v1/practices/${practiceId}`, {
      method: 'GET'
    }),
  history: (practiceId) =>
    httpClient.request(`/api/v1/practices/${practiceId}/history`, {
      method: 'GET'
    }),
  states: (practiceId) =>
    httpClient.request(`/api/v1/practices/${practiceId}/states`, {
      method: 'GET'
    }),
  relatedActions: (practiceId) =>
    httpClient.request(`/api/v1/practices/${practiceId}/related-actions`, {
      method: 'GET'
    })
};
