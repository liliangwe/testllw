package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void helloworldReturnsSuccessWithDefaultName() throws Exception {
        mockMvc.perform(get("/api/demo/helloworld"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.msg", is("SUCCESS")))
                .andExpect(jsonPath("$.data.message", is("Hello, World!")));
    }

    @Test
    void helloworldUsesProvidedName() throws Exception {
        mockMvc.perform(get("/api/demo/helloworld").param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.message", is("Hello, Alice!")));
    }

    @Test
    void helloworldRejectsNameOver64Chars() throws Exception {
        String longName = "a".repeat(65);
        mockMvc.perform(get("/api/demo/helloworld").param("name", longName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("ALG_001")));
    }

    @Test
    void hashReturnsSha256Digest() throws Exception {
        mockMvc.perform(post("/api/demo/hash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"abc\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.algorithm", is("SHA-256")))
                .andExpect(jsonPath("$.data.digest",
                        is("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")));
    }

    @Test
    void hashRejectsEmptyText() throws Exception {
        mockMvc.perform(post("/api/demo/hash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("ALG_002")));
    }

    @Test
    void bubbleSortReturnsSortedAndSwaps() throws Exception {
        mockMvc.perform(post("/api/demo/bubble-sort")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"array\":[5,3,8,1]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.sorted[0]", is(1)))
                .andExpect(jsonPath("$.data.sorted[1]", is(3)))
                .andExpect(jsonPath("$.data.sorted[2]", is(5)))
                .andExpect(jsonPath("$.data.sorted[3]", is(8)))
                .andExpect(jsonPath("$.data.swaps", is(4)));
    }

    @Test
    void bubbleSortRejectsEmptyArray() throws Exception {
        mockMvc.perform(post("/api/demo/bubble-sort")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"array\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("ALG_003")));
    }

    @Test
    void bubbleSortRejectsNonNumericElement() throws Exception {
        mockMvc.perform(post("/api/demo/bubble-sort")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"array\":[1,2,\"x\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("ALG_004")));
    }

    @Test
    void bubbleSortRejectsArrayOver1000() throws Exception {
        StringBuilder sb = new StringBuilder("{\"array\":[");
        for (int i = 0; i < 1001; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(1);
        }
        sb.append("]}");
        mockMvc.perform(post("/api/demo/bubble-sort")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sb.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("ALG_005")));
    }

    @Test
    void reportRejectsInvalidDimension() throws Exception {
        mockMvc.perform(get("/api/demo/tracking/report").param("dimension", "bogus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("RPT_001")));
    }

    @Test
    void exportRejectsInvalidTab() throws Exception {
        mockMvc.perform(post("/api/demo/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tab\":\"bogus\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("EXP_001")));
    }
}