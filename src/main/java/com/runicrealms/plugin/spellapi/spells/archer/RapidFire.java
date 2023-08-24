package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.api.event.BasicAttackEvent;
import com.runicrealms.plugin.api.event.RunicBowEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RapidFire extends Spell implements DurationSpell {
    private final Map<UUID, Long> players = new HashMap<>();
    private double duration;
    private double percent;

    public RapidFire() {
        super("Rapid Fire", CharacterClass.ARCHER);
        this.setDescription("For " + duration + "s, you rapid-fire your arrows, " +
                "granting you " + (percent * 100) + "% attack speed!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 0.5f, 1.0f);
        players.put(player.getUniqueId(), System.currentTimeMillis());
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

    @EventHandler(ignoreCancelled = true)
    public void onBasicAttack(BasicAttackEvent event) {
        if (!isUsing(event.getPlayer())) {
            return;
        }

        double reducedTicks = event.getOriginalCooldownTicks();
        reducedTicks /= percent;
        int roundedCooldownTicks = (int) (event.getOriginalCooldownTicks() - reducedTicks);
        // Cooldown cannot drop beneath a certain value
        event.setCooldownTicks(Math.max(event.getCooldownTicks() - roundedCooldownTicks, BasicAttackEvent.MINIMUM_COOLDOWN_TICKS));
    }

    @EventHandler
    private void onRunicBow(RunicBowEvent event) {
        if (!this.isUsing(event.getPlayer())) {
            return;
        }

        event.getPlayer().playSound(event.getPlayer(), Sound.BLOCK_BAMBOO_PLACE, SoundCategory.PLAYERS, 1, 1);
        EntityTrail.entityTrail(event.getArrow(), Particle.CRIT_MAGIC);
    }

    /**
     * A method that returns if the provided player is currently using barrage
     *
     * @param player the provided player
     * @return if the provided player is currently using barrage
     */
    public boolean isUsing(@NotNull Player player) {
        Long start = this.players.get(player.getUniqueId());

        return start != null && start + (this.duration * 1000) > System.currentTimeMillis();
    }
}
