package com.alrayan.blackboxtester;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HttpTestResult {
    private String responseBody;
    private int responseStatusCode;
    private String exception;
    private Date startTime;
    private Date endTime;

    public String getResponseBody() {
        if (responseBody != null) {
            return responseBody.replaceAll("\\s+", " ").replaceAll("\\s+([\\{\\}\\[\\]\"])", "$1");
        }
        return null;
    }

    public long getExecutionTime() {
        if (startTime == null || endTime == null) {
            return 0l;
        }
        return endTime.getTime() - startTime.getTime();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HttpTestResult{");
        sb.append("responseBody='").append(getResponseBody()).append('\'');
        sb.append(", responseStatusCode=").append(responseStatusCode);
        if (exception != null) {
            sb.append(", exception='").append(exception).append('\'');
        }
        sb.append(", executionTime=").append(getExecutionTime());
        sb.append('}');
        return sb.toString();
    }
}
