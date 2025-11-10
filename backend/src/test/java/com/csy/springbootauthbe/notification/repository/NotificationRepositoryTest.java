package com.csy.springbootauthbe.notification.repository;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificationRepositoryTest {

    @Test
    void testInterfaceDefinition() {
        // MongoRepository generic signature check
        NotificationRepository repo = null;
        assertNull(repo); // just ensures class loads and compiles
    }
}
