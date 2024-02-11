package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.api.event.BasicAttackEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.effect.warrior.HolyFervorEffect;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.ShieldingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.KnockbackUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.SlashEffect;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.RayTraceResult;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class SacredWings extends Spell implements DurationSpell, RadiusSpell, ShieldingSpell {
    private static final double BEAM_WIDTH = 2;
    private static final double DISTANCE = 2;
    private final Map<UUID, Map<UUID, Long>> sweepCooldownMap = new HashMap<>();
    private final Random random = new Random();
    private double allyShield;
    private double allyShieldPerLevel;
    private double duration;
    private double knockback;
    private double radius;
    private double shield;
    private double shieldPerLevel;
    private double sweepCooldown;

    public SacredWings() {
        super("Sacred Wings", CharacterClass.WARRIOR);
        this.setDescription("For the next " + duration + "s, you conjure wings of light, " +
                "empowering you with &6holy fervor&7, boosting the speed of you and your " +
                "allies within " + radius + " blocks " +
                "and granting you a (" + shield + " + &f" + shieldPerLevel + "x&7 lvl) health shield!" +
                "\n\n&2&lEFFECT &6Holy Fervor" +
                "\n&7While &6holy fervor &7lasts, your basic attacks " +
                "transform into radiant sweeps of light, launching enemies back and " +
                "&eshielding &7other allies within " + radius + " blocks for " +
                "(" + allyShield + " + &f" + allyShieldPerLevel + "x&7 lvl) health! " +
                "Cannot sweep the same target more than once every " + sweepCooldown + "s.");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        // give spell effect
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5F, 0.5F);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5F, 1.0F);
        player.getWorld().spigot().strikeLightningEffect(player.getLocation(), true);
        this.addStatusEffect(player, RunicStatusEffect.SPEED_I, this.duration, false);
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius, target -> isValidAlly(player, target))) {
            this.addStatusEffect((LivingEntity) entity, RunicStatusEffect.SPEED_I, this.duration, false);
        }
        this.shieldPlayer(player, player, this.shield, this);
        HolyFervorEffect holyFervorEffect = new HolyFervorEffect(player, this.duration);
        holyFervorEffect.initialize();
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isRanged()) return;
        if (!(event.getSpell() instanceof SacredWings)) return;
        if (!this.hasSpellEffect(event.getPlayer().getUniqueId(), SpellEffectType.HOLY_FERVOR)) return;
        UUID uuid = event.getPlayer().getUniqueId();
        UUID victimId = event.getVictim().getUniqueId();
        if (sweepCooldownMap.containsKey(uuid)) {
            Map<UUID, Long> entitiesOnCooldown = sweepCooldownMap.get(uuid);
            if (entitiesOnCooldown.containsKey(victimId)) {
                long lastHitTime = entitiesOnCooldown.get(victimId);
                // Ignore targets on cooldown
                if (System.currentTimeMillis() - lastHitTime < (sweepCooldown * 1000)) return;
            }
        }
        sweepCooldownMap.computeIfAbsent(uuid, k -> new HashMap<>());
        sweepCooldownMap.get(uuid).put(victimId, System.currentTimeMillis());
        this.sweepTarget(event.getPlayer(), event.getVictim());
    }

    @EventHandler
    public void onBasicAttack(BasicAttackEvent event) {
        if (!this.hasSpellEffect(event.getPlayer().getUniqueId(), SpellEffectType.HOLY_FERVOR)) return;
        event.setCancelled(true);
        sweepEffect(event.getPlayer(), event.getMaterial(), event.getDamage(), event.getMaxDamage(), event.getRoundedCooldownTicks());
    }

    private void sweepEffect(Player player, Material material, int minDamage, int maxDamage, int cooldownTicks) {
        // Apply attack effects, random damage amount
        int randomNum = ThreadLocalRandom.current().nextInt(minDamage, maxDamage + 1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.2f);
        double distance = DISTANCE;
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        distance,
                        BEAM_WIDTH,
                        entity -> isValidEnemy(player, entity)
                );
        if (rayTraceResult == null) {
            Location location = player.getTargetBlock(null, (int) distance).getLocation();
            location.setDirection(player.getLocation().getDirection());
            location.setY(player.getLocation().add(0, 1, 0).getY());
            SlashEffect.slashHorizontal(
                    player.getLocation(),
                    random.nextBoolean(),
                    random.nextBoolean(),
                    Particle.VILLAGER_ANGRY,
                    0.2f
            );
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            SlashEffect.slashHorizontal(
                    player.getLocation(),
                    random.nextBoolean(),
                    random.nextBoolean(),
                    Particle.VILLAGER_ANGRY,
                    0.2f
            );
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 2.0f);
            Collection<Entity> targets = player.getWorld().getNearbyEntities
                    (livingEntity.getLocation(), BEAM_WIDTH, BEAM_WIDTH, BEAM_WIDTH, target -> isValidEnemy(player, target));
            // Then pass to double-scale off INT
            targets.forEach(target -> DamageUtil.damageEntityPhysical(
                    randomNum,
                    (LivingEntity) target,
                    player,
                    false,
                    false,
                    false,
                    this
            ));
        }

        player.setCooldown(material, cooldownTicks);
    }

    private void sweepTarget(Player player, LivingEntity victim) {
        // Knock enemies away
        victim.getWorld().spawnParticle(Particle.CLOUD, victim.getLocation(), 25, 0.75f, 1.0f, 0.75f, 0);
        KnockbackUtil.knockBackCustom(player, victim, knockback);
        // Shield nearby allies (ignore caster)
        for (Entity entity : victim.getWorld().getNearbyEntities(victim.getLocation(), radius, radius, radius, target -> isValidAlly(player, target))) {
            if (entity.equals(player)) continue;
            this.shieldPlayer(player, (Player) entity, shield, this);
        }
    }

    @Override
    public void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number allyShield = (Number) spellData.getOrDefault("ally-shield", 20);
        setAllyShield(allyShield.doubleValue());
        Number allyShieldPerLevel = (Number) spellData.getOrDefault("ally-shield-per-level", 1.0);
        setAllyShieldPerLevel(allyShieldPerLevel.doubleValue());
        Number knockback = (Number) spellData.getOrDefault("knockback", 1.0);
        setKnockback(knockback.doubleValue());
        Number sweepCooldown = (Number) spellData.getOrDefault("sweep-cooldown", 2);
        setSweepCooldown(sweepCooldown.doubleValue());
    }

    public void setSweepCooldown(double sweepDuration) {
        this.sweepCooldown = sweepDuration;
    }

    public void setAllyShield(double allyShield) {
        this.allyShield = allyShield;
    }

    public void setAllyShieldPerLevel(double allyShieldPerLevel) {
        this.allyShieldPerLevel = allyShieldPerLevel;
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    public void setKnockback(double knockback) {
        this.knockback = knockback;
    }

    @Override
    public double getShield() {
        return shield;
    }

    @Override
    public void setShield(double shield) {
        this.shield = shield;
    }

    @Override
    public double getShieldingPerLevel() {
        return shieldPerLevel;
    }

    @Override
    public void setShieldPerLevel(double shieldingPerLevel) {
        this.shieldPerLevel = shieldingPerLevel;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }
}

