package com.orbittrack.backend.auth;

import com.orbittrack.backend.common.BusinessRuleException;
import com.orbittrack.backend.security.JwtService;
import com.orbittrack.backend.user.AppUser;
import com.orbittrack.backend.user.AppUserRepository;
import com.orbittrack.backend.user.Role;
import com.orbittrack.backend.user.UserResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Inscription, connexion et profil de l'utilisateur connecté. */
@Service
public class AuthService {

    /** Même message que l'email existe ou non : on ne révèle pas qui a un compte. */
    static final String BAD_CREDENTIALS = "Email ou mot de passe incorrect.";
    static final String EMAIL_TAKEN = "Un compte existe déjà avec cet email.";
    static final String PASSWORD_TOO_LONG = "Le mot de passe est trop long (72 octets maximum).";

    /** BCrypt ne prend en compte que les 72 premiers octets (un caractère accentué en compte 2). */
    static final int BCRYPT_MAX_BYTES = 72;

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** Toute inscription crée un compte USER. Seul un admin peut ensuite donner le rôle ADMIN. */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (tooLongForBcrypt(request.password())) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, PASSWORD_TOO_LONG);
        }
        String email = normalizeEmail(request.email());
        if (users.existsByEmail(email)) {
            throw new BusinessRuleException(HttpStatus.CONFLICT, EMAIL_TAKEN);
        }
        AppUser user = new AppUser(email, passwordEncoder.encode(request.password()),
                request.displayName().trim(), Role.USER);
        try {
            user = users.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            // Deux inscriptions simultanées avec le même email : la contrainte d'unicité tranche.
            throw new BusinessRuleException(HttpStatus.CONFLICT, EMAIL_TAKEN);
        }
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        if (tooLongForBcrypt(request.password())) {
            throw new BadCredentialsException(BAD_CREDENTIALS);
        }
        AppUser user = users.findByEmail(normalizeEmail(request.email()))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException(BAD_CREDENTIALS));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(Long userId) {
        return users.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new BusinessRuleException(HttpStatus.UNAUTHORIZED,
                        "Ce compte n'existe plus. Reconnectez-vous."));
    }

    private static boolean tooLongForBcrypt(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES;
    }

    static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private AuthResponse toResponse(AppUser user) {
        JwtService.IssuedToken token = jwtService.issue(user);
        return new AuthResponse(token.value(), token.expiresAt(), UserResponse.from(user));
    }
}
