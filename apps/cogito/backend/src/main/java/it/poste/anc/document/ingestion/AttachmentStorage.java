package it.poste.anc.document.ingestion;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Adapter MinIO/S3 per la persistenza binaria allegati (GAP-BLOCKER-001).
 *
 * Implementazione: AWS SDK v2 S3Client con path-style access (richiesto da MinIO).
 * URI canonica persistita su attachment.storage_uri: s3://<bucket>/<key>.
 * Auto-create del bucket al primo accesso se non esiste.
 */
@Service
public class AttachmentStorage {

    private static final Logger LOG = LoggerFactory.getLogger(AttachmentStorage.class);
    private static final String S3_SCHEME = "s3://";

    private final S3Client s3Client;
    private final String bucket;

    public AttachmentStorage(
            @Value("${anc.attachment.storage.endpoint:http://minio:9000}") String endpoint,
            @Value("${anc.attachment.storage.region:us-east-1}") String region,
            @Value("${anc.attachment.storage.bucket:anc-attachments}") String bucket,
            @Value("${anc.attachment.storage.access-key:anc}") String accessKey,
            @Value("${anc.attachment.storage.secret-key:anc-poc-minio-secret}") String secretKey
    ) {
        this.bucket = bucket;
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    @PostConstruct
    void ensureBucket() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (NoSuchBucketException ex) {
            createBucketQuietly();
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                createBucketQuietly();
            } else {
                LOG.warn("Verifica bucket {} non riuscita allo startup: {}. Tentativo lazy a runtime.",
                        bucket, ex.getMessage());
            }
        } catch (RuntimeException ex) {
            LOG.warn("Verifica bucket {} non riuscita allo startup: {}. Tentativo lazy a runtime.",
                    bucket, ex.getMessage());
        }
    }

    /**
     * Scrive il binario su MinIO sotto la key indicata e ritorna l'URI canonica.
     */
    public String put(String key, byte[] bytes, String contentType) {
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .contentLength((long) bytes.length)
                            .build(),
                    RequestBody.fromBytes(bytes));
        } catch (NoSuchBucketException ex) {
            createBucketQuietly();
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .contentLength((long) bytes.length)
                            .build(),
                    RequestBody.fromBytes(bytes));
        }
        return S3_SCHEME + bucket + "/" + key;
    }

    /**
     * Recupera il binario partendo dall'URI canonica s3://bucket/key.
     */
    public byte[] getBytes(String storageUri) {
        ParsedUri parsed = parse(storageUri);
        ResponseBytes<GetObjectResponse> resp = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                .bucket(parsed.bucket())
                .key(parsed.key())
                .build());
        return resp.asByteArray();
    }

    public String bucket() {
        return bucket;
    }

    private void createBucketQuietly() {
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            LOG.info("Creato bucket MinIO '{}'.", bucket);
        } catch (S3Exception ex) {
            // bucket potrebbe essere stato creato da un'altra istanza in race condition
            LOG.warn("createBucket({}) ha risposto {}: {}", bucket, ex.statusCode(), ex.getMessage());
        }
    }

    private ParsedUri parse(String storageUri) {
        if (storageUri == null || !storageUri.startsWith(S3_SCHEME)) {
            throw new IllegalArgumentException("storage_uri non in formato s3://: " + storageUri);
        }
        try {
            URI uri = new URI(storageUri);
            String host = uri.getHost();
            String path = uri.getPath();
            if (host == null || path == null || path.length() <= 1) {
                throw new IllegalArgumentException("storage_uri incompleto: " + storageUri);
            }
            return new ParsedUri(host, path.substring(1));
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("storage_uri malformato: " + storageUri, ex);
        }
    }

    private record ParsedUri(String bucket, String key) {
    }
}
