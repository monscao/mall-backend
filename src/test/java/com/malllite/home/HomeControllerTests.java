package com.malllite.home;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void homeShouldReturnAppleLiteStructure() throws Exception {
        mockMvc.perform(get("/api/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theme").value("apple-lite"))
                .andExpect(jsonPath("$.hero.title").exists())
                .andExpect(jsonPath("$.featuredCategories.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.sections.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.sections[0].layout").exists());
    }
}
