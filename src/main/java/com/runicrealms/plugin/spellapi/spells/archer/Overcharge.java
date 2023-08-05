package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.BasicAttackEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.RangedDamageEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Ultimate passive for Stormshot subclass
 *
 * @author BoBoBalloon
 */
public class Overcharge extends Spell implements DurationSpell {
    private final Map<UUID, Map<UUID, Long>> marked;
    private final Map<UUID, Long> overcharged;
    private double duration;
    private int manaRestore;
    private double percent;
    private double markedDuration;

    public Overcharge() {
        super("Overcharge", CharacterClass.ARCHER);
        this.setIsPassive(true);
        this.setDescription("When you deal damage to an enemy with &aThunder Arrow&7, &aStormborn&7, or &aJolt&7 it now marks them for " + this.markedDuration + "s. " +
                "When a marked enemy is hit you gain " + (this.percent * 100) + "% additional attack speed for " + this.duration + "s and restore " + this.manaRestore + " mana. " +
                "This refreshes on gaining another stack and stacks up to 10 times. ");
        this.marked = new HashMap<>();
        this.overcharged = new HashMap<>();

        //display particles on marked entities (can't be done async due to getEntity method)
        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), () -> {
            for (Map.Entry<UUID, Map<UUID, Long>> entry : this.marked.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());

                if (player == null || !player.isOnline()) {
                    continue;
                }

                Particle.DustOptions options = new Particle.DustOptions(Color.BLUE, 1);

                for (UUID uuid : entry.getValue().keySet()) {
                    Entity entity = Bukkit.getEntity(uuid);

                    if (entity == null || !this.isMarked(entry.getKey(), uuid)) {
                        continue;
                    }

                    player.spawnParticle(Particle.REDSTONE, entity.getLocation(), 25, Math.random() * 2.5, Math.random() * 2, Math.random() * 2.5, options);
                }
            }
        }, 200, 10);
    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number manaRestore = (Number) spellData.getOrDefault("mana-restore", 10);
        this.manaRestore = manaRestore.intValue();
        Number percent = (Number) spellData.getOrDefault("percent", 10);
        this.percent = percent.doubleValue();
        Number markedDuration = (Number) spellData.getOrDefault("marked-duration", this.duration);
        this.markedDuration = markedDuration.doubleValue();
    }

    @Override
    public double getDuration() {
        return this.duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    /**
     * A method used to check if a caster has a target marked
     *
     * @param caster the caster
     * @param target their target
     * @return if a caster has a target marked
     */
    public boolean isMarked(@NotNull UUID caster, @NotNull UUID target) {
        Map<UUID, Long> casterMarked = this.marked.get(caster);

        if (casterMarked == null) {
            return false;
        }

        Long lastTimeMarked = casterMarked.get(target);

        if (lastTimeMarked == null) {
            return false;
        }

        return (this.markedDuration * 1000) + lastTimeMarked > System.currentTimeMillis();
    }

    /**
     * A method used to set if a caster has a target marked
     *
     * @param caster the caster
     * @param target the target
     * @param marked if the target is marked
     */
    public void setMarked(@NotNull UUID caster, @NotNull UUID target, boolean marked) {
        if (!this.marked.containsKey(caster)) {
            this.marked.put(caster, new HashMap<>());
        }

        Map<UUID, Long> casterMarked = this.marked.get(caster);

        if (marked) {
            casterMarked.put(target, System.currentTimeMillis());
        } else {
            casterMarked.remove(target);
        }
    }

    /**
     * A method used to check if the caster is in overcharged mode
     *
     * @param caster the caster
     * @return if the caster is in overcharged mode
     */
    public boolean isOvercharged(@NotNull UUID caster) {
        Long timeSinceOvercharged = this.overcharged.get(caster);

        if (timeSinceOvercharged == null) {
            return false;
        }

        return (this.duration * 1000) + timeSinceOvercharged > System.currentTimeMillis();
    }

    /**
     * A method used to set if a caster is overcharged
     *
     * @param caster      the caster
     * @param overcharged if a caster is overcharged
     */
    public void setOvercharged(@NotNull UUID caster, boolean overcharged) {
        if (overcharged) {
            this.overcharged.put(caster, System.currentTimeMillis());
        } else {
            this.overcharged.remove(caster);
        }
    }

    /**
     * When either Thunder Arrow, Stormborn, or Jolt hits a target with this passive enabled, make them marked
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        if (!this.hasPassive(event.getPlayer().getUniqueId(), this.getName()) || !(event.getSpell() instanceof ThunderArrow || event.getSpell() instanceof Stormborn || event.getSpell() instanceof Jolt)) {
            return;
        }

        this.setMarked(event.getPlayer().getUniqueId(), event.getVictim().getUniqueId(), true);
    }

    /**
     * If an enemy hit while marked, the player is now overcharged and the enemy should be unmarked
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onRangedDamage(RangedDamageEvent event) {
        if (!this.isMarked(event.getPlayer().getUniqueId(), event.getVictim().getUniqueId())) {
            return;
        }

        this.setOvercharged(event.getPlayer().getUniqueId(), true);
        RunicCore.getRegenManager().addMana(event.getPlayer(), this.manaRestore); //restore mana on overcharge
        this.setMarked(event.getPlayer().getUniqueId(), event.getVictim().getUniqueId(), false);
    }

    /**
     * When a player is overcharged, they gain a cooldown reduction on their primary fire
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    //priority is high so it runs after Barrage, it should stack with it
    public void onBasicAttack(BasicAttackEvent event) {
        if (!this.hasPassive(event.getPlayer().getUniqueId(), this.getName()) || !this.isOvercharged(event.getPlayer().getUniqueId())) {
            return;
        }

        double reducedTicks = event.getOriginalCooldownTicks() * this.percent; //reduce the cooldown based on the total cooldown time
        int roundedCooldownTicks = (int) (event.getOriginalCooldownTicks() - reducedTicks);

        // Cooldown cannot drop beneath a certain value
        event.setCooldownTicks(Math.max(event.getCooldownTicks() - roundedCooldownTicks, BasicAttackEvent.MINIMUM_COOLDOWN_TICKS)); //apply reduction to current cooldown time
    }

    /**
     * Clean up memory
     */
    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.marked.remove(event.getPlayer().getUniqueId());
        this.overcharged.remove(event.getPlayer().getUniqueId());
    }
}
