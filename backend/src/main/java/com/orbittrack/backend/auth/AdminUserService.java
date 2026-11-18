package com.orbittrack.backend.auth;

import com.orbittrack.backend.common.BusinessRuleException;
import com.orbittrack.backend.security.AdminProperties;
import com.orbittrack.backend.user.AppUser;
import com.orbittrack.backend.user.AppUserRepository;
import com.orbittrack.backend.user.Role;
import com.orbittrack.backend.user.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Gestion des rôles par un administrateur. */
@Service
public class AdminUserService {

    private static final Logger log = LoggerFactory.getLogger(AdminUserService.class);

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return users.findAll(Sort.by("createdAt")).stream().map(UserResponse::from).toList();
    }

    /** Règle : le site doit toujours garder au moins un administrateur. */
    @Transactional
    public UserResponse changeRole(Long userId, Role newRole) {
        AppUser user = users.findById(userId)
                .orElseThrow(() -> new BusinessRuleException(HttpStatus.NOT_FOUND, "Utilisateur introuvable."));
        if (user.getRole() == Role.ADMIN && newRole != Role.ADMIN && users.countByRole(Role.ADMIN) <= 1) {
            throw new BusinessRuleException(HttpStatus.CONFLICT,
                    "Impossible de retirer le dernier administrateur du site.");
        }
        user.setRole(newRole);
        return UserResponse.from(user);
    }

    /**
     * Au démarrage : crée le premier administrateur (ADMIN_EMAIL / ADMIN_PASSWORD)
     * s'il n'existe encore aucun admin. Si un compte existe déjà avec cet email, il devient admin.
     */
    @Transactional
    public void ensureFirstAdmin(AdminProperties admin) {
        if (!admin.isConfigured() || users.countByRole(Role.ADMIN) > 0) {
            return;
        }
        String email = AuthService.normalizeEmail(admin.email());
        AppUser user = users.findByEmail(email).orElseGet(() ->
                new AppUser(email, passwordEncoder.encode(admin.password()), "Administrateur", Role.ADMIN));
        user.setRole(Role.ADMIN);
        users.save(user);
        log.info("Premier administrateur prêt : {}", email);
    }
}
