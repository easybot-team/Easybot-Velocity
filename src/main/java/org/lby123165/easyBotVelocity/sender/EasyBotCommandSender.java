package org.lby123165.easyBotVelocity.sender;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.permission.PermissionChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.lby123165.easyBotVelocity.utils.LegacyTextUtils;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

public class EasyBotCommandSender implements CommandSource, PermissionChecker, ConsoleCommandSource {
    private final List<String> feedbacks = new CopyOnWriteArrayList<>();
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    private final Object lock = new Object();
    private CompletableFuture<List<String>> completionFuture;
    private ScheduledFuture<?> timeoutTask;

    public List<String> getFeedbacks() {
        return List.copyOf(feedbacks);
    }

    public CompletableFuture<List<String>> awaitForMessages() {
        synchronized (lock) {
            if (completionFuture == null) {
                completionFuture = new CompletableFuture<>();
                // 启动初始计时器（防止命令执行后一条消息都没发的情况）
                refreshSlidingWindow();
            }
        }
        return completionFuture;
    }
    private void refreshSlidingWindow() {
        synchronized (lock) {
            // 如果已经完成，则不再接收处理
            if (completionFuture != null && completionFuture.isDone()) {
                return;
            }
            if (timeoutTask != null && !timeoutTask.isDone()) {
                timeoutTask.cancel(false);
            }
            timeoutTask = SCHEDULER.schedule(() -> {
                synchronized (lock) {
                    if (completionFuture != null && !completionFuture.isDone()) {
                        // 1秒内没有新消息，认为命令执行结束，完成 Future
                        completionFuture.complete(getFeedbacks());
                    }
                }
            }, 1, TimeUnit.SECONDS);
        }
    }

    @Override
    public Tristate getPermissionValue(String s) {
        return Tristate.TRUE;
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public void sendMessage(@NotNull SignedMessage signedMessage, ChatType.@NotNull Bound boundChatType) {
        sendMessage(Component.text(signedMessage.message()));
    }

    @Override
    public void sendMessage(@NotNull Component message) {
        if (message instanceof TranslatableComponent translatable) {
            if (GlobalTranslator.translator().canTranslate(translatable.key(), Locale.CHINA)) {
                Component translated = GlobalTranslator.renderer().render(translatable, Locale.CHINA);
                feedbacks.add(LegacyTextUtils.toString(translated));
            } else {
                String fallback = translatable.fallback();
                if (fallback == null) {
                    fallback = translatable.key();
                }
                feedbacks.add(fallback);
            }
        } else {
            feedbacks.add(LegacyTextUtils.toString(message));
        }
        refreshSlidingWindow();
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

    @Override
    public void sendRichMessage(@NotNull String message) {
        sendMessage(Component.text(message));
    }

    @Override
    public void sendPlainMessage(@NotNull String message) {
        sendMessage(Component.text(message));
    }

    @Override
    public void sendRichMessage(@NotNull String message, @NotNull TagResolver @NotNull ... resolvers) {
        sendMessage(Component.text(message));
    }

    @Override
    public PermissionChecker getPermissionChecker() {
        return this;
    }

    @Override
    public @NotNull TriState value(@NotNull String permission) {
        return TriState.TRUE;
    }

    @Override
    public boolean test(@NotNull String permission) {
        return hasPermission(permission);
    }
}