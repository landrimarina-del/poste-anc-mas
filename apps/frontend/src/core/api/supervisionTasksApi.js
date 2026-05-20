import { httpClient } from './httpClient';
import { tasksApi } from './tasksApi';

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

function isRecoverableEndpointError(error) {
  const status = error?.status;
  return status === 404 || status === 405 || status === 501;
}

function normalizeDateAsDay(value) {
  if (!value) {
    return '';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return String(value).slice(0, 10);
  }

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function normalizeTask(task) {
  return {
    taskId: task?.taskId ?? task?.id ?? '',
    practiceId: task?.practiceId ?? task?.practice?.id ?? '',
    practiceNumber: task?.practiceNumber ?? task?.practice?.practiceNumber ?? '-',
    assignmentDate: task?.assignmentDate ?? task?.assignedAt ?? task?.createdAt ?? '',
    owner: task?.owner ?? task?.ownerUser ?? task?.ownerUsername ?? '-',
    assignee: task?.assignee ?? task?.assigneeUser ?? task?.assigneeUsername ?? '-',
    taskState: task?.taskState ?? task?.state ?? '',
    activityLabel: task?.activityLabel ?? task?.taskName ?? 'Task ANC'
  };
}

function normalizeListResponse(response) {
  if (Array.isArray(response)) {
    return response.map(normalizeTask);
  }
  if (Array.isArray(response?.items)) {
    return response.items.map(normalizeTask);
  }
  return [];
}

function filterFallbackItems(items, filters) {
  return items.filter((task) => {
    const practiceNumber = String(task.practiceNumber ?? '').toLowerCase();
    const owner = String(task.owner ?? '').toLowerCase();
    const assignee = String(task.assignee ?? '').toLowerCase();
    const assignmentDate = normalizeDateAsDay(task.assignmentDate);

    if (filters.practiceNumber && !practiceNumber.includes(String(filters.practiceNumber).toLowerCase())) {
      return false;
    }

    if (filters.owner && !owner.includes(String(filters.owner).toLowerCase())) {
      return false;
    }

    if (filters.assignee && !assignee.includes(String(filters.assignee).toLowerCase())) {
      return false;
    }

    if (filters.assignmentDate && assignmentDate !== filters.assignmentDate) {
      return false;
    }

    return true;
  });
}

async function withFallback(requestPrimary, requestFallback) {
  try {
    return await requestPrimary();
  } catch (error) {
    if (!isRecoverableEndpointError(error)) {
      throw error;
    }
    return requestFallback();
  }
}

export const supervisionTasksApi = {
  list: async (filters) => {
    return withFallback(
      async () => {
        const response = await httpClient.request(
          `/api/v1/supervision/tasks${buildQueryString(filters)}`,
          { method: 'GET' }
        );

        return {
          items: normalizeListResponse(response),
          fallbackMode: false
        };
      },
      async () => {
        const response = await tasksApi.list({
          practiceNumber: filters?.practiceNumber
        });

        const normalized = normalizeListResponse(response);
        return {
          items: filterFallbackItems(normalized, filters ?? {}),
          fallbackMode: true
        };
      }
    );
  },

  reassignGroup: async (taskId, reason) => {
    return withFallback(
      () =>
        httpClient.request(`/api/v1/supervision/tasks/${taskId}/reassign-group`, {
          method: 'POST',
          body: JSON.stringify(reason ? { reason } : {})
        }),
      () =>
        httpClient.request(`/api/v1/tasks/${taskId}/reassign-group`, {
          method: 'POST',
          body: JSON.stringify(reason ? { reason } : {})
        })
    );
  },

  reassignUser: async (taskId, username, reason) => {
    return withFallback(
      () =>
        httpClient.request(`/api/v1/supervision/tasks/${taskId}/reassign-user`, {
          method: 'POST',
          body: JSON.stringify(reason ? { username, reason } : { username })
        }),
      () =>
        httpClient.request(`/api/v1/tasks/${taskId}/reassign-user`, {
          method: 'POST',
          body: JSON.stringify(reason ? { username, reason } : { username })
        })
    );
  }
};
