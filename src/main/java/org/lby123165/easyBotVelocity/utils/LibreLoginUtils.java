package org.lby123165.easyBotVelocity.utils;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.jetbrains.annotations.Nullable;
import org.lby123165.easyBotVelocity.EasyBotVelocity;
import xyz.kyngs.librelogin.api.LibreLoginPlugin;
import xyz.kyngs.librelogin.api.provider.LibreLoginProvider;

public class LibreLoginUtils {
    private static @Nullable LibreLoginPlugin<Player, RegisteredServer> libreLoginPlugin = null;

    public static boolean hasLiberLogin() {
        return EasyBotVelocity.getInstance().getServer().getPluginManager().isLoaded("librelogin");
    }

    @SuppressWarnings("unchecked")
    public static boolean isAuthenticated(Player proxyPlayer) {
        if (!hasLiberLogin()) {
            return true;
        } else {
            if (libreLoginPlugin == null) {
                libreLoginPlugin = ((LibreLoginProvider<Player, RegisteredServer>) EasyBotVelocity.getInstance().getServer().getPluginManager().getPlugin("librelogin").orElseThrow().getInstance().orElseThrow()).getLibreLogin();
            }
            return libreLoginPlugin.getAuthorizationProvider().isAuthorized(proxyPlayer);
        }
    }
}
