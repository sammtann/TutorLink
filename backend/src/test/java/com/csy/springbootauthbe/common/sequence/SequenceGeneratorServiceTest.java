package com.csy.springbootauthbe.common.sequence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SequenceGeneratorServiceTest {

    @Mock
    private MongoOperations mongoOperations;

    @InjectMocks
    private SequenceGeneratorService sequenceGeneratorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- getNextSequence ----------

    @Test
    void testGetNextSequence_ReturnsIncrementedValue() {
        Counter mockCounter = new Counter("studentId", 5);
        when(mongoOperations.findAndModify(
            any(Query.class),
            any(Update.class),
            any(FindAndModifyOptions.class),
            eq(Counter.class))
        ).thenReturn(mockCounter);

        long result = sequenceGeneratorService.getNextSequence("studentId");

        assertEquals(5, result);
    }

    @Test
    void testGetNextSequence_ReturnsOneWhenNull() {
        when(mongoOperations.findAndModify(any(), any(), any(), eq(Counter.class)))
            .thenReturn(null);

        long result = sequenceGeneratorService.getNextSequence("missingSeq");
        assertEquals(1, result);
    }

    // ---------- getNextStudentId ----------

    @Test
    void testGetNextStudentId_FormatsProperly() {
        Counter mockCounter = new Counter("studentId", 12);
        when(mongoOperations.findAndModify(any(), any(), any(), eq(Counter.class)))
            .thenReturn(mockCounter);

        String id = sequenceGeneratorService.getNextStudentId();
        assertEquals("S12", id);
    }

    // ---------- getNextEventId ----------

    @Test
    void testGetNextEventId_ReturnsLong() {
        Counter mockCounter = new Counter("eventId", 7);
        when(mongoOperations.findAndModify(any(), any(), any(), eq(Counter.class)))
            .thenReturn(mockCounter);

        Long id = sequenceGeneratorService.getNextEventId();
        assertEquals(7L, id);
    }

    // ---------- peekSequence ----------

    @Test
    void testPeekSequence_ReturnsExistingPlusOne() {
        Counter counter = new Counter("studentId", 3);
        when(mongoOperations.findOne(any(Query.class), eq(Counter.class)))
            .thenReturn(counter);

        long result = sequenceGeneratorService.peekSequence("studentId");
        assertEquals(4, result);
    }

    @Test
    void testPeekSequence_ReturnsOneIfMissing() {
        when(mongoOperations.findOne(any(), eq(Counter.class))).thenReturn(null);
        long result = sequenceGeneratorService.peekSequence("missing");
        assertEquals(1, result);
    }

    // ---------- peekNextStudentId ----------

    @Test
    void testPeekNextStudentId_FormatsProperly() {
        Counter counter = new Counter("studentId", 9);
        when(mongoOperations.findOne(any(Query.class), eq(Counter.class)))
            .thenReturn(counter);

        String id = sequenceGeneratorService.peekNextStudentId();
        assertEquals("S10", id);
    }
}
