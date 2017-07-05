package com.manh.rest.http2;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import okhttp3.*;
import okio.Buffer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@RunWith(ConcurrentTestRunner.class)
@SpringBootTest
public class DemoApplicationTests {
    private final OkHttpClient client;

    public DemoApplicationTests() {
        X509TrustManager trustManager;
        SSLSocketFactory sslSocketFactory;
        try {
            trustManager = trustManagerForCertificates(trustedCertificatesInputStream());
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        client = new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustManager)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
                .readTimeout(0, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool())
                .build();

    }

//    @Test
    @ThreadCount(100)
    public void validateHttpWithNIOConnector() throws Exception {
        RestTemplate http2Template = new RestTemplate(clientHttpRequestFactory());

        ResponseEntity<String> responseEntity = http2Template.getForEntity("http://localhost:9910/hello?waitTimeSec=180", String.class);
        String responseBody = responseEntity.getBody();
        HttpHeaders headers = responseEntity.getHeaders();

        System.out.println("responseBody: " + responseBody);
        System.out.println("headers: " + headers.toString());
        System.out.println("status: " + responseEntity.getHeaders());
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(0);
        return factory;
    }

        @Test
    @ThreadCount(100)
    public void okHttpTest() throws IOException {
        Request request = new Request.Builder()
                .url("https://localhost:9900/hello")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
            }

            System.out.println(response.body().string());
        }
    }

    // Self signed certificate for testing.
    private InputStream trustedCertificatesInputStream() {
        String selfSignedCert = ""
                + "-----BEGIN CERTIFICATE-----\n"
                + "MIIDiDCCAnCgAwIBAgIEXp7Z6jANBgkqhkiG9w0BAQsFADBsMRAwDgYDVQQGEwdV\n"
                + "bmtub3duMRAwDgYDVQQIEwdVbmtub3duMRAwDgYDVQQHEwdVbmtub3duMRAwDgYD\n"
                + "VQQKEwdVbmtub3duMRAwDgYDVQQLEwdVbmtub3duMRAwDgYDVQQDEwdVbmtub3du\n"
                + "MB4XDTE2MTIwMzIzMzM0OVoXDTI3MTExNjIzMzM0OVowbDEQMA4GA1UEBhMHVW5r\n"
                + "bm93bjEQMA4GA1UECBMHVW5rbm93bjEQMA4GA1UEBxMHVW5rbm93bjEQMA4GA1UE\n"
                + "ChMHVW5rbm93bjEQMA4GA1UECxMHVW5rbm93bjEQMA4GA1UEAxMHVW5rbm93bjCC\n"
                + "ASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKLv8UND9NDA3gc7RY0DI8e9\n"
                + "prhF//PvRLdmWzao3IGe2Uf5ipr7y6Ly89Wr/Oyjcpt5xLR3rXVsvUmEM8xpQA3p\n"
                + "43C9BPNWNOFnoO3HZt2HSfNob4Yyd56kp8FB9DJ2pJIu0uBWsxhmrHi1op51xqha\n"
                + "B1uPwJE8NQNfDf9AkW41Owyze3MfQoscN0OOzhhzvMFIcrBJfQKcyiVBK365McY2\n"
                + "PPikKUdMasWrPQoFxZfI5V648c/JCvpRn5aaMzBNqSbN3n4WF7L6HGza1dVGTH5+\n"
                + "kHWJFTN+dsJEdx+KYQpXWsBgyOI1OYrTW4NweAvWqoKto/O5YMPxFV7iurOu/8EC\n"
                + "AwEAAaMyMDAwDwYDVR0RBAgwBocEwKgBBjAdBgNVHQ4EFgQUl6FlCcMg42wxArhO\n"
                + "8KnpVBl1kmEwDQYJKoZIhvcNAQELBQADggEBAHEKW71BBuJrUSaBpi21p6cfcOH9\n"
                + "m4SXfQXqD2XNgwX9WjZSDYCjRTaf+/ZGCDvdNcArixtOfxEr4ZvvR9TK04zkZHs/\n"
                + "O9qyAtvNTAXqlEfxYW0PoDreSPFBrXYZxbke+AD//zXQpeulJgabxifzr2/hh3CL\n"
                + "ljUJrlA3StRmQUlZql3fwDhNIVDCuzdbgjHjJjRG6TlOnf9+ceP0MFfbRkfNV8db\n"
                + "OAJmyM2FBcFr+00a2zsO1pO2tMVc91np2Yb0w6VPcsP5K5c/a19ZEcFS5v9hOjC4\n"
                + "tEORPTqi4NKo7gcnrVxvBikPBdmBv7Ruzq6FOu0pfWjwlwigJ8Nob/L9qxs=\n"
                + "-----END CERTIFICATE-----\n";
        return new Buffer()
                .writeUtf8(selfSignedCert)
                .inputStream();
    }

    private X509TrustManager trustManagerForCertificates(InputStream in)
            throws GeneralSecurityException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
        if (certificates.isEmpty()) {
            throw new IllegalArgumentException("expected non-empty set of trusted certificates");
        }

        // Put the certificates a key store.
        char[] password = "tomcat".toCharArray(); // Any password will work.
        KeyStore keyStore = newEmptyKeyStore(password);
        int index = 0;
        for (Certificate certificate : certificates) {
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificate);
        }

        // Use it to build an X509 trust manager.
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }
        return (X509TrustManager) trustManagers[0];
    }

    private KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream in = null; // By convention, 'null' creates an empty key store.
            keyStore.load(in, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }


}
