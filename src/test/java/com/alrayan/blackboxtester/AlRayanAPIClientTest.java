package com.alrayan.blackboxtester;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class AlRayanAPIClientTest {

    @Test
    public void testGetAispSignedJWT() throws Exception {
        BBTAPIClient alRayanAPIClient = new BBTAPIClient("gateway02.alrayanbank.co.uk", "erSLRhpiVlPek4QBZPEFNwOFJ8ga", null, "https://www.google.com");
        alRayanAPIClient.setKeyStorePath("C:\\opensource\\apache-jmeter-5.2.1\\jwtExtResources-PISP");
        String jwt = alRayanAPIClient.getPispSignedJWT();
        assertNotNull(jwt);
    }
}
