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
  counters: () =>
    httpClient.request('/api/v1/tasks/counters', { method: 'GET' }),
  getById: (taskId) =>
    httpClient.request(`/api/v1/tasks/${taskId}`, {
      method: 'GET'
    }),
  accept: (taskId) =>
    httpClient.request(`/api/v1/tasks/${taskId}/accept`, {
      method: 'POST'
    }),
  getSavedFilters: () =>
    httpClient.request('/api/v1/tasks/filters/saved', { method: 'GET' }),
  saveFilter: (filterName, filterJson) =>
    httpClient.request('/api/v1/tasks/filters/saved', {
      method: 'POST',
      body: JSON.stringify({ filterName, filterJson })
    }),
  deleteFilter: (id) =>
    httpClient.request(`/api/v1/tasks/filters/saved/${id}`, { method: 'DELETE' })
};
