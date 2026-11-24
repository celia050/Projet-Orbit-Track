package com.orbittrack.backend.alert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByUserIdOrderByCreatedAtDescIdDesc(Long userId);

    long countByUserId(Long userId);

    Optional<Alert> findByIdAndUserId(Long id, Long userId);

    List<Alert> findByActiveTrue();
}
