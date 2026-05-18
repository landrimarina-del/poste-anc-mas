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

export const tasksApi = {
  list: (params) =>
    httpClient.request(`/api/v1/tasks${buildQueryString(params)}`, {
      method: 'GET'
    }),
  accept: (taskId) =>
    httpClient.request(`/api/v1/tasks/${taskId}/accept`, {
      method: 'POST'
    })
};
