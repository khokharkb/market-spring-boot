@ApplicationModule(
        type = ApplicationModule.Type.OPEN,
        displayName = "Annonce Module",
        allowedDependencies = {"user", "message", "panier", "favoris", "rating"}
)
package org.example.market.annonce;

import org.springframework.modulith.ApplicationModule;