package com.alrayan.blackboxtester;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class BlackBoxTestRunner {

    final static Logger logger = LoggerFactory.getLogger(BlackBoxTestRunner.class);

    private HttpTestSuite httpTestSuite;
    private String inputFile;
    private Map<String, List<LoadTestResult>> loadResults = new ConcurrentHashMap<>();
    private BBTAPIClient alRayanAPIClient;

    private Date loadTestStart;
    private int accessTokenReUseCount = 0;

    public BlackBoxTestRunner(String inputFile) throws Exception {
        this(new FileInputStream(inputFile));
        this.inputFile = inputFile;
    }

    public BlackBoxTestRunner(InputStream inputStream) throws Exception {
        String jsonInput = IOUtils.toString(inputStream, Charset.defaultCharset());
        httpTestSuite = new ObjectMapper().readValue(jsonInput, HttpTestSuite.class);
        for (AlRayanNameValuePair nameValuePair : httpTestSuite.getContstants()) {
            if (nameValuePair.getName().equals("accessToken")) {
                httpTestSuite.setAccessToken(nameValuePair.getValue());
            }
        }
        alRayanAPIClient = new BBTAPIClient(httpTestSuite.getGatewayAlrayanbank(), httpTestSuite.getClientId(), httpTestSuite.getClientSecret(), httpTestSuite.getRedirectUrl());
        alRayanAPIClient.setVerbose(httpTestSuite.isVerbose());
        if (httpTestSuite.getGatewayAlrayanbank().contains("dgateway")) {
            alRayanAPIClient.production = false;
        }
        for (HttpTest httpTest : httpTestSuite.getHttpTests()) {
            httpTest.setUrl(httpTest.getUrl().replaceAll("\\$\\{gatewayAlrayanbank\\}", httpTestSuite.getGatewayAlrayanbank()));
            for (AlRayanNameValuePair constant : httpTestSuite.getContstants()) {
                httpTest.setUrl(httpTest.getUrl().replaceAll("\\$\\{" + constant.getName() + "\\}", constant.getValue()));
            }
        }
    }

    public void runReport() {
        if (isLoadTest()) {
            return;
        }
        int passed = 0;
        int failed = 0;
        long totalExecutionTime = 0;
        long averageExecutionTime = 0;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Inclusion.NON_NULL);
        objectMapper.setSerializationInclusion(Inclusion.NON_EMPTY);
        for (HttpTest httpTest : httpTestSuite.getHttpTests()) {
            if (httpTest.isGood()) {
                System.out.println(httpTest.getSummary() + " in " + httpTest.getHttpTestResult().getExecutionTime() + "ms" + " - OK");
                totalExecutionTime += httpTest.getHttpTestResult().getExecutionTime();
                passed++;
            }
        }
        System.out.println("\n");
        for (HttpTest httpTest : httpTestSuite.getHttpTests()) {
            if (!httpTest.isGood()) {
                String asString = null;
                try {
                    asString = objectMapper.writeValueAsString(httpTest);
                } catch (IOException e) {
                    asString = httpTest.toString();
                }
                System.out.println(httpTest.getDescription() + " - FAILED\n" + asString + "\n");
                failed++;
            }
        }
        int total = failed + passed;
        if (passed > 0) {
            averageExecutionTime = totalExecutionTime / passed;
        }
        System.out.println("ran=" + total + ", passed=" + passed + "(aveage execution time=" + averageExecutionTime + "ms)" + ", failed=" + failed);
    }

    public HttpTestSuite runTests() throws Exception {
        System.out.print(httpTestSuite.getSummary());
        Random r = new Random();
        logger.debug(httpTestSuite.getSummary());
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < httpTestSuite.getThreads(); i++) {
            if (isLoadTest()) {
                Thread.sleep((long) (r.nextInt(6) * 1000));
            }
            System.out.print("\nstarting thread " + i + ", Running " + httpTestSuite.getHttpTests().size() + " tests ");
            BlackBoxTest blackBoxTest = new BlackBoxTest(httpTestSuite, loadResults, i);
            Thread thread = new Thread(blackBoxTest);
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("\nfinished all threads");
        return httpTestSuite;
    }

    private boolean isLoadTest() {
        return httpTestSuite.getRepeat() > 1 || httpTestSuite.getThreads() > 1;
    }

    public void getConsentUrl() throws Exception {
        System.out.println("consentUrl=" + alRayanAPIClient.getConsentUrl());
    }

    public void getRefreshToken(String code) throws Exception {
        String refreshToken = alRayanAPIClient.getRefreshToken(code);
        System.out.println("\nrefresh token=" + refreshToken);
        changeRefreshToken(refreshToken);
    }

    public void changeRefreshToken(String refreshToken) throws IOException {
        String jsonInput = IOUtils.toString(new FileInputStream(inputFile), Charset.defaultCharset());
        String newJson = jsonInput.replaceAll("\"refreshToken\": \".*\"", "\"refreshToken\": \"" + refreshToken + "\"");
        FileUtils.writeStringToFile(new File(inputFile), newJson, Charset.defaultCharset());
    }

    public void getToken() throws Exception {
        System.out.println("\naccess token=" + alRayanAPIClient.getToken(httpTestSuite.getRefreshToken()));
    }
}
