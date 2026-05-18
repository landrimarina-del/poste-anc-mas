package it.poste.anc.document.ingestion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Componente di pull-through sincrono del binario allegato (GAP-BLOCKER-001).
 *
 * Mitigazioni SSRF / abuso:
 * - schemi ammessi: SOLO http/https
 * - allow-list per HOSTNAME (string match esatto, case-insensitive)
 * - redirect: NEVER (no follow cross-host)
 * - timeout configurabile
 * - size cap durante lo streaming (no read-all illimitato)
 * - validazione Content-Type vs estensione dichiarata
 */
@Service
public class AttachmentFetcher {

    private final Set<String> allowlistHosts;
    private final long maxBytes;
    private final Duration timeout;
    private final HttpClient httpClient;

    public AttachmentFetcher(
            @Value("${anc.attachment.allowlist-hosts:bpm-stub}") String allowlistCsv,
            @Value("${anc.attachment.max-bytes:26214400}") long maxBytes,
            @Value("${anc.attachment.pull-timeout-ms:5000}") long pullTimeoutMs
    ) {
        this.allowlistHosts = parseAllowlist(allowlistCsv);
        this.maxBytes = maxBytes;
        this.timeout = Duration.ofMillis(pullTimeoutMs);
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(this.timeout)
                .build();
    }

    /**
     * Esegue il fetch dell'allegato, valida e ritorna il binario in memoria.
     * In caso di failure lancia IllegalStateException con messaggio human-readable;
     * il chiamante e' responsabile di trasformarla in AttachmentIngestionError.
     */
    public FetchedAttachment fetch(String url, String declaredExtension) {
        URI uri = parseUri(url);
        validateScheme(uri);
        validateHost(uri);

        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(timeout)
                .GET()
                .build();

        HttpResponse<InputStream> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException ex) {
            throw new IllegalStateException("Errore di rete durante il pull: " + ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Pull interrotto", ex);
        }

        int status = response.statusCode();
        if (status >= 300) {
            throw new IllegalStateException("HTTP status non valido sul pull: " + status);
        }

        String contentType = response.headers().firstValue("Content-Type").orElse(null);
        String normalizedContentType = normalizeContentType(contentType);
        validateContentTypeVsExtension(normalizedContentType, declaredExtension);

        byte[] bytes = readWithCap(response.body(), maxBytes);

        String sha256 = sha256Hex(bytes);
        return new FetchedAttachment(bytes, normalizedContentType, bytes.length, sha256);
    }

    /**
     * Esposto per test/diagnostica: indica se l'host della URL e' nell'allow-list.
     */
    public boolean isHostAllowed(String url) {
        try {
            URI uri = parseUri(url);
            validateScheme(uri);
            validateHost(uri);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private URI parseUri(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("URL LINKDOWNLOAD assente");
        }
        try {
            return new URI(url.trim());
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("URL LINKDOWNLOAD malformato: " + ex.getMessage(), ex);
        }
    }

    private void validateScheme(URI uri) {
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new IllegalStateException("Schema URL assente");
        }
        String s = scheme.toLowerCase(Locale.ROOT);
        if (!"http".equals(s) && !"https".equals(s)) {
            throw new IllegalStateException("Schema URL non consentito: " + scheme);
        }
    }

    private void validateHost(URI uri) {
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("Host URL assente");
        }
        if (!allowlistHosts.contains(host.toLowerCase(Locale.ROOT))) {
            throw new IllegalStateException("Host non in allow-list: " + host);
        }
    }

    private void validateContentTypeVsExtension(String contentType, String extension) {
        if (extension == null) {
            throw new IllegalStateException("Estensione dichiarata assente");
        }
        if (contentType == null) {
            throw new IllegalStateException("Content-Type assente nella risposta");
        }
        String ext = extension.toLowerCase(Locale.ROOT);
        String ct = contentType.toLowerCase(Locale.ROOT);
        List<String> expected = switch (ext) {
            case "pdf" -> List.of("application/pdf");
            case "jpg", "jpeg" -> List.of("image/jpeg");
            case "png" -> List.of("image/png");
            default -> throw new IllegalStateException("Estensione non supportata: " + extension);
        };
        boolean match = expected.stream().anyMatch(ct::equals);
        if (!match) {
            throw new IllegalStateException(
                    "Mismatch Content-Type vs estensione: ct=" + contentType + " ext=" + extension);
        }
    }

    private String normalizeContentType(String raw) {
        if (raw == null) {
            return null;
        }
        int semi = raw.indexOf(';');
        return (semi < 0 ? raw : raw.substring(0, semi)).trim().toLowerCase(Locale.ROOT);
    }

    private byte[] readWithCap(InputStream in, long cap) {
        try (InputStream stream = in) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            long total = 0;
            int read;
            while ((read = stream.read(chunk)) != -1) {
                total += read;
                if (total > cap) {
                    throw new IllegalStateException("Dimensione allegato oltre il limite: " + cap + " byte");
                }
                buffer.write(chunk, 0, read);
            }
            return buffer.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Errore lettura stream allegato: " + ex.getMessage(), ex);
        }
    }

    private String sha256Hex(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(bytes));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 non disponibile nel runtime", ex);
        }
    }

    private static Set<String> parseAllowlist(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }
}
