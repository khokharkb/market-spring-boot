@ApplicationModule(
        type = ApplicationModule.Type.OPEN,
        displayName = "Admin Module",
        allowedDependencies = {"user", "annonce"}
)
package org.example.market.admin;

import org.springframework.modulith.ApplicationModule;