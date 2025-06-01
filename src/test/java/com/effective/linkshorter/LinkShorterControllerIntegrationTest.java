package com.effective.linkshorter;

import com.effective.linkshorter.entity.Link;
import com.effective.linkshorter.repository.LinkRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LinkShorterControllerIntegrationTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Очистим таблицу перед каждым тестом
        linkRepository.deleteAll();
    }

    @Test
    void postSourceLink_createsNewLink_andReturnsFilteredFields() throws Exception {
        // 1. Отправляем POST /sourceLink
        String payload = "{\"sourceLink\": \"https://integration.example.com/page\"}";

        String responseBody = mockMvc.perform(post("/sourceLink")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                // Проверяем, что в ответе есть поле id
                .andExpect(jsonPath("$.id").isNumber())
                // Проверяем, что есть поле linkWithUniqueIdentifier и оно начинается с http://localhost:8080/
                .andExpect(jsonPath("$.linkWithUniqueIdentifier").value(org.hamcrest.Matchers.startsWith("http://localhost:8080/")))
                // Проверяем, что sourceLink отфильтрован (его нет в ответе)
                .andExpect(jsonPath("$.sourceLink").doesNotExist())
                // Проверяем, что uniqueIdentifier отфильтрован (его нет в ответе)
                .andExpect(jsonPath("$.uniqueIdentifier").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 2. Разбираем JSON-ответ, чтобы достать linkWithUniqueIdentifier
        JsonNode root = objectMapper.readTree(responseBody);
        String shortLink = root.get("linkWithUniqueIdentifier").asText();
        assertThat(shortLink).startsWith("http://localhost:8080/");

        // 3. Извлекаем сам уникальный идентификатор (последняя часть URL)
        String uniqueId = shortLink.substring("http://localhost:8080/".length());
        assertThat(uniqueId).isNotBlank();

        // 4. Проверим, что в базе появился объект с этим uniqueIdentifier
        Optional<Link> saved = linkRepository.findByUniqueIdentifier(uniqueId);
        assertThat(saved).isPresent();
        assertThat(saved.get().getSourceLink()).isEqualTo("https://integration.example.com/page");
        assertThat(saved.get().getLinkWithUniqueIdentifier()).isEqualTo(shortLink);
    }

    @Test
    void getExistingUniqueIdentifier_redirectsToOriginal() throws Exception {
        // 1. Сохраним ссылку напрямую в БД
        Link link = new Link();
        link.setSourceLink("https://redirect.example.com/home");
        link.setUniqueIdentifier("REDIR123");
        link.setLinkWithUniqueIdentifier("http://localhost:8080/REDIR123");
        linkRepository.save(link);

        // 2. GET /REDIR123 должен вернуть 302 FOUND с header Location = https://redirect.example.com/home
        mockMvc.perform(get("/REDIR123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://redirect.example.com/home"));
    }

    @Test
    void getUnknownUniqueIdentifier_returnsNotFound() throws Exception {
        // Если в БД нет объекта с таким идентификатором, получаем 404
        mockMvc.perform(get("/NO_SUCH_ID"))
                .andExpect(status().isNotFound());
    }
}
