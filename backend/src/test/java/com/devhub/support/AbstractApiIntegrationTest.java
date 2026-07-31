package com.devhub.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@AutoConfigureMockMvc
public abstract class AbstractApiIntegrationTest extends AbstractIntegrationTest {

    protected MockMvcTester mvc;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void createMockMvcTester() {
        mvc = MockMvcTester.create(mockMvc);
    }
}