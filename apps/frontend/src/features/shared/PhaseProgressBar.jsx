// Componente barra milestone fasi pratica (Sprint 12 — S12-FE-4)
// Ref: GAP-UI.md §6 + GAP-UX.md §8

const FASI = [
  { id: 'RACCOLTA_INPUT', label: 'Raccolta Input' },
  { id: 'LAVORAZIONE', label: 'Lavorazione' },
  { id: 'CHIUSURA_PRATICA', label: 'Chiusura Pratica' }
];

export function PhaseProgressBar({ fase }) {
  const currentIndex = FASI.findIndex(
    (f) => f.id === String(fase ?? '').toUpperCase()
  );

  return (
    <div className="phase-progress-bar" aria-label="Avanzamento fasi pratica">
      {FASI.map((f, index) => {
        let stepClass = 'future';
        if (index < currentIndex) {
          stepClass = 'completed';
        } else if (index === currentIndex) {
          stepClass = 'current';
        }

        return (
          <div key={f.id} className={`phase-step phase-step-${stepClass}`}>
            {index > 0 && <div className="phase-connector" />}
            <div className="phase-step-content">
              <span className="phase-step-dot">
                {stepClass === 'completed' ? (
                  <i className="fa fa-check" aria-hidden="true" />
                ) : (
                  index + 1
                )}
              </span>
              <span className="phase-step-label">{f.label}</span>
            </div>
          </div>
        );
      })}
    </div>
  );
}
