package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.BasicAttackEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * New bard ultimate spell
 *
 * @author BoBoBalloon
 */
public class GrandSymphony extends Spell implements RadiusSpell, MagicDamageSpell, DurationSpell, Tempo.Influenced {
    private static final int PARTICLES_PER_RING = 15;
    private final Map<UUID, Long> debuffed;
    private double[] ranges;
    private double radius;
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double debuffDuration;
    private double debuffRatio;

    public GrandSymphony() {
        super("Grand Symphony", CharacterClass.CLERIC);
        this.setDescription("You pulse waves of resonating magic every 1s for " + this.duration + "s in a " + this.radius + " block radius,\n" +
                "dealing (" + this.damage + " + &f" + this.damagePerLevel + "x&7 lvl) magicʔ damage and reducing enemy attack speed by " + (this.debuffRatio * 100) + "% and spell damage by " + (this.debuffRatio * 50) + "% for " + this.debuffDuration + "s.\n" +
                "Against mobs, this reduces their damage by " + (this.debuffRatio * 100) + "% instead.\n" +
                "If this spell pulses 6 times, the pulse also stuns enemies hit for " + this.debuffDuration + "s.");
        this.debuffed = new HashMap<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        this.removeExtraDuration(player);

        Particle.DustOptions option = new Particle.DustOptions(Color.YELLOW, 2);

        AtomicInteger count = new AtomicInteger(1);

        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), task -> {
            if (count.get() > this.getDuration(player) || count.get() > this.getMaxExtraDuration()) {
                task.cancel();
                return;
            }

            this.particleWave(player, option);

            long now = System.currentTimeMillis();

            for (Entity entity : player.getNearbyEntities(this.radius, this.radius, this.radius)) {
                if (!(entity instanceof LivingEntity target) || !this.isValidEnemy(player, target)) {
                    continue;
                }

                DamageUtil.damageEntitySpell(this.damage, target, player, false, this);
                this.debuffed.put(target.getUniqueId(), now);

                if (count.get() >= this.getMaxExtraDuration()) {
                    this.addStatusEffect(target, RunicStatusEffect.STUN, this.debuffDuration, true);
                }
            }

            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, SoundCategory.AMBIENT, 0.5F, 1F);
            count.set(count.get() + 1);
        }, 0, 20);
    }

    /**
     * When a player attacks but their cooldown is debuffed
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onBasicAttack(BasicAttackEvent event) {
        Long lastUsed = this.debuffed.get(event.getPlayer().getUniqueId());

        if (lastUsed == null || System.currentTimeMillis() > lastUsed + (this.debuffDuration * 1000)) {
            return;
        }

        event.setCooldownTicks(event.getUnroundedCooldownTicks() * (1 + this.debuffRatio));
    }

    /**
     * When a player deals damage with a spell but is debuffed
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        Long lastUsed = this.debuffed.get(event.getPlayer().getUniqueId());

        if (lastUsed == null || System.currentTimeMillis() > lastUsed + (this.debuffDuration * 1000)) {
            return;
        }

        event.setAmount((int) (event.getAmount() * (1 - (this.debuffRatio / 2))));
    }

    /**
     * When a mob deals damage with but is debuffed
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onMobDamage(MobDamageEvent event) {
        Long lastUsed = this.debuffed.get(event.getMob().getUniqueId());

        if (lastUsed == null || System.currentTimeMillis() > lastUsed + (this.debuffDuration * 1000)) {
            return;
        }

        event.setAmount((int) (event.getAmount() * (1 - this.debuffRatio)));
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.debuffed.remove(event.getPlayer().getUniqueId());
    }

    private void particleWave(@NotNull Player player, @NotNull Particle.DustOptions option) {
        AtomicInteger index = new AtomicInteger(0);

        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), particleTask -> {
            if (index.get() >= this.ranges.length) {
                particleTask.cancel();
                return;
            }

            double radius = this.ranges[index.get()];

            this.drawParticleRing(player, radius, option);

            index.set(index.get() + 1);
        }, 0, 10);
    }

    private void drawParticleRing(@NotNull Player player, double radius, @NotNull Particle.DustOptions option) {
        for (int i = 0; i < PARTICLES_PER_RING; i++) {
            double angle = (2 * Math.PI / PARTICLES_PER_RING) * i;
            double x = player.getLocation().getX() + (radius * Math.cos(angle));
            double z = player.getLocation().getZ() + (radius * Math.sin(angle));

            player.spawnParticle(Particle.REDSTONE, x, player.getLocation().getY(), z, 1, option);
        }
    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number debuffDuration = (Number) spellData.getOrDefault("debuff-duration", 2);
        this.debuffDuration = debuffDuration.doubleValue();
        Number debuffRatio = (Number) spellData.getOrDefault("debuff-ratio", 0.5);
        this.debuffRatio = debuffRatio.doubleValue();
    }

    @Override
    public double getRadius() {
        return this.radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
        this.ranges = IntStream.range(1, (int) radius * 2 + 1).mapToDouble(integer -> (double) integer / 2).toArray();
    }

    @Override
    public double getMagicDamage() {
        return this.damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return this.damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }

    @Override
    public double getDuration() {
        return this.duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public double getMaxExtraDuration() {
        return 6;
    }
}
