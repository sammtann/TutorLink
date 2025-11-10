package com.csy.springbootauthbe.common.sequence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SequenceGeneratorControllerTest {

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @InjectMocks
    private SequenceGeneratorController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testGetNextStudentId_Returns200AndBody() throws Exception {
        when(sequenceGeneratorService.peekNextStudentId()).thenReturn("S25");

        mockMvc.perform(get("/api/v1/seq/next-id")
                .accept(MediaType.TEXT_PLAIN))
            .andExpect(status().isOk())
            .andExpect(content().string("S25"));

        verify(sequenceGeneratorService, times(1)).peekNextStudentId();
    }
}
