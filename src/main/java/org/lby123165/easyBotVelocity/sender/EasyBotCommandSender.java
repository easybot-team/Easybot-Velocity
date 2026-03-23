package org.lby123165.easyBotVelocity.sender;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.NotNull;
import org.lby123165.easyBotVelocity.EasyBotVelocity;
import org.lby123165.easyBotVelocity.utils.LegacyTextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class EasyBotCommandSender implements CommandSource {
    private final List<String> feedbacks = new ArrayList<>();

    public List<String> getFeedbacks() {
        return feedbacks.stream().toList(); // 有必要复制一份,防止外部破坏内部List结构
    }

    @Override
    public Tristate getPermissionValue(String s) {
        return Tristate.TRUE;
    }

    @Override
    public void sendMessage(@NotNull SignedMessage signedMessage, ChatType.@NotNull Bound boundChatType) {
        sendMessage(Component.text(signedMessage.message()));
    }

    @Override
    public void sendMessage(@NotNull Component message) {
        if(message instanceof TranslatableComponent translatable) {
            if(GlobalTranslator.translator().canTranslate(translatable.key(), Locale.CHINA)) {
                Component translated = GlobalTranslator.renderer().render(translatable, Locale.CHINA);
                feedbacks.add(LegacyTextUtils.toString(translated));
            }else{
                String fallback = translatable.fallback();
                if(fallback == null) {
                    fallback = translatable.key();
                }
                feedbacks.add(fallback);
            }
        }else {
            feedbacks.add(LegacyTextUtils.toString(message));
        }
    }

    @Override
    public void sendMessage(@NotNull ComponentLike message) {
        sendMessage(message.asComponent());
    }

    @Override
    public void sendMessage(@NotNull ComponentLike message, ChatType.@NotNull Bound boundChatType) {
        sendMessage(message.asComponent());
    }

    @Override
    public void sendMessage(@NotNull Component message, ChatType.@NotNull Bound boundChatType) {
        sendMessage(message.asComponent());
    }
}
