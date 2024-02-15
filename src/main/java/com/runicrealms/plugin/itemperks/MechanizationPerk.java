package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.EnterCombatEvent;
import com.runicrealms.plugin.events.LeaveCombatEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.perk.DynamicItemPerkPercentStatPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.DynamicItemPerkStatPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class that models the mechanization item perk
 *
 * @author BoBoBalloon
 */
public class MechanizationPerk extends ItemPerkHandler {
    private final Map<UUID, BukkitTask> mechanized;
    private final double cooldownReductionPercent;
    private final int manaDrainPerSecond;
    private final long interval;

    public MechanizationPerk() {
        super("mechanization");

        this.mechanized = new HashMap<>();

        this.cooldownReductionPercent = ((Number) this.config.get("cooldown-reduction-percent-per-stack")).doubleValue();
        this.manaDrainPerSecond = ((Number) this.config.get("mana-drain-per-stack")).intValue();
        this.interval = ((Number) this.config.get("interval")).longValue() * 1000; //convert seconds to milliseconds

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("mechanization-cooldown-reduction-percent-per-stack", this, () -> this.cooldownReductionPercent));  //This is used in the configured lore
        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkStatPlaceholder("mechanization-mana-drain-per-stack", this, () -> this.manaDrainPerSecond));

        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), () -> this.mechanized.keySet()
                .stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(player -> RunicCore.getRegenManager().addMana(player, -this.manaDrainPerSecond * this.getCurrentStacks(player))), 0, 20);
    }

    @Override
    public void onChange(Player player, int stacks) {
        if (stacks <= 0 && this.mechanized.containsKey(player.getUniqueId())) {
            this.mechanized.remove(player.getUniqueId()).cancel();
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onEnterCombat(EnterCombatEvent event) {
        if (!this.isActive(event.getPlayer())) {
            return;
        }

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), () -> {
            ConcurrentHashMap.KeySetView<Spell, Long> cooldowns = RunicCore.getSpellAPI().getSpellsOnCooldown(event.getPlayer().getUniqueId());

            if (cooldowns == null) {
                return;
            }

            for (Spell spell : cooldowns) {
                double cooldown = RunicCore.getSpellAPI().getUserCooldown(event.getPlayer(), spell);
                RunicCore.getSpellAPI().reduceCooldown(event.getPlayer(), spell, cooldown * this.cooldownReductionPercent * this.getCurrentStacks(event.getPlayer()));
            }
        }, this.interval, 0);

        this.mechanized.put(event.getPlayer().getUniqueId(), task);
    }

    @EventHandler(ignoreCancelled = true)
    private void onLeaveCombat(LeaveCombatEvent event) {
        BukkitTask task = this.mechanized.remove(event.getPlayer().getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        BukkitTask task = this.mechanized.remove(event.getPlayer().getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
}
