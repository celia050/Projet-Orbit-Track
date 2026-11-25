package com.orbittrack.backend.article;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class ArticleScheduler {

    private final ArticleGenerationService generationService;
    private final ArticleRepository articleRepository;

    public ArticleScheduler(
            ArticleGenerationService generationService,
            ArticleRepository articleRepository) {

        this.generationService = generationService;
        this.articleRepository = articleRepository;
    }

    @Scheduled(cron = "0 0 2 * * *", zone = "Europe/Paris")
    public void generateDailyArticle() {    

        System.out.println("Scheduler : vérification quotidienne");

        LocalDate today = LocalDate.now(
            ZoneId.of("Europe/Paris")
        );

        if (articleRepository.existsByArticleDate(today)) {
            System.out.println("Un article existe déjà aujourd'hui");
            return;
        }

        generationService.generateDraft();

        System.out.println("Brouillon généré automatiquement");
    }
}