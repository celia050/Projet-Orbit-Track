package com.orbittrack.backend.article;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleGenerationService generationService;

    public ArticleController(
            ArticleService articleService,
            ArticleGenerationService generationService) {

        this.articleService = articleService;
        this.generationService = generationService;
    }

    // =========================
    // ROUTES PUBLIQUES
    // =========================

    // Consulter les articles publiés
    @GetMapping("/api/articles")
    public List<Article> getPublishedArticles() {
        return articleService.getPublishedArticles();
    }

    // Consulter l'article publié du jour
    @GetMapping("/api/articles/today")
    public Article getTodayArticle() {
        return articleService.getTodayArticle();
    }

    // =========================
    // ROUTES ADMINISTRATEUR
    // =========================

    // Consulter tous les articles
    @GetMapping("/api/admin/articles")
    public List<Article> getAllArticles() {
        return articleService.getAllArticles();
    }

    // Consulter les brouillons
    @GetMapping("/api/admin/articles/drafts")
    public List<Article> getDraftArticles() {
        return articleService.getDraftArticles();
    }

    // Créer un brouillon
    @PostMapping("/api/admin/articles")
    @ResponseStatus(HttpStatus.CREATED)
    public Article createDraft(@RequestBody Article article) {
        return articleService.createDraft(article);
    }

    // Générer automatiquement un brouillon
    @PostMapping("/api/admin/articles/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public Article generateArticle() {
        return generationService.generateDraft();
    }

    // Modifier un brouillon
    @PutMapping("/api/admin/articles/{id}")
    public Article updateDraft(
            @PathVariable Long id,
            @RequestBody Article article) {

        return articleService.updateDraft(id, article);
    }

    // Publier un article
    @PostMapping("/api/admin/articles/{id}/publish")
    public Article publishArticle(@PathVariable Long id) {
        return articleService.publishArticle(id);
    }

    // Rejeter un article
    @PostMapping("/api/admin/articles/{id}/reject")
    public Article rejectArticle(@PathVariable Long id) {
        return articleService.rejectArticle(id);
    }
}
