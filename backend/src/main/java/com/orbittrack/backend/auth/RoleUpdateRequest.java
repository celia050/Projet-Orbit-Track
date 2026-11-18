package com.orbittrack.backend.auth;

import com.orbittrack.backend.user.Role;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(@NotNull(message = "Le rôle est obligatoire (USER ou ADMIN).") Role role) {
}
