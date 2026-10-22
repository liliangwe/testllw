package com.example.todo.controller;

import com.example.todo.TodoApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TodoController 集成测试。
 *
 * <p>使用 MockMvc 对 POST /api/todos 进行端到端测试。</p>
 */
@SpringBootTest(classes = TodoApplication.class)
@AutoConfigureMockMvc
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateTodoAndReturn201WithTodoJson() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "name", "买菜",
                "description", "买青菜和豆腐"
        ));

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id", not(emptyString())))
                .andExpect(jsonPath("$.name", is("买菜")))
                .andExpect(jsonPath("$.description", is("买青菜和豆腐")))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.createdAt", not(emptyString())));
    }

    @Test
    void shouldReturn400WhenNameIsEmpty() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "name", ""
        ));

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenNameIsMissing() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "description", "只有描述"
        ));

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGenerateUniqueUuidsForEachCreation() throws Exception {
        String body1 = objectMapper.writeValueAsString(Map.of("name", "事项A"));
        String body2 = objectMapper.writeValueAsString(Map.of("name", "事项B"));

        String id1 = mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body1))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id2 = mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body2))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String uuid1 = objectMapper.readTree(id1).get("id").asText();
        String uuid2 = objectMapper.readTree(id2).get("id").asText();

        assert !uuid1.equals(uuid2) : "Expected different UUIDs but got same: " + uuid1;
    }

    @Test
    void shouldCreateTodoWithDescriptionOnly() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "name", "跑步",
                "description", "晨跑5公里"
        ));

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("跑步")))
                .andExpect(jsonPath("$.description", is("晨跑5公里")));
    }

    @Test
    void shouldAcceptOptionalDescriptionField() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "name", "仅名称"
        ));

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("仅名称")))
                .andExpect(jsonPath("$.description", is("")));
    }
}