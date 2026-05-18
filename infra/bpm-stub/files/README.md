# bpm-stub - asset statici per LINKDOWNLOAD (GAP-BLOCKER-001)

Questo file server simula il `LINKDOWNLOAD` lato BPM Sinergia per la POC.

## Esposizione

- Container: `bpm-stub` (immagine `nginx:1.27-alpine`).
- Mount: `./infra/bpm-stub/files` -> `/usr/share/nginx/html/files` (read-only).
- URL base (rete docker-compose interna): `http://bpm-stub/files/<FILE>`.
- Nessuna porta esposta sull'host: raggiungibile SOLO dalla rete docker-compose.

## File disponibili e mapping ID_DOC tipico

| File                               | URL                                 | Content-Type (servito da nginx) | Uso suggerito                          |
|------------------------------------|-------------------------------------|---------------------------------|----------------------------------------|
| `sample.pdf`                       | `http://bpm-stub/files/sample.pdf`  | `application/pdf`               | modulo doc id 3 (verbale/carta PDF)    |
| `sample.png`                       | `http://bpm-stub/files/sample.png`  | `image/png`                     | modulo doc id 1 (foto fronte) |
| `sample.jpg`                       | `http://bpm-stub/files/sample.jpg`  | `image/jpeg`                    | modulo doc id 2 (foto retro)  |

Il mapping `ID_DOC -> file` e' una semplificazione per i payload di trigger BPM
inbound (`docs/...`). Nei test reali il file servito puo' essere qualsiasi,
purche' coerente con `ESTENSIONE` dichiarata nel payload.

## Pattern URL nel payload `CONTENUTI[].LINKDOWNLOAD`

```
http://bpm-stub/files/sample.pdf
http://bpm-stub/files/sample.png
http://bpm-stub/files/sample.jpg
```

L'host `bpm-stub` e' incluso nell'allow-list di `AttachmentFetcher`
(`ANC_ATTACHMENT_ALLOWLIST_HOSTS=bpm-stub`).

## Nota POC sui contenuti

- `sample.pdf` e' un PDF 1.4 minimo valido (~470 byte): supera la validazione
  `application/pdf` e si apre nel viewer.
- `sample.png` / `sample.jpg` in questa baseline sono **placeholder testuali**
  con la sola estensione coerente: nginx li serve con Content-Type corretto
  (la validazione `Content-Type vs ESTENSIONE` del fetcher passa), ma il viewer
  non riuscira' a renderizzarli come immagine. Sostituirli con asset reali e'
  un debito tecnico tracciato come CrossAgent issue nel report Sprint 4.
