package com.alrayan.blackboxtester;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AppTest {

    private static final Logger logger = LoggerFactory.getLogger(BlackBoxTestRunner.class);

    @Test
    public void test0() throws IOException {
        HttpTestSuite httpTestSuite = new HttpTestSuite();
        httpTestSuite.getContstants().add(new AlRayanNameValuePair("name", "value"));
        httpTestSuite.getContstants().add(new AlRayanNameValuePair("name", "value"));
        new ObjectMapper().writeValueAsString(httpTestSuite);
    }

    @Test
    public void test1() {
        List<HttpTest> httpTestList = new ArrayList<>();
        HttpTest httpTest = new HttpTest();
        httpTest.setMethod("GET");
        httpTest.setUrl("http://wso2dev02.bank.local:9081/testlogsummary/search/findFirst2ByTestSetUpIdOrderByTestStartTimeDesc?testSetUpId=4");
        httpTest.setExpected(new Expected());
        httpTest.getExpected().setHttpStatus(200);
        httpTest.getExpected().getContains().add("testFunctionURL");
        httpTest.getExpected().getContains().add("testAccountNo");
        httpTest.getHeaders().add(new HttpTestHeader("key", "value"));
        httpTestList.add(httpTest);
    }

    @Test
    public void test2() {

        HttpTestSuite httpTestSuite = new HttpTestSuite();
        HttpTest httpTest2 = new HttpTest();
        httpTest2.setUrl("http://wso2dev02.bank.local:9081/testlogsummary/search/findFirst2ByTestSetUpIdOrderByTestStartTimeDesc?testSetUpId=4");
        httpTest2.getHeaders().add(new HttpTestHeader("header1", "value1"));
        httpTest2.getHeaders().add(new HttpTestHeader("header2", "value2"));
        httpTest2.setMethod("GET");
        Expected expected = httpTest2.getExpected();
        expected.setHttpStatus(200);
        expected.getContains().add("this this");
        expected.getContains().add("and this");
        httpTestSuite.getHttpTests().add(httpTest2);
    }

    @Test
    public void test9() throws Exception {

        BlackBoxTestRunner blackBoxTestRunner = new BlackBoxTestRunner("test.json");

        HttpPost httpPost = new HttpPost("https://iam.dev02.bank.local:9446/eidas-cert-validator/services/validator/application-registration");

        File file = new File("qseal.pem");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addPart("certificate", new FileBody(file, ContentType.DEFAULT_BINARY));
        HttpEntity httpEntity = builder.build();
        httpPost.setEntity(httpEntity);
        HttpResponse httpResponse = blackBoxTestRunner.getAlRayanAPIClient().execute(httpPost);
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());

        blackBoxTestRunner.runTests();
        blackBoxTestRunner.runReport();
        assertTrue(blackBoxTestRunner.getHttpTestSuite().getHttpTests().get(0).isGood());
    }
}

