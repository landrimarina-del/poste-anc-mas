import { httpClient } from './httpClient';
import { practicesApi } from './practicesApi';
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

function toMonthBounds(month) {
  const match = /^(\d{4})-(\d{2})$/.exec(String(month ?? ''));
  if (!match) {
    return { from: '', to: '' };
  }

  const year = Number(match[1]);
  const monthIndex = Number(match[2]);
  if (!year || monthIndex < 1 || monthIndex > 12) {
    return { from: '', to: '' };
  }

  const daysInMonth = new Date(year, monthIndex, 0).getDate();
  return {
    from: `${match[1]}-${match[2]}-01`,
    to: `${match[1]}-${match[2]}-${String(daysInMonth).padStart(2, '0')}`
  };
}

function toDayOfMonth(value) {
  if (!value) {
    return 0;
  }
  const date = new Date(value);
  if (!Number.isNaN(date.getTime())) {
    return date.getDate();
  }

  const fromText = String(value).slice(8, 10);
  const parsed = Number(fromText);
  return Number.isFinite(parsed) ? parsed : 0;
}

function normalizePractice(item) {
  return {
    state: item?.state ?? item?.practiceState ?? item?.status ?? '',
    sdOutcome: item?.sdOutcome ?? item?.outcome ?? item?.result ?? '',
    openedAt: item?.openedAt ?? item?.createdAt ?? item?.insertedAt ?? '',
    closedAt: item?.closedAt ?? item?.closedDate ?? item?.updatedAt ?? ''
  };
}

function normalizeCounters(details) {
  return {
    activities: Number(details?.activities ?? details?.attivita ?? details?.tasks ?? 0) || 0,
    activePractices: Number(details?.activePractices ?? details?.praticheAttive ?? details?.openPractices ?? 0) || 0,
    closedPractices: Number(details?.closedPractices ?? details?.praticheChiuse ?? details?.closed ?? 0) || 0
  };
}

function normalizeDailyOpened(details) {
  const list = Array.isArray(details)
    ? details
    : Array.isArray(details?.items)
      ? details.items
      : [];

  return list
    .map((item) => ({
      day: Number(item?.day ?? item?.giorno ?? toDayOfMonth(item?.date ?? item?.openedAt ?? item?.dayRef)) || 0,
      count: Number(item?.count ?? item?.openedPractices ?? item?.value ?? item?.total ?? 0) || 0
    }))
    .filter((item) => item.day > 0)
    .sort((a, b) => a.day - b.day);
}

function normalizeDailyWorked(details) {
  const list = Array.isArray(details)
    ? details
    : Array.isArray(details?.items)
      ? details.items
      : [];

  return list
    .map((item) => ({
      day: Number(item?.day ?? item?.giorno ?? toDayOfMonth(item?.date ?? item?.closedAt ?? item?.dayRef)) || 0,
      ok: Number(item?.ok ?? item?.okPractices ?? item?.countOk ?? item?.approved ?? 0) || 0,
      ko: Number(item?.ko ?? item?.koPractices ?? item?.countKo ?? item?.rejected ?? 0) || 0
    }))
    .filter((item) => item.day > 0)
    .sort((a, b) => a.day - b.day);
}

function normalizeByState(details) {
  const list = Array.isArray(details)
    ? details
    : Array.isArray(details?.items)
      ? details.items
      : [];

  return list
    .map((item) => ({
      day: Number(item?.day ?? item?.giorno ?? toDayOfMonth(item?.date ?? item?.openedAt ?? item?.dayRef)) || 0,
      state: String(item?.state ?? item?.stato ?? item?.label ?? 'N/D'),
      count: Number(item?.count ?? item?.practices ?? item?.value ?? item?.total ?? 0) || 0
    }))
    .filter((item) => item.state && item.count >= 0)
    .sort((a, b) => (a.day - b.day) || a.state.localeCompare(b.state));
}

const fallbackSnapshotCache = new Map();

async function readFallbackSnapshot(month) {
  if (fallbackSnapshotCache.has(month)) {
    return fallbackSnapshotCache.get(month);
  }

  const bounds = toMonthBounds(month);
  const [tasksResponse, practicesResponse] = await Promise.all([
    tasksApi.list({}),
    practicesApi.list({
      page: 0,
      size: 500,
      openedFrom: bounds.from,
      openedTo: bounds.to
    })
  ]);

  const tasksItems = Array.isArray(tasksResponse)
    ? tasksResponse
    : Array.isArray(tasksResponse?.items)
      ? tasksResponse.items
      : [];

  const practiceItemsRaw = Array.isArray(practicesResponse)
    ? practicesResponse
    : Array.isArray(practicesResponse?.items)
      ? practicesResponse.items
      : [];

  const practiceItems = practiceItemsRaw.map(normalizePractice);
  const snapshot = { tasksItems, practiceItems };
  fallbackSnapshotCache.set(month, snapshot);
  return snapshot;
}

function computeFallbackCounters(snapshot) {
  const closedStates = new Set(['CHIUSA_OK', 'CHIUSA_KO']);
  const activities = snapshot.tasksItems.length;
  const closedPractices = snapshot.practiceItems.filter((item) => closedStates.has(item.state)).length;
  const activePractices = snapshot.practiceItems.length - closedPractices;

  return { activities, activePractices, closedPractices };
}

function computeFallbackDailyOpened(snapshot) {
  const byDay = new Map();

  snapshot.practiceItems.forEach((item) => {
    const day = toDayOfMonth(item.openedAt);
    if (!day) {
      return;
    }
    byDay.set(day, (byDay.get(day) ?? 0) + 1);
  });

  return Array.from(byDay.entries())
    .map(([day, count]) => ({ day, count }))
    .sort((a, b) => a.day - b.day);
}

function computeFallbackDailyWorked(snapshot) {
  const byDay = new Map();

  snapshot.practiceItems.forEach((item) => {
    const day = toDayOfMonth(item.closedAt);
    if (!day) {
      return;
    }

    const current = byDay.get(day) ?? { day, ok: 0, ko: 0 };
    const outcome = String(item.sdOutcome ?? '').toUpperCase();

    if (outcome === 'OK' || item.state === 'CHIUSA_OK') {
      current.ok += 1;
    } else {
      current.ko += 1;
    }

    byDay.set(day, current);
  });

  return Array.from(byDay.values()).sort((a, b) => a.day - b.day);
}

function computeFallbackByState(snapshot) {
  const byStateAndDay = new Map();

  snapshot.practiceItems.forEach((item) => {
    const day = toDayOfMonth(item.openedAt);
    if (!day) {
      return;
    }
    const state = String(item.state || 'N/D');
    const key = `${day}|${state}`;
    byStateAndDay.set(key, (byStateAndDay.get(key) ?? 0) + 1);
  });

  return Array.from(byStateAndDay.entries())
    .map(([key, count]) => {
      const separatorIndex = key.indexOf('|');
      const day = Number(key.slice(0, separatorIndex)) || 0;
      const state = key.slice(separatorIndex + 1) || 'N/D';
      return { day, state, count };
    })
    .sort((a, b) => (a.day - b.day) || a.state.localeCompare(b.state));
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

export const supervisionDashboardApi = {
  counters: async () => {
    return withFallback(
      async () => {
        const details = await httpClient.request('/api/v1/supervision/dashboard/counters', { method: 'GET' });
        return {
          values: normalizeCounters(details),
          fallbackMode: false
        };
      },
      async () => {
        const snapshot = await readFallbackSnapshot('all');
        return {
          values: computeFallbackCounters(snapshot),
          fallbackMode: true
        };
      }
    );
  },

  dailyOpened: async (month) => {
    return withFallback(
      async () => {
        const details = await httpClient.request(
          `/api/v1/supervision/dashboard/daily-opened${buildQueryString({ month })}`,
          { method: 'GET' }
        );
        return {
          items: normalizeDailyOpened(details),
          fallbackMode: false
        };
      },
      async () => {
        const snapshot = await readFallbackSnapshot(month);
        return {
          items: computeFallbackDailyOpened(snapshot),
          fallbackMode: true
        };
      }
    );
  },

  dailyWorked: async (month) => {
    return withFallback(
      async () => {
        const details = await httpClient.request(
          `/api/v1/supervision/dashboard/daily-worked${buildQueryString({ month })}`,
          { method: 'GET' }
        );
        return {
          items: normalizeDailyWorked(details),
          fallbackMode: false
        };
      },
      async () => {
        const snapshot = await readFallbackSnapshot(month);
        return {
          items: computeFallbackDailyWorked(snapshot),
          fallbackMode: true
        };
      }
    );
  },

  byState: async (month) => {
    return withFallback(
      async () => {
        const details = await httpClient.request(
          `/api/v1/supervision/dashboard/by-state${buildQueryString({ month })}`,
          { method: 'GET' }
        );
        return {
          items: normalizeByState(details),
          fallbackMode: false
        };
      },
      async () => {
        const snapshot = await readFallbackSnapshot(month);
        return {
          items: computeFallbackByState(snapshot),
          fallbackMode: true
        };
      }
    );
  }
};