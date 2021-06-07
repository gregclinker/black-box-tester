package com.alrayan.blackboxtester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public enum ApplicationProperties {
    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(ApplicationProperties.class);

    private final Properties properties;

    ApplicationProperties() {
        properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDevAispKeyStoreName() {
        return properties.getProperty("dgateway.aisp.keystore.name");
    }

    public String getProdAispKeyStoreName() {
        return properties.getProperty("gateway.aisp.keystore.name");
    }

    public String getDevPispKeyStoreName() {
        return properties.getProperty("dgateway.pisp.keystore.name");
    }

    public String getProdPispKeyStoreName() {
        return properties.getProperty("gateway.pisp.keystore.name");
    }

    public String getKeyStorePassword() {
        return properties.getProperty("keystore.password");
    }

    public String getAispCertificateAlias() {
        return properties.getProperty("aisp.certificate.alias");
    }

    public String getPispCertificateAlias() {
        return properties.getProperty("pisp.certificate.alias");
    }
}