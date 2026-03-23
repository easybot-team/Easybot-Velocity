package org.lby123165.easyBotVelocity.sender;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
        var legacyMessage = LegacyComponentSerializer.legacySection().serialize(message);
        feedbacks.add(legacyMessage);
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
