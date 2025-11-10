package com.csy.springbootauthbe.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;

import static org.junit.jupiter.api.Assertions.*;

class AwsConfigTest {

    private AwsConfig awsConfig;

    @BeforeEach
    void setUp() {
        awsConfig = new AwsConfig();
        // simulate @Value injection
        awsConfig.getClass().getDeclaredFields();
        awsConfig = new AwsConfig();
        awsConfig.getClass();
        // Reflection assignment
        TestUtils.setField(awsConfig, "accessKey", "dummy-access");
        TestUtils.setField(awsConfig, "secretKey", "dummy-secret");
        TestUtils.setField(awsConfig, "region", "us-east-1");
    }

    @Test
    void testS3Client_CreatesSuccessfully() {
        S3Client client = awsConfig.s3Client();
        assertNotNull(client);
        assertEquals("us-east-1", client.serviceClientConfiguration().region().id());
    }
}
