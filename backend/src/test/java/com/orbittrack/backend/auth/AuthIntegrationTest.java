package com.orbittrack.backend.auth;

import com.jayway.jsonpath.JsonPath;
import com.orbittrack.backend.TestcontainersConfiguration;
import com.orbittrack.backend.user.AppUserRepository;
import com.orbittrack.backend.user.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste la feature 12 de bout en bout : HTTP → sécurité → service → vraie base PostgreSQL.
 * Chaque test utilise un email unique, pour ne pas dépendre de l'ordre des tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AuthIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@test.local";
    private static final String ADMIN_PASSWORD = "mot-de-passe-admin-test";

    @Autowired
    MockMvc mvc;

    @Autowired
    AppUserRepository users;

    // ---------- Inscription ----------

    @Test
    void inscriptionCreeUnCompteUserEtRenvoieUnJeton() throws Exception {
        String email = uniqueEmail();

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(email, "motdepasse123", "Astro Fan")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(not(emptyString())))
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());

        assertThat(users.findByEmail(email)).get()
                .satisfies(u -> assertThat(u.getPasswordHash()).isNotEqualTo("motdepasse123"));
    }

    @Test
    void inscriptionRefuseUnEmailDejaUtiliseMemeAvecDesMajuscules() throws Exception {
        String email = uniqueEmail();
        register(email, "motdepasse123");

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(email.toUpperCase(), "autremotdepasse", "Doublon")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(AuthService.EMAIL_TAKEN));
    }

    @Test
    void inscriptionRefuseUnFormulaireInvalide() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("pas-un-email", "court", "A")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.displayName").exists());
    }

    // ---------- Connexion ----------

    @Test
    void connexionAvecLesBonsIdentifiants() throws Exception {
        String email = uniqueEmail();
        register(email, "motdepasse123");

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("  " + email.toUpperCase() + " ", "motdepasse123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(not(emptyString())))
                .andExpect(jsonPath("$.user.email").value(email));
    }

    @Test
    void connexionRefuseeAvecLeMemeMessageQueLEmailExisteOuNon() throws Exception {
        String email = uniqueEmail();
        register(email, "motdepasse123");

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, "mauvais-mot-de-passe")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value(AuthService.BAD_CREDENTIALS));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(uniqueEmail(), "motdepasse123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value(AuthService.BAD_CREDENTIALS));
    }

    // ---------- Règles d'accès ----------

    @Test
    void consulterNeDemandePasDeCompte() throws Exception {
        // Aucune route GET publique n'existe encore : on obtient 404 (et pas 401), donc l'accès est libre.
        mvc.perform(get("/api/events")).andExpect(status().isNotFound());
    }

    @Test
    void profilAccessibleSeulementConnecte() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());

        String email = uniqueEmail();
        String token = register(email, "motdepasse123");
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void lesDonneesPersonnellesDemandentUnCompteMemeEnLecture() throws Exception {
        mvc.perform(get("/api/me/notifications")).andExpect(status().isUnauthorized());
    }

    @Test
    void motDePasseTropLongPourBcryptRefuseProprement() throws Exception {
        String tropLong = "é".repeat(40); // 40 caractères mais 80 octets
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(uniqueEmail(), tropLong, "Accents")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(AuthService.PASSWORD_TOO_LONG));
    }

    @Test
    void jetonFalsifieRefuse() throws Exception {
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer pas.un.jeton"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ecrireDemandeDEtreConnecte() throws Exception {
        // Une route POST inconnue renvoie 401 sans jeton : la règle « il faut un compte » s'applique.
        mvc.perform(post("/api/events/1/participation")).andExpect(status().isUnauthorized());
    }

    @Test
    void administrationReserveeAuxAdmins() throws Exception {
        String userToken = register(uniqueEmail(), "motdepasse123");
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk());
    }

    @Test
    void unAdminPeutPromouvoirUnUtilisateur() throws Exception {
        String email = uniqueEmail();
        register(email, "motdepasse123");
        Long id = users.findByEmail(email).orElseThrow().getId();

        mvc.perform(patch("/api/admin/users/" + id + "/role").header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        // Le nouveau rôle est pris en compte à la connexion suivante.
        String newToken = login(email, "motdepasse123");
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + newToken))
                .andExpect(status().isOk());

        // Remise en état pour les autres tests.
        users.findByEmail(email).ifPresent(u -> {
            u.setRole(Role.USER);
            users.save(u);
        });
    }

    @Test
    void impossibleDeRetirerLeDernierAdmin() throws Exception {
        // On s'assure que l'admin de test est le seul admin.
        users.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN && !u.getEmail().equals(ADMIN_EMAIL))
                .forEach(u -> {
                    u.setRole(Role.USER);
                    users.save(u);
                });
        assertThat(users.countByRole(Role.ADMIN)).isEqualTo(1);
        Long adminId = users.findByEmail(ADMIN_EMAIL).orElseThrow().getId();

        mvc.perform(patch("/api/admin/users/" + adminId + "/role").header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"USER\"}"))
                .andExpect(status().isConflict());
    }

    // ---------- Outils ----------

    private String register(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(email, password, "Testeur")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private String login(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private String adminToken() throws Exception {
        return login(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    private static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@test.local";
    }

    private static String registerJson(String email, String password, String displayName) {
        return """
                {"email":"%s","password":"%s","displayName":"%s"}""".formatted(email, password, displayName);
    }

    private static String loginJson(String email, String password) {
        return """
                {"email":"%s","password":"%s"}""".formatted(email, password);
    }
}
