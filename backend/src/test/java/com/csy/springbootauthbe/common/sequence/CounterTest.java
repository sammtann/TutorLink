package com.csy.springbootauthbe.common.sequence;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CounterTest {

    @Test
    void testAllArgsConstructorAndGetters() {
        Counter c = new Counter("studentId", 10);
        assertEquals("studentId", c.getId());
        assertEquals(10, c.getSeq());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        Counter c = new Counter();
        c.setId("eventId");
        c.setSeq(99);

        assertEquals("eventId", c.getId());
        assertEquals(99, c.getSeq());
    }

    @Test
    void testEqualsAndHashCodeAndToString() {
        Counter c1 = new Counter("abc", 1);
        Counter c2 = new Counter("abc", 1);
        Counter c3 = new Counter("xyz", 2);

        assertEquals(c1, c2);
        assertNotEquals(c1, c3);
        assertTrue(c1.toString().contains("abc"));
        assertEquals(c1.hashCode(), c2.hashCode());
    }
}
