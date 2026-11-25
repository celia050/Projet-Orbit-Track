package com.orbittrack.backend.article;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

class ArticleAiServiceTest {

    @Test
    @EnabledIfEnvironmentVariable(
            named = "OLLAMA_TEST_URL",
            matches = ".+"
    )
    void shouldGenerateArticleWithOllama() {

        ArticleAiService aiService = new ArticleAiService(
                System.getenv("OLLAMA_TEST_URL"),
                "qwen2.5:3b"
        );

        String eventData = """
                Nom : Observation de Jupiter
                Type : Observation astronomique
                Description : Jupiter est la plus grande
                planète du système solaire.
                """;

        String result = aiService.generateArticle(eventData);

        assertNotNull(result);
        assertFalse(result.isBlank());

        System.out.println("Article généré :");
        System.out.println(result);
    }
}