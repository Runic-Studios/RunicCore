package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.EnterCombatEvent;
import com.runicrealms.plugin.events.LeaveCombatEvent;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.perk.DynamicItemPerkTextPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.plugin.runicrestart.event.PreShutdownEvent;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GaleblessedPerk extends ItemPerkHandler implements Listener {

    private final float PERCENT_SPEED_PER_STACK;
    private final long SPEED_DURATION_TICKS;
    private final long COOLDOWN_TICKS;
    private final Map<UUID, AttributeModifier> activeGaleblessed = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> deactivationTasks = new ConcurrentHashMap<>(); // Tasks for deactivating galeblessed after 5s
    private final Map<UUID, BukkitTask> cooldownTasks = new HashMap<>();

    public GaleblessedPerk() {
        super("galeblessed");
        SPEED_DURATION_TICKS = (long) (((Number) this.config.get("speed-duration")).floatValue() * 20);
        COOLDOWN_TICKS = (long) (((Number) this.config.get("cooldown")).floatValue() * 20);
        PERCENT_SPEED_PER_STACK = ((Number) this.config.get("speed-per-stack")).floatValue();

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkTextPlaceholder("galeblessed-percent") { // This is used in the configured lore
            @Override
            public String generateReplacement(Player viewer, ItemStack item, NBTItem itemNBT, RunicItemTemplate template) {
                int basePercentage = (int) (PERCENT_SPEED_PER_STACK * 100);

                int percentage;
                if (getEquippedSlot(viewer, item, template) != null) { // Item is equipped
                    percentage = getDisplayedPercentSpeedChange(viewer);
                } else {
                    percentage = itemNBT.getInteger("perks-" + GaleblessedPerk.this.getType().getIdentifier()) * basePercentage;
                }

                if (percentage != basePercentage) {
                    return ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + basePercentage + "%" + ChatColor.YELLOW + " " + percentage + "%";
                } else {
                    return ChatColor.YELLOW.toString() + basePercentage + "%";
                }
            }
        });
    }

    @Override
    public void onChange(Player player, int stacks) { // Called when we change the number of active galeblessed stacks
        tryDeactivateGaleblessed(player, true);
        if (stacks > 0) {
            AttributeModifier modifier = new AttributeModifier(
                    "galeblessed",
                    player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() * stacks * PERCENT_SPEED_PER_STACK,
                    AttributeModifier.Operation.ADD_NUMBER
            );
            activeGaleblessed.put(player.getUniqueId(), modifier);
        } else {
            activeGaleblessed.remove(player.getUniqueId());
        }
    }

    private int getDisplayedPercentSpeedChange(Player player) {
        return (int) (getCurrentStacks(player) * PERCENT_SPEED_PER_STACK * 100);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCombatEnter(EnterCombatEvent event) {
        if (event.isCancelled()) return;
        AttributeModifier modifier = activeGaleblessed.get(event.getPlayer().getUniqueId());
        if (modifier != null && !deactivationTasks.containsKey(event.getPlayer().getUniqueId())) { // we have a modifier and are not already active
            event.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(modifier);
            int percentage = (int) (modifier.getAmount() * 100);
            event.getPlayer().sendMessage(
                    ChatColor.AQUA + "" + ChatColor.ITALIC + "Galeblessed: +"
                            + ChatColor.YELLOW + ChatColor.ITALIC + getDisplayedPercentSpeedChange(event.getPlayer()) + "% "
                            + ChatColor.AQUA + ChatColor.ITALIC + "walk speed");
            deactivationTasks.put(event.getPlayer().getUniqueId(), new BukkitRunnable() {
                @Override
                public void run() {
                    tryDeactivateGaleblessed(event.getPlayer(), false);
                }
            }.runTaskLater(RunicCore.getInstance(), SPEED_DURATION_TICKS));
            cooldownTasks.put(event.getPlayer().getUniqueId(), new BukkitRunnable() {
                @Override
                public void run() {
                    cooldownTasks.remove(event.getPlayer().getUniqueId());
                }
            }.runTaskLater(RunicCore.getInstance(), COOLDOWN_TICKS));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCombatLeave(LeaveCombatEvent event) {
        if (event.isCancelled()) return;
        tryDeactivateGaleblessed(event.getPlayer(), true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        tryDeactivateGaleblessed(event.getPlayer(), true);
        if (cooldownTasks.containsKey(event.getPlayer().getUniqueId())) {
            cooldownTasks.get(event.getPlayer().getUniqueId()).cancel();
            cooldownTasks.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPreShutdown(PreShutdownEvent event) {
        for (BukkitTask task : deactivationTasks.values()) {
            task.cancel();
        }
        for (BukkitTask task : cooldownTasks.values()) {
            task.cancel();
        }
    }

    private void tryDeactivateGaleblessed(Player player, boolean cancelTask) {
        AttributeModifier modifier = activeGaleblessed.get(player.getUniqueId());
        if (modifier != null) {
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(modifier);
        }
        if (cancelTask) {
            BukkitTask task = deactivationTasks.get(player.getUniqueId());
            if (task != null) {
                task.cancel();
                deactivationTasks.remove(player.getUniqueId());
            }
        }
    }

}
