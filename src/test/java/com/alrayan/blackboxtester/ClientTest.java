package com.alrayan.blackboxtester;

import org.junit.Before;
import org.junit.Test;

public class ClientTest {

    public String gatewayAlrayanbank = "gateway02.alrayanbank.co.uk";
    private BBTAPIClient alRayanApiClient;
    private String accountId = "30008346340601";
    private String code = "f157b215-4921-3bbf-bc18-c9bf2f9e3d2e";
    private String refreshToken = "10b13322-89c9-3f72-9459-cef6e615c3ee";
    private String clientId = "miHtDXKbXPlL1At1laWYwb22gsca";
    private String clientSecret = "TdmWnbTdYy32UJoG14cRNSkkgf0a";
    private String redirectUrl = "https://www.google.com";

    @Before
    public void init() {
        alRayanApiClient = new BBTAPIClient(gatewayAlrayanbank, clientId, clientSecret, redirectUrl);
        alRayanApiClient.setVerbose(true);
    }

    @Test
    public void getConsentUrl() throws Exception {
        System.out.println(alRayanApiClient.getConsentUrl());
    }

    @Test
    public void test1() {

        String json = "{\n" +
                "  \"Data\": {\n" +
                "    \"Account\": [\n" +
                "      {\n" +
                "        \"AccountId\": \"30008349046735\",\n" +
                "        \"Currency\": \"GBP\",\n" +
                "        \"AccountType\": \"EI\",\n" +
                "        \"AccountSubType\": \"\",\n" +
                "        \"Nickname\": \"DDDDDDDDDD\",\n" +
                "        \"Account\": {\n" +
                "          \"SchemeName\": \"SortCodeAccountNumber\",\n" +
                "          \"Identification\": \"30008349046735\",\n" +
                "          \"Name\": \"CUSTOMER 490467\",\n" +
                "          \"SecondaryIdentification\": \"\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"Links\": {\n" +
                "    \"Self\": \"https://dgateway.alrayanbank.co.uk/open-banking/v3.1/aisp/accounts/30008349046735\"\n" +
                "  },\n" +
                "  \"Meta\": {\n" +
                "    \"TotalPages\": 1\n" +
                "  }\n" +
                "}";

        System.out.println(json.replaceAll("\\s+", " "));
        System.out.println(json.replaceAll("\\s+", " ").replaceAll("\\s+([\\{\\}\\[\\]\"])","$1"));
    }

}
