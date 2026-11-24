package com.orbittrack.backend.forum;

import com.orbittrack.backend.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Feature 8 : forum de chaque sortie. Toutes les routes demandent d'être connecté (voir SecurityConfig). */
@RestController
public class ForumController {

    private final ForumService service;

    public ForumController(ForumService service) {
        this.service = service;
    }

    @GetMapping("/api/events/{eventId}/forum")
    public ForumPage list(@PathVariable Long eventId,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size,
                          @AuthenticationPrincipal Jwt jwt) {
        return service.list(eventId, CurrentUser.id(jwt), CurrentUser.isAdmin(jwt), page, size);
    }

    @PostMapping("/api/events/{eventId}/forum")
    @ResponseStatus(HttpStatus.CREATED)
    public ForumMessageResponse post(@PathVariable Long eventId, @Valid @RequestBody ForumMessageRequest request,
                                     @AuthenticationPrincipal Jwt jwt) {
        return service.post(eventId, CurrentUser.id(jwt), CurrentUser.isAdmin(jwt), request.content());
    }

    @PutMapping("/api/forum/messages/{messageId}")
    public ForumMessageResponse edit(@PathVariable Long messageId, @Valid @RequestBody ForumMessageRequest request,
                                     @AuthenticationPrincipal Jwt jwt) {
        return service.edit(messageId, CurrentUser.id(jwt), CurrentUser.isAdmin(jwt), request.content());
    }

    /** L'auteur supprime son message, ou un administrateur le retire (modération). */
    @DeleteMapping("/api/forum/messages/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long messageId, @AuthenticationPrincipal Jwt jwt) {
        service.delete(messageId, CurrentUser.id(jwt), CurrentUser.isAdmin(jwt));
    }
}
