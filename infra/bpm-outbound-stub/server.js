const express = require('express');
const { v4: uuidv4 } = require('uuid');
const app = express();
app.use(express.json());

let currentMode = process.env.BPM_STUB_ESITO_MODE || 'OK';
const koDescription = process.env.BPM_STUB_KO_DESCRIZIONE || 'Esito rifiutato da BPM (mock)';

// POST /receive-outcome
app.post('/receive-outcome', (req, res) => {
    const body = req.body || {};
    const outcome = String(body.outcome || '').toUpperCase();
    const esito = outcome === 'OK';
    console.log(`[STUB] receive-outcome: practiceId=${body.practiceId} outcome=${outcome} koCodes=${JSON.stringify(body.koCodes)} → esito=${esito}`);
    res.json({ esito, descrizioneEsito: esito ? 'ACK OK' : 'Esito rifiutato da BPM (mock)' });
});

// POST /ticketing/open-ticket
app.post('/ticketing/open-ticket', (req, res) => {
    res.json({ ticketId: 'MOCK-TICKET-' + uuidv4() });
});

// GET /admin/mode
app.get('/admin/mode', (req, res) => {
    res.json({ mode: currentMode });
});

// PUT /admin/mode
app.put('/admin/mode', (req, res) => {
    const { mode } = req.body;
    if (mode !== 'OK' && mode !== 'KO') {
        return res.status(400).json({ error: 'mode deve essere OK o KO' });
    }
    currentMode = mode;
    res.json({ mode: currentMode });
});

const PORT = process.env.PORT || 8090;
app.listen(PORT, () => console.log(`bpm-outbound-stub avviato su porta ${PORT} — modo: ${currentMode}`));
