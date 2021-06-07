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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HttpTest {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<HttpTestHeader> headers = new ArrayList<>();
    private String description;
    private String url;
    private String method;
    private String body;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<AlRayanNameValuePair> bodyParams = new ArrayList<>(2);
    private Expected expected = new Expected();
    private HttpTestResult httpTestResult;
    @JsonIgnore
    private boolean withToken = true;
    @JsonIgnore
    private Boolean forceResult;

    @JsonIgnore
    public boolean isGood() {
        if (forceResult != null) {
            return forceResult;
        } else if (httpTestResult != null) {
            return isGood(httpTestResult);
        }
        return false;
    }

    @JsonIgnore
    public boolean isGood(HttpTestResult httpTestResult) {
        boolean ok = true;
        ok = httpTestResult.getResponseStatusCode() == expected.getHttpStatus();
        for (String contains : expected.getContains()) {
            ok = ok && httpTestResult.getResponseBody().contains(contains);
        }
        return ok;
    }

    @JsonIgnore
    public String getSummary() {
        return description + " (" + method + ":" + url + ")";
    }
}
