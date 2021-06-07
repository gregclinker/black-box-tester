package com.alrayan.blackboxtester;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

import static java.util.Arrays.stream;

@Setter
public abstract class BBTHttpClient {

    protected int timeout = 10;
    protected boolean verbose = false;
    protected boolean production = true;
    protected String keyStorePath;
    private KeyStore keyStore;

    private CloseableHttpClient getHttpClient() throws Exception {
        keyStore = getAispKeyStore();

        SSLContext sslContext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, ApplicationProperties.INSTANCE.getKeyStorePassword().toCharArray())
                .loadTrustMaterial(keyStore, new TrustAllStrategy())
                .build();

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
                .setDefaultRequestConfig(config)
                .build();

        return httpClient;
    }

    protected KeyStore getAispKeyStore() throws Exception {
        if (keyStore == null) {
            String keyStoreName = ApplicationProperties.INSTANCE.getProdAispKeyStoreName();
            if (!production) {
                keyStoreName = ApplicationProperties.INSTANCE.getDevAispKeyStoreName();
            }
            InputStream file = getClass().getResourceAsStream("/" + keyStoreName);
            if (keyStorePath != null) {
                file = new FileInputStream(keyStorePath + "/" + keyStoreName);
            }
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(file, ApplicationProperties.INSTANCE.getKeyStorePassword().toCharArray());
        }
        return keyStore;
    }

    protected RSAPublicKey getAispPublicKey() throws Exception {
        return (RSAPublicKey) getAispKeyStore().getCertificate(ApplicationProperties.INSTANCE.getAispCertificateAlias()).getPublicKey();
    }

    protected RSAPrivateKey getAispPrivateKey() throws Exception {
        return (RSAPrivateKey) getPispKeyStore().getKey(ApplicationProperties.INSTANCE.getAispCertificateAlias(), ApplicationProperties.INSTANCE.getKeyStorePassword().toCharArray());
    }

    protected KeyStore getPispKeyStore() throws Exception {
        if (keyStore == null) {
            String keyStoreName = ApplicationProperties.INSTANCE.getProdPispKeyStoreName();
            if (!production) {
                keyStoreName = ApplicationProperties.INSTANCE.getDevPispKeyStoreName();
            }
            InputStream file = getClass().getResourceAsStream("/" + keyStoreName);
            if (keyStorePath != null) {
                file = new FileInputStream(keyStorePath + "/" + keyStoreName);
            }
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(file, ApplicationProperties.INSTANCE.getKeyStorePassword().toCharArray());
        }
        return keyStore;
    }

    protected RSAPublicKey getPispPublicKey() throws Exception {
        return (RSAPublicKey) getPispKeyStore().getCertificate(ApplicationProperties.INSTANCE.getPispCertificateAlias()).getPublicKey();
    }

    protected RSAPrivateKey getPispPrivateKey() throws Exception {
        return (RSAPrivateKey) getPispKeyStore().getKey(ApplicationProperties.INSTANCE.getPispCertificateAlias(), ApplicationProperties.INSTANCE.getKeyStorePassword().toCharArray());
    }

    public synchronized HttpResponse execute(HttpRequestBase request) throws Exception {
        HttpResponse response = getHttpClient().execute(request);
        if (verbose) System.out.println(request + " returned " + response.getStatusLine().getStatusCode());
        return response;
    }

    public HttpResponse logAndExecute(HttpRequestBase request, List<NameValuePair> params) throws Exception {
        log(request, params);
        return execute(request);
    }

    public void log(HttpRequestBase request, List<NameValuePair> params) {
        if (verbose) {
            System.out.println("\n" + request);
            System.out.println("headers [");
            stream(request.getAllHeaders()).forEach(e -> {
                System.out.println("\t" + e.getName() + " : " + e.getValue());
            });
            System.out.println("]");
            if (params != null) {
                System.out.println("body params [");
                params.stream().forEach(e -> System.out.println("\t" + e.getName() + " : " + e.getValue()));
                System.out.println("]");
            }
        }
    }

    protected void log(String message) {
        if (verbose) System.out.println(message);
    }

    protected JsonNode getStringFromJsonBody(HttpResponse response) throws IOException {
        String content = getBody(response);
        JsonNode jsonNode = new ObjectMapper().readTree(content);
        return jsonNode;
    }

    public String getBody(HttpResponse response) throws IOException {
        String content = EntityUtils.toString(response.getEntity());
        if (verbose) System.out.println("response body=" + content);
        return content;
    }
}
