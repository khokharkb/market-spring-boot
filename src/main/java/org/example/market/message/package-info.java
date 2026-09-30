@ApplicationModule(
        type = ApplicationModule.Type.OPEN,
        displayName = "Message Module",
        allowedDependencies = {"user", "annonce"}
)
package org.example.market.message;

import org.springframework.modulith.ApplicationModule;