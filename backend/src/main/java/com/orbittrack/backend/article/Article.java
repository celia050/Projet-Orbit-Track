package com.orbittrack.backend.article;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "articles")
public class Article {

    // Identifiant unique de l'article
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Titre de l'article
    @Column(nullable = false, length = 200)
    private String title;

    // Contenu de l'article
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // Date concernée par l'article
    @Column(name = "article_date", nullable = false)
    private LocalDate articleDate;

    // Statut : DRAFT, PUBLISHED ou REJECTED
    @Column(nullable = false, length = 20)
    private String status = "DRAFT";

    // Date de création
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    // Date de dernière modification
    @Column(name = "updated_at", insertable = false)
    private OffsetDateTime updatedAt;

    // Date de publication
    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    // Constructeur vide obligatoire pour JPA
    public Article() {
    }

    // GETTERS ET SETTERS

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDate getArticleDate() {
        return articleDate;
    }

    public void setArticleDate(LocalDate articleDate) {
        this.articleDate = articleDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(OffsetDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}
