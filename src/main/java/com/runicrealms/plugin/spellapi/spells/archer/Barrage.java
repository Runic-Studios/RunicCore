package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.BasicAttackEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Barrage extends Spell implements DurationSpell {
    private static final Set<UUID> PLAYERS = new HashSet<>();
    private double duration;
    private double percent;

    public Barrage() {
        super("Barrage", CharacterClass.ARCHER);
        this.setDescription("For " + duration + "s, you rapid-fire your arrows, " +
                "granting you " + (percent * 100) + "% attack speed!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 0.5f, 1.0f);
        PLAYERS.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                () -> PLAYERS.remove(player.getUniqueId()), (int) duration * 20L);
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }


    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        Number percent = (Number) spellData.getOrDefault("percent", 0);
        setDuration(duration.doubleValue());
        setPercent(percent.doubleValue() / 100);
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBasicAttack(BasicAttackEvent event) {
        if (!PLAYERS.contains(event.getPlayer().getUniqueId())) return;
        double reducedTicks = event.getOriginalCooldownTicks();
        reducedTicks /= percent;
        int roundedCooldownTicks = (int) (event.getOriginalCooldownTicks() - reducedTicks);
        // Cooldown cannot drop beneath a certain value
        event.setCooldownTicks(Math.max(event.getCooldownTicks() - roundedCooldownTicks, BasicAttackEvent.MINIMUM_COOLDOWN_TICKS));
    }

    /**
     * A method that returns if the provided player is currently using barrage
     *
     * @param player the provided player
     * @return if the provided player is currently using barrage
     */
    public static boolean isUsing(@NotNull Player player) {
        return PLAYERS.contains(player.getUniqueId());
    }
}
