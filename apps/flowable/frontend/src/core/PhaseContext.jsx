import { createContext, useCallback, useContext, useState } from 'react';

const PhaseContext = createContext({ fase: null, setFase: () => {} });

export function PhaseProvider({ children }) {
  const [fase, setFaseRaw] = useState(null);

  const setFase = useCallback((value) => {
    setFaseRaw(value ? String(value).toUpperCase() : null);
  }, []);

  return (
    <PhaseContext.Provider value={{ fase, setFase }}>
      {children}
    </PhaseContext.Provider>
  );
}

export function usePhase() {
  return useContext(PhaseContext);
}
