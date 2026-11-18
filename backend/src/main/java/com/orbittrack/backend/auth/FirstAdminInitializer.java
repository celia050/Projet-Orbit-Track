package com.orbittrack.backend.auth;

import com.orbittrack.backend.security.AdminProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Lance la création du premier administrateur au démarrage de l'application. */
@Component
class FirstAdminInitializer implements ApplicationRunner {

    private final AdminUserService adminUserService;
    private final AdminProperties adminProperties;

    FirstAdminInitializer(AdminUserService adminUserService, AdminProperties adminProperties) {
        this.adminUserService = adminUserService;
        this.adminProperties = adminProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        adminUserService.ensureFirstAdmin(adminProperties);
    }
}
