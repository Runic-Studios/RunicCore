package com.runicrealms.plugin.utilities;

import com.nametagedit.plugin.NametagEdit;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.NameTagEvent;
import com.runicrealms.plugin.events.EnvironmentDamage;
import com.runicrealms.plugin.events.HealthRegenEvent;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.tablist.TabListManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class NametagHandler implements Listener {

    /**
     * Calls a new name tag event
     *
     * @param player to update
     * @param slot   of the character
     */
    public static void updateNametag(Player player, int slot) {
        ChatColor prefixColor = player.getLevel() >= PlayerLevelUtil.getMaxLevel() ? ChatColor.GOLD : ChatColor.GREEN;
        // Call task sync
        Bukkit.getScheduler().runTask(RunicCore.getInstance(),
                () -> {
                    Bukkit.getPluginManager().callEvent(new NameTagEvent
                            (
                                    player,
                                    slot,
                                    prefixColor,
                                    ChatColor.RESET,
                                    "[" + player.getLevel() + "]"
                            ));
                });
    }

    @EventHandler
    public void onGenericDamage(EnvironmentDamage event) {
        if (!(event.getVictim() instanceof Player player)) return;
        updateHealthBar(player);
    }

    @EventHandler
    public void onHealthRegen(HealthRegenEvent event) {
        updateHealthBar(event.getPlayer());
    }

    @EventHandler
    public void onMagicDamage(MagicDamageEvent event) {
        if (!(event.getVictim() instanceof Player player)) return;
        updateHealthBar(player);
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent event) {
        if (!(event.getVictim() instanceof Player player)) return;
        updateHealthBar(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST) // Last thing to run
    public void onNameTagEvent(NameTagEvent event) {
        if (event.isCancelled()) return;
        final String finalName = event.getPrefixColor() + event.getNametag() + event.getNameColor();
        NametagEdit.getApi().setPrefix(event.getPlayer(), finalName + " ");
        updateHealthBar(event.getPlayer());
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!(event.getVictim() instanceof Player player)) return;
        updateHealthBar(player);
    }

    private void updateHealthBar(Player player) {
        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
            ChatColor healthColor = TabListManager.getHealthChatColor(player);
            NametagEdit.getApi().setSuffix(player, healthColor + " " + (int) player.getHealth() + "❤");
        });
    }


}
