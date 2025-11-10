package com.csy.springbootauthbe.common.aws;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AwsResponseTest {

    @Test
    void testAllArgsConstructorAndGetters() {
        AwsResponse res = new AwsResponse("hash123", "key/file.png");
        assertEquals("hash123", res.getHash());
        assertEquals("key/file.png", res.getKey());
    }

    @Test
    void testBuilder() {
        AwsResponse res = AwsResponse.builder()
            .hash("hashABC")
            .key("profile/img.jpg")
            .build();

        assertEquals("hashABC", res.getHash());
        assertEquals("profile/img.jpg", res.getKey());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        AwsResponse res = new AwsResponse();
        res.setHash("xyz");
        res.setKey("file.txt");

        assertEquals("xyz", res.getHash());
        assertEquals("file.txt", res.getKey());
    }

    @Test
    void testEqualsAndHashCode() {
        AwsResponse r1 = new AwsResponse("a", "b");
        AwsResponse r2 = new AwsResponse("a", "b");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString() {
        AwsResponse res = new AwsResponse("123", "k.png");
        String result = res.toString();

        assertTrue(result.contains("123"));
        assertTrue(result.contains("k.png"));
    }
}
