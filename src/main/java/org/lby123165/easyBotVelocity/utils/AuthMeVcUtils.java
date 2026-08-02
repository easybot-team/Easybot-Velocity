package org.lby123165.easyBotVelocity.utils;

import com.velocitypowered.api.proxy.Player;
import io.github._4drian3d.authmevelocity.api.velocity.AuthMeVelocityAPI;
import org.jetbrains.annotations.Nullable;
import org.lby123165.easyBotVelocity.EasyBotVelocity;

public class AuthMeVcUtils {
    private static @Nullable AuthMeVelocityAPI authMeVelocityAPI = null;

    public static boolean hasAuthMeVelocity() {
        return EasyBotVelocity.getInstance().getServer().getPluginManager().isLoaded("authmevelocity");
    }

    public static AuthMeVelocityAPI getAuthMeVelocityAPI() {
        if (authMeVelocityAPI == null) {
            authMeVelocityAPI = (AuthMeVelocityAPI) EasyBotVelocity.getInstance().getServer().getPluginManager().getPlugin("authmevelocity").orElseThrow().getInstance().orElseThrow();
        }
        return authMeVelocityAPI;
    }

    @SuppressWarnings("unchecked")
    public static boolean isAuthenticated(Player proxyPlayer) {
        if (!hasAuthMeVelocity()) {
            return true;
        }
        return getAuthMeVelocityAPI().isLogged(proxyPlayer);
    }
}
