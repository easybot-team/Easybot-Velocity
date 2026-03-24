package org.lby123165.easyBotVelocity.utils;
import net.elytrium.limboauth.LimboAuth;
import org.lby123165.easyBotVelocity.EasyBotVelocity;

public class LimboAuthUtils {
    private static LimboAuth limboAuth = null;
    public static LimboAuth getLimboAuth() {
        if (limboAuth == null) {
            limboAuth = (LimboAuth) EasyBotVelocity.getInstance().getServer().getPluginManager().getPlugin("limboauth").orElseThrow().getInstance().orElseThrow();
        }
        return limboAuth;
    }

    public static boolean hasLimboAuth() {
        return EasyBotVelocity.getInstance().getServer().getPluginManager().isLoaded("limboauth");
    }

    @SuppressWarnings("unchecked")
    public static boolean isAuthenticated(String playerName) {
        if (!hasLimboAuth()) {
            return true;
        }
        return getLimboAuth().getAuthenticatingPlayer(playerName) == null;
    }
}
