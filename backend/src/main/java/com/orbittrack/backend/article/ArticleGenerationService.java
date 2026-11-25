package com.orbittrack.backend.article;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class ArticleGenerationService {

    private final ArticleEventRepository eventRepository;
    private final AstronomyFactService factService;
    private final ArticleService articleService;
    private final ArticleRepository articleRepository;
    private final ArticleAiService aiService;

    public ArticleGenerationService(
            ArticleEventRepository eventRepository,
            AstronomyFactService factService,
            ArticleService articleService,
            ArticleRepository articleRepository,
            ArticleAiService aiService) {

        this.eventRepository = eventRepository;
        this.factService = factService;
        this.articleService = articleService;
        this.articleRepository = articleRepository;
        this.aiService = aiService;
    }

    @Transactional
    public Article generateDraft() {

        LocalDate today = LocalDate.now(ZoneId.of("Europe/Paris"));

        // Vérifier si un article existe déjà aujourd'hui
        if (articleRepository.existsByArticleDate(today)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Un article existe déjà pour aujourd'hui"
            );
        }

        List<ArticleEvent> events =
                eventRepository.findUpcomingEvents();

        Article article = new Article();
        article.setArticleDate(today);

        if (events.isEmpty()) {

            // Aucun événement réel disponible
            AstronomyFact fact = factService.getRandomFact();

            article.setTitle(fact.title());

            article.setContent(
                fact.content()
                + "\n\nSource : "
                + fact.sourceName()
                + " - "
                + fact.sourceUrl()
            );

        } else {

            ArticleEvent event = events.get(0);

            String eventData =
                "Nom : " + event.name()
                + "\nType : " + event.type()
                + "\nDescription : " + event.description()
                + "\nDate de début : " + event.startAt()
                + "\nDate de fin : " + event.endAt()
                + "\nZone de visibilité : " + event.visibilityZone();

            String source =
                "\n\nSource : "
                + event.sourceName()
                + " - "
                + event.sourceUrl();

            try {

                // Génération de l'article par Mistral
                String generatedText =
                        aiService.generateArticle(eventData);

                // Séparer le titre et le contenu
                String[] parts =
                        generatedText.strip().split("\\R", 2);

                if (parts.length < 2 || parts[1].isBlank()) {
                    throw new IllegalStateException(
                        "Format de réponse IA incorrect"
                    );
                }

                article.setTitle(parts[0].strip());
                article.setContent(parts[1].strip() + source);

                System.out.println("Article généré par Ollama");

            } catch (RestClientException | IllegalStateException e) {

                // Solution de secours si Mistral est indisponible
                System.out.println(
                    "IA indisponible : génération sans IA"
                );

                article.setTitle(
                    "À découvrir : " + event.name()
                );

                article.setContent(
                    "Un événement astronomique est prévu : "
                    + event.name()
                    + ".\n\n"
                    + "Description : "
                    + event.description()
                    + "\n\n"
                    + "Zone de visibilité : "
                    + event.visibilityZone()
                    + source
                );
            }
        }

        return articleService.createDraft(article);
    }
}