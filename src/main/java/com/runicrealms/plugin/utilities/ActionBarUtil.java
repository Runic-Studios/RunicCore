package com.runicrealms.plugin.utilities;

import com.runicrealms.plugin.RunicCore;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionBarUtil {

    public static void sendTimedMessage(Player pl, String message, int seconds) {
        new BukkitRunnable() {
            long startTime = System.currentTimeMillis();
            @Override
            public void run() {
                long time = System.currentTimeMillis();
                if ((time - startTime) >= seconds*1000) {
                    this.cancel();
                } else {
                    pl.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ColorUtil.format(message)));
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 1L);
    }
}
