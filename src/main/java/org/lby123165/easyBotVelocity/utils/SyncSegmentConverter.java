package org.lby123165.easyBotVelocity.utils;
import com.springwater.easybot.bridge.message.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.lby123165.easyBotVelocity.EasyBotVelocity;

import java.util.*;

public class SyncSegmentConverter {
    public static List<Segment> mergeSegments(List<Segment> segments) {
        Queue<Segment> queue = new LinkedList<>(segments);
        StringBuilder currentText = new StringBuilder();
        List<Segment> segmentsToAdd = new ArrayList<>();
        while (!queue.isEmpty()) {
            Segment segment = queue.poll();
            if (segment instanceof TextSegment) {
                currentText.append(segment.getText());  // 直接用StringBuilder追加文本
            } else {
                if (!currentText.isEmpty()) {  // 如果有文本累积，则添加合并的TextSegment
                    TextSegment combinedTextSegment = new TextSegment();
                    combinedTextSegment.setText(currentText.toString());
                    segmentsToAdd.add(combinedTextSegment);
                    currentText.setLength(0);  // 重置StringBuilder
                }
                segmentsToAdd.add(segment);  // 直接添加非TextSegment的部分
            }
        }
        if (!currentText.isEmpty()) {
            TextSegment combinedTextSegment = new TextSegment();
            combinedTextSegment.setText(currentText.toString());
            segmentsToAdd.add(combinedTextSegment);
        }
        return segmentsToAdd;
    }
    public static Component toComponent(Segment segment) {
        Component component = LegacyTextUtils.toComponent(segment.getText());

        if (segment instanceof AtSegment atSegment) {
            component = component.color(NamedTextColor.GOLD);
            String[] atPlayerNames = atSegment.getAtPlayerNames();
            if (!Objects.equals(atSegment.getAtUserId(), "0")) {
                Component hoverComponent = Component.text()
                        .append(Component.text("@"))
                        .append(Component.text(atSegment.getAtUserName()))
                        .append(Component.text(" ("))
                        .append(Component.text(atSegment.getAtUserId()))
                        .append(Component.text(")"))
                        .append(
                                atPlayerNames.length > 1 ?
                                        Component.text("\n该玩家绑定了" + atPlayerNames.length + "个账号\n" + String.join(",", atPlayerNames))
                                        : Component.empty()
                        )
                        .build();

                component = component.hoverEvent(hoverComponent);
            }
        } else if (segment instanceof ImageSegment imageSegment) {
            component = component.color(NamedTextColor.GREEN);
            if (EasyBotVelocity.getInstance().getConfig().sync.chatImageSupport) {
                component = component.hoverEvent(
                        Component.text("[[CICode,url=" + imageSegment.getUrl() + ",name=" + imageSegment.getSummary() + "]]")
                );
            } else {
                component = component.hoverEvent(
                        LegacyTextUtils.toComponent("§7§n点击预览 §7§n" + imageSegment.getUrl())
                );
            }
            component = component.clickEvent(ClickEvent.openUrl(imageSegment.getUrl()));
        } else if (segment instanceof FileSegment fileSegment) {
            component = component.color(NamedTextColor.GOLD);
            component = component.hoverEvent(
                    LegacyTextUtils.toComponent("§7§n点击下载 §7§n" + fileSegment.getFileUrl())
            );
            component = component.clickEvent(ClickEvent.openUrl(fileSegment.getFileUrl()));
        } else if (segment instanceof FaceSegment) {
            component = component.color(NamedTextColor.GREEN);
        } else {
            component = component.color(NamedTextColor.WHITE);
        }

        return component;
    }
}