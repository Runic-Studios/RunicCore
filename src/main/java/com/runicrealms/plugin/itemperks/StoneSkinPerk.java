package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.EnvironmentDamageEvent;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.RunicDamageEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.perk.DynamicItemPerkPercentStatPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class that models the ravenous item perk
 *
 * @author BoBoBalloon
 */
public class StoneSkinPerk extends ItemPerkHandler implements Listener {
    private final Map<UUID, Integer> stacks;
    private final double damagePercentReduction;

    public StoneSkinPerk() {
        super("stoneskin");

        this.stacks = new ConcurrentHashMap<>(); //onChange method is async and iterated constantly so make this thread safe

        this.damagePercentReduction = ((Number) this.config.get("damage-percent-reduction")).doubleValue();

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("stoneskin-damage-percent-reduction", this, () -> this.damagePercentReduction));  //This is used in the configured lore

        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), () -> this.stacks.keySet()
                .stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(player -> RunicCore.getStatusEffectAPI().addStatusEffect(player, RunicStatusEffect.SLOW_II, 0.5, false)), 10, 0);
    }

    @Override
    public void onChange(Player player, int stacks) {
        if (stacks > 0) {
            this.stacks.put(player.getUniqueId(), stacks);
        } else {
            this.stacks.remove(player.getUniqueId());
        }
    }

    private void onDamage(@NotNull RunicDamageEvent event) {
        if (!(event.getVictim() instanceof Player player)) {
            return;
        }

        Integer stacks = this.stacks.get(player.getUniqueId());
        if (stacks == null) {
            return;
        }

        double damage = event.getAmount() * (1 - this.damagePercentReduction * stacks);
        event.setAmount((int) damage);
    }

    @EventHandler(ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        this.onDamage(event);
    }

    @EventHandler(ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        this.onDamage(event);
    }

    @EventHandler(ignoreCancelled = true)
    private void onEnvironmentDamage(EnvironmentDamageEvent event) {
        this.onDamage(event);
    }

    @EventHandler(ignoreCancelled = true)
    private void onMobDamage(MobDamageEvent event) {
        this.onDamage(event);
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.stacks.remove(event.getPlayer().getUniqueId());
    }
}
