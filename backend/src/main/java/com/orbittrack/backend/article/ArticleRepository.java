package com.orbittrack.backend.article;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    // Récupérer tous les articles publiés, du plus récent au plus ancien
    List<Article> findByStatusOrderByArticleDateDesc(String status);

    // Récupérer les articles selon leur statut
    List<Article> findByStatus(String status);

    // Rechercher un article publié pour une date donnée
    Optional<Article> findFirstByArticleDateAndStatusOrderByIdDesc(
        LocalDate articleDate,
        String status
    );

    // Vérifier si un article existe déjà pour une date donnée
    boolean existsByArticleDate(LocalDate articleDate);
}
