package com.orbittrack.backend.article;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    // Injection du Repository
    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    // Récupérer tous les articles publiés
    @Transactional(readOnly = true)
    public List<Article> getPublishedArticles() {
        return articleRepository
                .findByStatusOrderByArticleDateDesc("PUBLISHED");
    }

    // Récupérer les brouillons
    @Transactional(readOnly = true)
    public List<Article> getDraftArticles() {
        return articleRepository.findByStatus("DRAFT");
    }

    // Récupérer tous les articles (administrateur)
    @Transactional(readOnly = true)
    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    // Récupérer l'article publié du jour
    @Transactional(readOnly = true)
    public Article getTodayArticle() {
        return articleRepository
                .findFirstByArticleDateAndStatusOrderByIdDesc(
                        LocalDate.now(ZoneId.of("Europe/Paris")),
                        "PUBLISHED"
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Aucun article publié aujourd'hui"
                ));
    }

    // Créer un brouillon
    @Transactional
    public Article createDraft(Article article) {
        article.setStatus("DRAFT");
        article.setPublishedAt(null);

        if (article.getArticleDate() == null) {
            article.setArticleDate(LocalDate.now(ZoneId.of("Europe/Paris")));
        }

        return articleRepository.save(article);
    }

    // Modifier un brouillon
    @Transactional
    public Article updateDraft(Long id, Article updatedArticle) {
        Article article = findArticle(id);

        if (!"DRAFT".equals(article.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seul un brouillon peut être modifié"
            );
        }

        article.setTitle(updatedArticle.getTitle());
        article.setContent(updatedArticle.getContent());
        article.setArticleDate(updatedArticle.getArticleDate());

        return articleRepository.save(article);
    }

    // Publier un brouillon après validation
    @Transactional
    public Article publishArticle(Long id) {
        Article article = findArticle(id);

        if (!"DRAFT".equals(article.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seul un brouillon peut être publié"
            );
        }

        article.setStatus("PUBLISHED");
        article.setPublishedAt(OffsetDateTime.now());

        return articleRepository.save(article);
    }

    // Rejeter un brouillon
    @Transactional
    public Article rejectArticle(Long id) {
        Article article = findArticle(id);

        if (!"DRAFT".equals(article.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seul un brouillon peut être rejeté"
            );
        }

        article.setStatus("REJECTED");

        return articleRepository.save(article);
    }

    // Rechercher un article par son identifiant
    private Article findArticle(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Article introuvable"
                ));
    }
}