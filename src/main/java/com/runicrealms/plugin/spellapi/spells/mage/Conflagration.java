package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Conflagration extends Spell implements DurationSpell {
    private final Map<UUID, ConflagrationContainer> conflagrationMap = new HashMap<>();
    private int period;
    private double percent;
    private int damageCapPerTick;
    private double duration;

    public Conflagration() {
        super("Conflagration", CharacterClass.MAGE);
        this.setIsPassive(true);
        this.setDescription("Your spells roar with magical fire! " +
                "Enemies hit by your spells take " + (int) (percent * 100) + "% of their " +
                "max health as magic damage over " + duration + "s! " +
                "Capped at " + damageCapPerTick + " total damage per tick against monsters.");
        startConflagrationTask();
    }

    private void conflagration(Player player, LivingEntity victim) {
        boolean isCapped = !(victim instanceof Player);
        player.getWorld().spawnParticle(Particle.REDSTONE, victim.getEyeLocation(), 15, 0.25f, 0, 0.25f, new Particle.DustOptions(Color.ORANGE, 1));
        int damage = (int) ((percent * victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) / period);
        if (isCapped && damage > damageCapPerTick)
            damage = damageCapPerTick;
        DamageUtil.damageEntitySpell(damage, victim, player);
    }

    public int getDamageCapPerTick() {
        return damageCapPerTick;
    }

    public void setDamageCapPerTick(int damageCapPerTick) {
        this.damageCapPerTick = damageCapPerTick;
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
        Number period = (Number) spellData.getOrDefault("period", 0);
        Number damageCapPerTick = (Number) spellData.getOrDefault("damage-cap-per-tick", 0);
        setDuration(duration.doubleValue());
        setPercent(percent.doubleValue() / 100);
        setPeriod(period.intValue());
        setDamageCapPerTick((int) damageCapPerTick.doubleValue());
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    @EventHandler
    public void onMagicDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (event.getSpell() == null) return;
        Player caster = event.getPlayer();
        LivingEntity victim = event.getVictim();
        conflagrationMap.computeIfAbsent
                (
                        victim.getUniqueId(),
                        k -> conflagrationMap.put(victim.getUniqueId(), new ConflagrationContainer(caster, victim, (int) duration))
                );
        // Refresh uptime
        conflagrationMap.get(event.getVictim().getUniqueId()).setDurationRemaining((int) duration);
    }

    private void startConflagrationTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : conflagrationMap.keySet()) {
                    ConflagrationContainer container = conflagrationMap.get(uuid);
                    container.setDurationRemaining(container.getDurationRemaining() - period);
                    conflagration(container.getCaster(), container.getVictim());
                    if (container.getDurationRemaining() <= 0) {
                        conflagrationMap.remove(uuid);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, period * 20L);
    }

    static class ConflagrationContainer {
        private final Player caster;
        private final LivingEntity victim;
        private Integer durationRemaining;

        public ConflagrationContainer(Player caster, LivingEntity victim, Integer durationRemaining) {
            this.caster = caster;
            this.victim = victim;
            this.durationRemaining = durationRemaining;
        }

        public Player getCaster() {
            return caster;
        }

        public Integer getDurationRemaining() {
            return durationRemaining;
        }

        public void setDurationRemaining(Integer durationRemaining) {
            this.durationRemaining = durationRemaining;
        }

        public LivingEntity getVictim() {
            return victim;
        }
    }

}

