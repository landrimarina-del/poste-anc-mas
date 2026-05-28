// Componente "Verifica Documento" — sezione 2 del task lavorazione (Sprint 12 — S12-FE-2)
// Ref: GAP-UI.md §2.5 — Verifica Documenti
// Layout 2 colonne: col-sx (dati + checklist) | col-dx (allegati/viewer)

import { Fragment, useEffect, useState } from 'react';
import { attachmentsApi } from '../../core/api/attachmentsApi';
import { intakeApi } from '../../core/api/intakeApi';

const yesNoOptions = [
  { value: 'SI', label: 'Si' },
  { value: 'NO', label: 'No' }
];

const PPEZ_DESCRIPTIONS = {
  PPEZ026: 'Verbale Denuncia non leggibile',
  PPEZ027: 'Intestazione mancante nel Verbale Denuncia',
  PPEZ028: 'Firme mancanti nel Verbale Denuncia',
  PPEZ029: 'Intestazione non conforme al Timbro nel Verbale Denuncia',
  PPEZ030: 'Dichiarazione non conforme alle firme nel Verbale Denuncia',
  PPEZ031: 'Mancata descrizione emissione Carta da Poste Italiane',
  PPEZ032: 'Incoerenza dati Denunciante con dati Cliente',
  PPEZ033: 'Incorenza numero Carta nel Verbale Denuncia',
  PPEZ034: 'Carta non leggibile',
  PPEZ035: 'Carta non tagliata o incorenza numero Carta',
};

const formalKoReasonOptions = [
  { value: 'INTESTAZIONE', label: 'Intestazione' },
  { value: 'FIRME',        label: 'Firme' },
  { value: 'TIMBRO',       label: 'Timbro' },
  { value: 'DICHIARAZIONE', label: 'Dichiarazione' },
  { value: 'CARTA_PI',     label: 'Carta Poste Italiane' }
];

const verbaleChecklistRows = [
  {
    key: 'legibility',
    required: 'SI',
    title: 'Il Verbale di Denuncia risulta leggibile?',
    detail: 'Verificare che tutte le informazioni essenziali presenti nel verbale siano chiaramente leggibili e non compromesse da qualità scansione o oscuramenti.'
  },
  {
    key: 'formalSuitability',
    required: 'SI',
    title: 'Il Verbale di Denuncia è idoneo al controllo formale?',
    detail: 'Verificare la presenza e la qualità degli elementi formali richiesti (es. intestazione, firme, timbro, dichiarazione) utili alla validazione documentale.',
    isGroupHeader: true,
    subRows: [
      { key: 'intestazioneOk',                    label: '• Intestazione' },
      { key: 'firmeOk',                            label: '• Firme' },
      { key: 'intestazioneConformeAlTimbroOk',     label: '• Intestazione Conforme al Timbro' },
      { key: 'dichiarazioneConformeAlleFirmeOk',   label: '• Dichiarazione Conforme alle Firme' },
      { key: 'cartaPosteItalianeOk',               label: '• Carta Poste Italiane' },
    ]
  },
  {
    key: 'clientDataConsistency',
    required: 'SI',
    title: 'I dati del Verbale di Denuncia sono coerenti con i dati del Cliente?',
    detail: 'Confrontare i dati anagrafici e identificativi riportati nel verbale con quelli presenti in pratica per evidenziare eventuali incoerenze.'
  },
  {
    key: 'cardNumberMatch',
    required: 'NO',
    title: 'Il numero della carta nel Verbale di Denuncia corrisponde al numero di carta presente tra i dati della pratica?',
    detail: 'Controllo facoltativo: da valorizzare solo quando il numero carta e presente nel verbale di denuncia.'
  }
];

const cartaChecklistRows = [
  {
    key: 'legibility',
    required: 'SI',
    title: 'La Carta risulta leggibile?',
    detail: 'Verificare che l\'immagine o il documento della carta sia chiaramente leggibile in tutte le sue parti necessarie al controllo.'
  },
  {
    key: 'formalSuitability',
    required: 'SI',
    title: 'La Carta è idonea al controllo formale?',
    detail: 'Verificare che la carta sia idonea ai controlli formali previsti dal processo e che consenta la corretta valutazione documentale.'
  }
];

/**
 * Props:
 *   practiceDetail     : object — risposta completa GET /practices/{id}
 *   confirmedType      : string (VERBALE | CARTA)
 *   attachments        : array
 *   activeAttachmentId : string
 *   onAttachmentChange : (id: string) => void
 *   checklistForm      : object
 *   onChecklistChange  : (field, value) => void
 *   onToggleKoReason   : (reason) => void
 *   checklistStatus    : string
 *   checklistOutcome   : string
 *   checklistLoading   : boolean
 *   checklistSaving    : boolean
 *   canSaveChecklist   : boolean
 *   isCardChecklist    : boolean
 */
export function VerificaDocumentiStep({
  practiceId,
  practiceDetail,
  confirmedType,
  attachments,
  activeAttachmentId,
  onAttachmentChange,
  checklistForm,
  onChecklistChange,
  onToggleKoReason,
  checklistStatus,
  checklistOutcome,
  checklistLoading,
  checklistSaving,
  canSaveChecklist,
  isCardChecklist,
  koCodes
}) {
  function pickValue(...values) {
    for (const value of values) {
      if (value === null || value === undefined) continue;
      if (typeof value === 'string' && value.trim() === '') continue;
      return value;
    }
    return '-';
  }

  const [allegatiVisible, setAllegatiVisible] = useState(true);
  const [sezioneVisible,  setSezioneVisible]  = useState(true);
  const [showAddress,     setShowAddress]     = useState(false);
  const [verbaleDescriptionOpen, setVerbaleDescriptionOpen] = useState({});
  const [cartaDescriptionOpen, setCartaDescriptionOpen] = useState({});

  const header      = practiceDetail?.header      ?? {};
  const client      = practiceDetail?.client      ?? {};
  const legacyClient = practiceDetail?.cliente ?? practiceDetail?.clientData ?? {};
  const blockedCard = practiceDetail?.blockedCard ?? {};
  const safeAttachments = Array.isArray(attachments) ? attachments : [];

  const clienteNome = pickValue(client.firstName, client.nome, legacyClient.firstName, legacyClient.nome, legacyClient.NOME);
  const clienteCognome = pickValue(client.lastName, client.cognome, legacyClient.lastName, legacyClient.cognome, legacyClient.COGNOME);
  const clienteSesso = pickValue(client.gender, client.sesso, legacyClient.gender, legacyClient.sesso, legacyClient.SESSO);
  const clienteCodiceFiscale = pickValue(
    client.fiscalCode,
    client.codiceFiscale,
    client.codice_fiscale,
    legacyClient.fiscalCode,
    legacyClient.codiceFiscale,
    legacyClient.codice_fiscale,
    legacyClient.CODICEFISCALE,
    header.cfCliente,
    header.codiceFiscale
  );
  const clienteDataNascita = pickValue(
    client.birthDate,
    client.dataNascita,
    client.data_nascita,
    legacyClient.birthDate,
    legacyClient.dataNascita,
    legacyClient.data_nascita,
    legacyClient.DATANASCITA,
    legacyClient.DATA_NASCITA
  );
  const clienteComuneNascita = pickValue(
    client.birthCity,
    client.comuneNascita,
    client.comune_nascita,
    legacyClient.birthCity,
    legacyClient.comuneNascita,
    legacyClient.comune_nascita,
    legacyClient.COMUNENASCITA
  );
  const clienteProvinciaNascita = pickValue(
    client.birthProvince,
    client.provinciaNascita,
    client.provincia_nascita,
    legacyClient.birthProvince,
    legacyClient.provinciaNascita,
    legacyClient.provincia_nascita,
    legacyClient.PROVINCIANASCITA
  );
  const clienteNazioneNascita = pickValue(
    client.birthCountry,
    client.nazioneNascita,
    client.nazione_nascita,
    legacyClient.birthCountry,
    legacyClient.nazioneNascita,
    legacyClient.nazione_nascita,
    legacyClient.NAZIONENASCITA
  );
  const clienteTelefono = pickValue(
    client.phone,
    client.telephone,
    client.telefono,
    legacyClient.phone,
    legacyClient.telephone,
    legacyClient.telefono,
    legacyClient.TELEFONO
  );
  const clienteCellulare = pickValue(
    client.mobilePhone,
    client.mobile,
    client.cellulare,
    legacyClient.mobilePhone,
    legacyClient.mobile,
    legacyClient.cellulare,
    legacyClient.CELLULARE
  );

  const residence = client.residenceAddress
    ?? client.residence
    ?? client.address
    ?? client.indirizzoDiResidenza
    ?? client.indirizzoResidenza
    ?? legacyClient.residenceAddress
    ?? legacyClient.residence
    ?? legacyClient.address
    ?? legacyClient.indirizzoDiResidenza
    ?? legacyClient.indirizzoResidenza
    ?? {};
  const residenceVia = pickValue(residence.street, residence.via, residence.addressLine, residence.indirizzo, residence.INDIRIZZO);
  const residenceCivico = pickValue(residence.streetNumber, residence.civicNumber, residence.numeroCivico, residence.NUMERO_CIVICO);
  const residenceCap = pickValue(
    residence.postalCode,
    residence.zipCode,
    residence.cap,
    residence.CAP,
    client.residencePostalCode,
    legacyClient.residencePostalCode
  );
  const residenceComune = pickValue(residence.city, residence.comune, residence.COMUNE);
  const residenceProvincia = pickValue(residence.province, residence.provincia, residence.PROVINCIA);
  const residenceNazione = pickValue(residence.country, residence.nazione, residence.NAZIONE);

  const tipoCarta = blockedCard.cardType ?? blockedCard.tipoCarta ?? '-';
  const numeroCarta = blockedCard.cardNumberMasked ?? blockedCard.cardNumber ?? header.cardNumber ?? '-';
  const intestazioneCarta = blockedCard.cardHolder
    ?? blockedCard.intestatario
    ?? [client.lastName ?? client.cognome, client.firstName ?? client.nome].filter(Boolean).join(' ')
    ?? '-';

  const previewUrl = activeAttachmentId
    ? attachmentsApi.previewUrl(activeAttachmentId)
    : '';
  const downloadUrl = activeAttachmentId
    ? attachmentsApi.downloadUrl(activeAttachmentId)
    : '';

  const isConformityDisabled = checklistForm?.documentPresent === 'NO';

  const isBusy = checklistLoading || checklistSaving;

  // Sprint 13: stato causali e toggle aree KO
  const [causaliCarta,   setCausaliCarta]   = useState([]);
  const [causaliVerbale, setCausaliVerbale] = useState([]);
  const [koCartaOpen,    setKoCartaOpen]    = useState(true);

  // Sprint 13: carica causali CARTA quando conformità diventa NO o esiste causale già selezionata
  useEffect(() => {
    const hasKo = checklistForm?.formalSuitability === 'NO' || checklistForm?.legibility === 'NO';
    const hasSelectedCausale = Boolean(checklistForm?.codiceCausaleIdCarta);
    if (!isCardChecklist || !practiceId || causaliCarta.length > 0 || (!hasKo && !hasSelectedCausale)) return;
    intakeApi.getCausali(practiceId, 'CARTA')
      .then((r) => setCausaliCarta(Array.isArray(r) ? r : []))
      .catch(() => {});
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [checklistForm?.formalSuitability, checklistForm?.legibility, checklistForm?.codiceCausaleIdCarta, isCardChecklist, practiceId]);

  // Sprint 13: carica causali VERBALE quando almeno un item diventa NO
  useEffect(() => {
    if (isCardChecklist || !practiceId || causaliVerbale.length > 0) return;
    const anyKo = checklistForm?.legibility === 'NO'
      || checklistForm?.formalSuitability === 'NO'
      || checklistForm?.clientDataConsistency === 'NO'
      || checklistForm?.cardNumberMatch === 'NO';
    const hasSelectedCausale = Boolean(checklistForm?.codiceCausaleIdVerbale);
    if (!anyKo && !hasSelectedCausale) return;
    intakeApi.getCausali(practiceId, 'VERBALE')
      .then((r) => setCausaliVerbale(Array.isArray(r) ? r : []))
      .catch(() => {});
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    checklistForm?.legibility,
    checklistForm?.formalSuitability,
    checklistForm?.clientDataConsistency,
    checklistForm?.cardNumberMatch,
    checklistForm?.codiceCausaleIdVerbale,
    isCardChecklist,
    practiceId
  ]);


  // Esito live: KO se almeno un campo è NO, OK se tutto valorizzato senza KO
  const esitoControllo = (() => {
    if (koCodes && koCodes.length > 0) return 'KO';
    if (isCardChecklist) {
      // CARTA: OK quando legibility e formalSuitability sono entrambe valorizzate a SI
      if (checklistForm.legibility === 'SI' && checklistForm.formalSuitability === 'SI') return 'OK';
    } else {
      // VERBALE: OK quando il documento è presente (documentPresent='SI') e nessun KO
      if (checklistForm.documentPresent === 'SI') return 'OK';
    }
    return checklistOutcome ? (checklistOutcome === 'APPROVATA' ? 'OK' : 'KO') : '-';
  })();
  const selectedCausaleId = isCardChecklist
    ? checklistForm.codiceCausaleIdCarta
    : checklistForm.codiceCausaleIdVerbale;
  const causaleDescription = selectedCausaleId
    ? ((isCardChecklist ? causaliCarta : causaliVerbale)
      .find((c) => String(c.id) === String(selectedCausaleId))?.descrizione ?? '-')
    : '-';

  return (
    <div className="verifica-wrapper">
      {/* Button bar */}
      <div className="verifica-button-bar">
        <button
          type="button"
          className="btn btn-outline btn-small"
          onClick={() => setAllegatiVisible((v) => !v)}
        >
          <i
            className={`fa ${allegatiVisible ? 'fa-eye-slash' : 'fa-eye'}`}
            aria-hidden="true"
          />
          {' '}{allegatiVisible ? 'nascondi allegati' : 'mostra allegati'}
        </button>
        <button
          type="button"
          className="btn btn-outline btn-small"
          onClick={() => setSezioneVisible((v) => !v)}
        >
          {sezioneVisible ? 'nascondi sezione' : 'mostra sezione'}
        </button>
      </div>

      {sezioneVisible ? (
        <div className="verifica-content verifica-content-grid-2x2">
          <div className="summary-card verifica-data-card verifica-area-top-left">
            <h4>Dati Carta Bloccata</h4>
            <dl>
              <div>
                <dt>Tipo Carta</dt>
                <dd>{tipoCarta || '-'}</dd>
              </div>
              <div>
                <dt>Numero Carta</dt>
                <dd>{numeroCarta || '-'}</dd>
              </div>
              <div>
                <dt>Intestazione Carta</dt>
                <dd>{intestazioneCarta || '-'}</dd>
              </div>
            </dl>
          </div>

          <div className="summary-card verifica-data-card verifica-area-top-right">
            <h4>Dati Cliente</h4>
            <div className="verifica-client-rows">
              <dl className="verifica-client-row verifica-client-row-3">
                <div className="verifica-client-field"><dt>Nome</dt><dd>{clienteNome}</dd></div>
                <div className="verifica-client-field"><dt>Cognome</dt><dd>{clienteCognome}</dd></div>
                <div className="verifica-client-field"><dt>Sesso</dt><dd>{clienteSesso}</dd></div>
              </dl>
              <dl className="verifica-client-row verifica-client-row-3">
                <div className="verifica-client-field"><dt>Codice Fiscale</dt><dd>{clienteCodiceFiscale}</dd></div>
                <div className="verifica-client-field"><dt>Data di nascita</dt><dd>{clienteDataNascita}</dd></div>
                <div className="verifica-client-field"><dt>Comune di nascita</dt><dd>{clienteComuneNascita}</dd></div>
              </dl>
              <dl className="verifica-client-row verifica-client-row-3">
                <div className="verifica-client-field"><dt>Provincia di nascita</dt><dd>{clienteProvinciaNascita}</dd></div>
                <div className="verifica-client-field"><dt>Nazione di nascita</dt><dd>{clienteNazioneNascita}</dd></div>
                <div className="verifica-client-field"><dt>Telefono</dt><dd>{clienteTelefono}</dd></div>
              </dl>
              <dl className="verifica-client-row verifica-client-row-1">
                <div className="verifica-client-field"><dt>Cellulare</dt><dd>{clienteCellulare}</dd></div>
              </dl>
            </div>

            <button
              type="button"
              className="btn btn-outline btn-small"
              onClick={() => setShowAddress((v) => !v)}
              style={{ marginTop: 12 }}
            >
              {showAddress ? 'nascondi indirizzo residenza' : 'mostra indirizzo residenza'}
            </button>

            {showAddress ? (
              <div className="info-box" style={{ marginTop: 12 }}>
                <strong>Indirizzo di residenza</strong>
                <div className="verifica-client-rows" style={{ marginTop: 8 }}>
                  <dl className="verifica-client-row verifica-client-row-3">
                    <div className="verifica-client-field"><dt>Via</dt><dd>{residenceVia}</dd></div>
                    <div className="verifica-client-field"><dt>Numero civico</dt><dd>{residenceCivico}</dd></div>
                    <div className="verifica-client-field"><dt>CAP</dt><dd>{residenceCap}</dd></div>
                  </dl>
                  <dl className="verifica-client-row verifica-client-row-3">
                    <div className="verifica-client-field"><dt>Comune</dt><dd>{residenceComune}</dd></div>
                    <div className="verifica-client-field"><dt>Provincia</dt><dd>{residenceProvincia}</dd></div>
                    <div className="verifica-client-field"><dt>Nazione</dt><dd>{residenceNazione}</dd></div>
                  </dl>
                </div>
              </div>
            ) : null}
          </div>

          <div className="summary-card verifica-data-card verifica-area-bottom-right">
            <h4>{isCardChecklist ? 'Contenuti Carta' : 'Contenuti Verbale di denuncia'}</h4>
            {downloadUrl ? (
              <a
                className="btn btn-outline btn-small"
                href={downloadUrl}
                target="_blank"
                rel="noreferrer"
                style={{ marginBottom: 12 }}
              >
                Download documento
              </a>
            ) : null}
            {allegatiVisible ? (
              safeAttachments.length > 0 ? (
                previewUrl ? (
                  <div className="viewer-frame-shell" style={{ height: 500 }}>
                    <iframe
                      title="Anteprima allegato"
                      src={previewUrl}
                      className="viewer-frame"
                    />
                  </div>
                ) : (
                  <div className="panel-note">Selezionare un allegato per visualizzare il contenuto.</div>
                )
              ) : (
                <div className="panel-note">Nessun allegato disponibile sulla pratica.</div>
              )
            ) : (
              <div className="panel-note">Sezione allegati nascosta.</div>
            )}
          </div>

          <div className="checklist-form-section verifica-area-bottom-left">
            <h4>{isCardChecklist ? 'Controllo Carta' : 'Controllo Verbale di denuncia'}</h4>

            <div className="summary-card verifica-data-card" style={{ marginBottom: 12 }}>
              <h4>Tipo Documento</h4>
              <dl>
                <div>
                  <dt>Tipo confermato</dt>
                  <dd>
                    {confirmedType}{' '}
                    <span className="status-badge status-badge-draft">confermato</span>
                  </dd>
                </div>
              </dl>
            </div>

            {allegatiVisible && safeAttachments.length > 0 ? (
              <div className="viewer-attachment-select" style={{ marginBottom: 16 }}>
                <label htmlFor="verifica-attachSel">Scelta documento</label>
                <select
                  id="verifica-attachSel"
                  value={activeAttachmentId}
                  onChange={(e) => onAttachmentChange(e.target.value)}
                >
                  {safeAttachments.map((item) => {
                    const val = String(item.attachmentId ?? item.id);
                    return (
                      <option key={val} value={val}>
                        {item.fileName ?? item.name ?? `Allegato ${val}`}
                      </option>
                    );
                  })}
                </select>
              </div>
            ) : null}

            <div className="checklist-meta-row">
              <span className="status-badge">
                Stato: {checklistStatus || 'NON_INIZIATA'}
              </span>
            </div>

            {checklistLoading ? (
              <div className="panel-note">Caricamento checklist...</div>
            ) : (
              <div className="checklist-fields-grid checklist-fields-grid-table">
                <div
                  className="verbale-checklist-table-wrapper"
                  role="region"
                  aria-label={isCardChecklist ? 'Checklist carta' : 'Checklist verbale'}
                >
                  <table className="verbale-checklist-table">
                    <thead>
                      <tr>
                        <th scope="col">Richiesto</th>
                        <th scope="col">Descrizione</th>
                        <th scope="col">Conforme</th>
                        <th scope="col">Note</th>
                      </tr>
                    </thead>
                    <tbody>
                      {(isCardChecklist ? cartaChecklistRows : verbaleChecklistRows).map((row) => {
                        const isVerbaleCardRow = !isCardChecklist && row.key === 'cardNumberMatch';
                        const isRowEnabled = !isVerbaleCardRow || checklistForm.cardNumberCheckEnabled;
                        const rowDisabled = (!isCardChecklist && isConformityDisabled)
                          || isBusy
                          || !canSaveChecklist
                          || !isRowEnabled;
                        const radioGroupName = `vd-conforme-${row.key}`;
                        const isDescriptionOpen = isCardChecklist
                          ? Boolean(cartaDescriptionOpen[row.key])
                          : Boolean(verbaleDescriptionOpen[row.key]);

                        return (
                          <Fragment key={row.key}>
                            {row.isGroupHeader ? (
                              // Riga intestazione gruppo: solo label, senza radio SI/NO
                              <tr>
                                <td><strong>{row.required}</strong></td>
                                <td><div className="verbale-description-title">{row.title}</div></td>
                                <td></td>
                                <td></td>
                              </tr>
                            ) : (
                              <tr className={!isRowEnabled ? 'verbale-row-disabled' : ''}>
                                <td>
                                  <strong>{row.required}</strong>
                                  {isVerbaleCardRow ? (
                                    <div className="verbale-required-note">
                                      Da valorizzare solo se presente il numero carta nel verbale.
                                      <label style={{ display: 'block', marginTop: 4 }}>
                                        <input
                                          type="checkbox"
                                          checked={!!checklistForm.cardNumberCheckEnabled}
                                          onChange={(e) => onChecklistChange('cardNumberCheckEnabled', e.target.checked)}
                                          disabled={isBusy || !canSaveChecklist}
                                        />
                                        {' '}Abilita controllo
                                      </label>
                                    </div>
                                  ) : null}
                                </td>
                                <td>
                                  <div className="verbale-description-title">{row.title}</div>
                                  <button
                                    type="button"
                                    className="verbale-description-toggle"
                                    onClick={() => {
                                      if (isCardChecklist) {
                                        setCartaDescriptionOpen((prev) => ({ ...prev, [row.key]: !prev[row.key] }));
                                        return;
                                      }
                                      setVerbaleDescriptionOpen((prev) => ({ ...prev, [row.key]: !prev[row.key] }));
                                    }}
                                  >
                                    {isDescriptionOpen ? 'Nascondi descrizione' : 'Mostra descrizione'}
                                  </button>
                                </td>
                                <td>
                                  <div className="verbale-radio-group" role="radiogroup" aria-label={`Conforme ${row.title}`}>
                                    <label className="checklist-checkbox-inline">
                                      <input
                                        type="radio"
                                        name={radioGroupName}
                                        value="SI"
                                        checked={checklistForm[row.key] === 'SI'}
                                        onChange={(e) => onChecklistChange(row.key, e.target.value)}
                                        disabled={rowDisabled}
                                      />
                                      SI
                                    </label>
                                    <label className="checklist-checkbox-inline">
                                      <input
                                        type="radio"
                                        name={radioGroupName}
                                        value="NO"
                                        checked={checklistForm[row.key] === 'NO'}
                                        onChange={(e) => onChecklistChange(row.key, e.target.value)}
                                        disabled={rowDisabled}
                                      />
                                      NO
                                    </label>
                                  </div>
                                </td>
                                <td className="verbale-note-cell">
                                  <textarea
                                    className="verbale-note-textarea"
                                    value={isCardChecklist
                                      ? (checklistForm?.cardNotes?.[row.key] ?? '')
                                      : (checklistForm?.verbaleNotes?.[row.key] ?? '')}
                                    onChange={(e) => {
                                      if (isCardChecklist) {
                                        const nextCardNotes = {
                                          ...(checklistForm?.cardNotes ?? {}),
                                          [row.key]: e.target.value
                                        };
                                        onChecklistChange('cardNotes', nextCardNotes);
                                        return;
                                      }
                                      const nextVerbaleNotes = {
                                        ...(checklistForm?.verbaleNotes ?? {}),
                                        [row.key]: e.target.value
                                      };
                                      onChecklistChange('verbaleNotes', nextVerbaleNotes);
                                    }}
                                    placeholder="Inserisci note per la riga"
                                    maxLength={1000}
                                    rows={2}
                                    disabled={rowDisabled}
                                  />
                                </td>
                              </tr>
                            )}
                            {/* Sotto-righe del gruppo formalSuitability */}
                            {row.isGroupHeader && row.subRows ? row.subRows.map((sub) => {
                              const subDisabled = (!isCardChecklist && isConformityDisabled) || isBusy || !canSaveChecklist;
                              const subRadioName = `vd-conforme-${sub.key}`;
                              return (
                                <tr key={sub.key}>
                                  <td></td>
                                  <td>
                                    <div className="verbale-description-title" style={{ paddingLeft: '1.5rem' }}>{sub.label}</div>
                                  </td>
                                  <td>
                                    <div className="verbale-radio-group" role="radiogroup" aria-label={`Conforme ${sub.label}`}>
                                      <label className="checklist-checkbox-inline">
                                        <input
                                          type="radio"
                                          name={subRadioName}
                                          value="SI"
                                          checked={checklistForm[sub.key] === 'SI'}
                                          onChange={(e) => onChecklistChange(sub.key, e.target.value)}
                                          disabled={subDisabled}
                                        />
                                        SI
                                      </label>
                                      <label className="checklist-checkbox-inline">
                                        <input
                                          type="radio"
                                          name={subRadioName}
                                          value="NO"
                                          checked={checklistForm[sub.key] === 'NO'}
                                          onChange={(e) => onChecklistChange(sub.key, e.target.value)}
                                          disabled={subDisabled}
                                        />
                                        NO
                                      </label>
                                    </div>
                                  </td>
                                  <td className="verbale-note-cell">
                                    <textarea
                                      className="verbale-note-textarea"
                                      value={checklistForm?.verbaleNotes?.[sub.key] ?? ''}
                                      onChange={(e) => {
                                        const nextVerbaleNotes = {
                                          ...(checklistForm?.verbaleNotes ?? {}),
                                          [sub.key]: e.target.value
                                        };
                                        onChecklistChange('verbaleNotes', nextVerbaleNotes);
                                      }}
                                      placeholder="Inserisci note per la riga"
                                      maxLength={1000}
                                      rows={2}
                                      disabled={subDisabled}
                                    />
                                  </td>
                                </tr>
                              );
                            }) : null}
                            {!row.isGroupHeader && isDescriptionOpen ? (
                              <tr className="verbale-description-row">
                                <td colSpan={4}>{row.detail}</td>
                              </tr>
                            ) : null}
                          </Fragment>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {isConformityDisabled ? (
              <div className="info-box">
                {isCardChecklist
                  ? 'Carta assente: esito KO automatico applicato.'
                  : 'Documento assente: KO automatico applicato e controlli conformità disabilitati.'}
              </div>
            ) : null}

            <div className={`summary-card verbale-esito-causale-card${esitoControllo === 'OK' ? ' outcome-card-ok' : esitoControllo === 'KO' ? ' outcome-card-ko' : ''}`}>
              <dl>
                <div>
                  <dt>Esito Controllo</dt>
                  <dd>{esitoControllo}</dd>
                </div>
                <div>
                  <dt>Causali KO</dt>
                  <dd>
                    {koCodes && koCodes.length > 0 ? (
                      <ul style={{ margin: 0, paddingLeft: '1.2rem' }}>
                        {koCodes.map((code) => (
                          <li key={code}>{PPEZ_DESCRIPTIONS[code] || code}</li>
                        ))}
                      </ul>
                    ) : '-'}
                  </dd>
                </div>
              </dl>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
