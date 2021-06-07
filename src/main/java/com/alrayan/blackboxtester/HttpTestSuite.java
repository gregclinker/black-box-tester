package com.alrayan.blackboxtester;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class HttpTestSuite {
    @JsonIgnore
    private boolean verbose;
    private String description;
    private String gatewayAlrayanbank;
    @JsonIgnore
    private String refreshToken;
    @JsonIgnore
    private String accessToken;
    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private int timeout = 5;
    private int repeat = 1;
    private int threads = 1;
    private int delay = 0;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<AlRayanNameValuePair> contstants = new ArrayList<>();
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<HttpTest> httpTests = new ArrayList<>();

    public void setTags() {
        for (HttpTest httpTest : httpTests) {
            httpTest.setUrl(httpTest.getUrl().replaceAll("\\$\\{gatewayAlrayanbank}", gatewayAlrayanbank));
        }
        for (HttpTest httpTest : httpTests) {
            for (AlRayanNameValuePair alRayanNameValuePair : contstants) {
                httpTest.setUrl(httpTest.getUrl().replaceAll("\\$\\{" + alRayanNameValuePair.getName() + "}", alRayanNameValuePair.getValue()));
            }
        }
    }

    public synchronized String getAccessToken() {
        return accessToken;
    }

    public synchronized void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @JsonIgnore
    public String getSummary() {
        return "HttpTestSuite{" + "verbose=" + verbose +
                ", description='" + description + '\'' +
                ", gatewayAlrayanbank='" + gatewayAlrayanbank + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", redirectUrl='" + redirectUrl + '\'' +
                ", timeout=" + timeout + "s" +
                ", repeat=" + repeat +
                ", threads=" + threads +
                ", delay=" + delay +
                ", contstants=" + contstants +
                '}';
    }
}
