package com.alrayan.blackboxtester;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

@ToString
public class BBTAPIClient extends BBTHttpClient {

    final static Logger logger = LoggerFactory.getLogger(BBTHttpClient.class);

    private String gatewayAlrayanbank;
    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private String audience;
    private static final int TOKEN_LIFE_TIME_IN_SECONDS = 36500000;

    public BBTAPIClient(String gatewayAlrayanbank, String clientId, String clientSecret, String redirectUrl) {
        this.gatewayAlrayanbank = gatewayAlrayanbank;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUrl = redirectUrl;
        this.audience = "https://" + gatewayAlrayanbank + "/token";
        if (gatewayAlrayanbank.contains("dgateway")) {
            production = false;
        }
    }

    public String getConsentUrl() throws Exception {
        String accessToken = requestAccessToken();
        log("access_token=" + accessToken + "\n");

        String consentId = requestConsent(accessToken);
        System.out.println("consentId=" + consentId + "\n");

        return getAuthoriseEndPoint(consentId);
    }

    public String getRefreshToken(String code) throws Exception {
        HttpPost httpPost = new HttpPost("https://" + gatewayAlrayanbank + "/token");

        List<NameValuePair> params = new ArrayList<>(2);
        params.add(new BasicNameValuePair("grant_type", "authorization_code"));
        params.add(new BasicNameValuePair("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"));
        params.add(new BasicNameValuePair("client_assertion", getAispSignedJWT()));
        params.add(new BasicNameValuePair("code", code));
        params.add(new BasicNameValuePair("scope", "accounts"));
        params.add(new BasicNameValuePair("redirect_uri", redirectUrl));
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        HttpResponse response = logAndExecute(httpPost, params);

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("error getRefreshToken returned " + response.getStatusLine().getStatusCode());
        }

        return getStringFromJsonBody(response).get("refresh_token").asText();
    }

    public String getToken(String refreshToken) throws Exception {
        HttpPost httpPost = new HttpPost("https://" + gatewayAlrayanbank + "/token");

        List<NameValuePair> params = new ArrayList<>(2);
        params.add(new BasicNameValuePair("client_id", clientId));
        params.add(new BasicNameValuePair("grant_type", "refresh_token"));
        params.add(new BasicNameValuePair("client_secret", clientSecret));
        params.add(new BasicNameValuePair("refresh_token", refreshToken));
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        HttpResponse response = logAndExecute(httpPost, params);

        if (response.getStatusLine().getStatusCode() != 200) {
            String content = EntityUtils.toString(response.getEntity());
            logger.error("error getToken returned " + response.getStatusLine().getStatusCode() + ", body=" + content);
            throw new RuntimeException("error getToken returned " + response.getStatusLine().getStatusCode() + " : body=" + content);
        }

        return getStringFromJsonBody(response).get("access_token").asText();
    }

    public String getAuthoriseEndPoint(String consentId) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, TOKEN_LIFE_TIME_IN_SECONDS);
        Map<String, String> map = new HashMap<>();
        map.put("audience", audience);
        map.put("client_id", clientId);
        map.put("response_type", "code id_token");
        map.put("redirecturl", redirectUrl);
        map.put("scope", "accounts openid");
        map.put("state", "YWlzcDozMTQ2");
        map.put("nonce", "n-0S6_WzA2M");
        map.put("prompt", "login");
        map.put("ConsentId", consentId);
        map.put("expiration", calendar.getTimeInMillis() + "");
        map.put("max_age", TOKEN_LIFE_TIME_IN_SECONDS + "");
        String payLoad = IOUtils.toString(Objects.requireNonNull(getClass().getResourceAsStream("/authoriseEndPoint.json")), Charset.defaultCharset());
        for (String key : map.keySet()) {
            payLoad = payLoad.replaceAll("\\$\\{" + key + "}", map.get(key));
        }

        Payload contentPayload = new Payload(payLoad);
        if (verbose)
            System.out.println(contentPayload.toString());

        String keyID = UUID.randomUUID().toString();
        JWK jwk = new RSAKey.Builder(getAispPublicKey()).keyID(keyID).build();

        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwk.getKeyID()).build();
        JWSObject jws = new JWSObject(jwsHeader, contentPayload);

        JWSSigner signer = new RSASSASigner(getAispPrivateKey());
        jws.sign(signer);
        System.out.println("authorise endoint JWT=" + jws.serialize() + "\n");

        String url = "https://" + gatewayAlrayanbank + "/authorize/?response_type=${response_type}&client_id=${client_id}&scope=${scope}&redirect_uri=${redirecturl}&state=${state}&request=${JWTValue1}&prompt=${prompt}&nonce=${nonce}";
        for (String key : map.keySet())
            url = url.replaceAll("\\$\\{" + key + "}", URLEncoder.encode(map.get(key), StandardCharsets.UTF_8.toString()));
        url = url.replaceAll("\\$\\{JWTValue1}", jws.serialize());
        return url;
    }

    public String requestAccessToken() throws Exception {
        HttpPost httpPost = new HttpPost("https://" + gatewayAlrayanbank + "/token");
        List<NameValuePair> params = new ArrayList<>(2);
        params.add(new BasicNameValuePair("grant_type", "client_credentials"));
        params.add(new BasicNameValuePair("scope", "am_application_scope accounts openid"));
        params.add(new BasicNameValuePair("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"));
        params.add(new BasicNameValuePair("client_assertion", getAispSignedJWT()));
        params.add(new BasicNameValuePair("redirect_uri", redirectUrl));
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        HttpResponse response = logAndExecute(httpPost, params);

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("requestAccessToken failed returned " + response.getStatusLine().getStatusCode() + ", body=" + EntityUtils.toString(response.getEntity()));
        }

        return getStringFromJsonBody(response).get("access_token").asText();
    }

    public String getAispSignedJWT() throws Exception {
        return getSignedJWT(clientId, audience, getAispPublicKey(), getAispPrivateKey()).serialize();
    }

    public String getPispSignedJWT() throws Exception {
        return getSignedJWT(clientId, audience, getPispPublicKey(), getPispPrivateKey()).serialize();
    }

    public String requestConsent(String accessToken) throws Exception {
        HttpPost httpPost = new HttpPost("https://" + gatewayAlrayanbank + "/open-banking/v3.1/aisp/account-access-consents");

        String jsonBody = IOUtils.toString(Objects.requireNonNull(getClass().getResourceAsStream("/consent.json")), Charset.defaultCharset());
        httpPost.setHeader("Authorization", "Bearer " + accessToken);
        httpPost.setHeader("x-fapi-financial-id", "open-bank");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(jsonBody));

        HttpResponse response = execute(httpPost);

        return getStringFromJsonBody(response).get("Data").get("ConsentId").asText();
    }

    private SignedJWT getSignedJWT(String clientId, String audience, RSAPublicKey publicKey, RSAPrivateKey privateKey) throws Exception {
        String keyID = UUID.randomUUID().toString();
        JWK jwk = new RSAKey.Builder(publicKey).keyID(keyID).build();

        // Prepare JWT with claims set
        Calendar calendar = Calendar.getInstance();
        Date issueTime = calendar.getTime();
        calendar.add(Calendar.SECOND, TOKEN_LIFE_TIME_IN_SECONDS);
        Date expirationTime = calendar.getTime();
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(clientId)
                .issuer(clientId)
                .jwtID(issueTime.getTime() + "")
                .issueTime(issueTime)
                .notBeforeTime(issueTime)
                .expirationTime(expirationTime)
                .audience(audience)
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwk.getKeyID()).build(),
                claimsSet);

        // Create RSA-signer with the private key
        JWSSigner signer = new RSASSASigner(privateKey);
        signedJWT.sign(signer);

        if (verbose) {
            System.out.println(signedJWT.getJWTClaimsSet().toString());
        }

        return signedJWT;
    }
}
