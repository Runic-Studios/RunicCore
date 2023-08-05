package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.RunicBowEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.RangedDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.spellapi.spells.Combat;
import com.runicrealms.plugin.spellapi.spells.Potion;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Stormborn extends Spell implements MagicDamageSpell, RadiusSpell {
    private static final String ARROW_META_KEY = "data";
    private static final String ARROW_META_VALUE = "storm shot";
    private final Map<UUID, Integer> stormPlayers = new HashMap<>();
    private final Map<UUID, Long> hasAlreadyHit = new HashMap<>();
    private double damagePerLevel;
    private double damage;
    private double maxTargets;
    private double radius;

    public Stormborn() {
        super("Stormborn", CharacterClass.ARCHER);
        this.setIsPassive(true);
        this.setDescription("After casting an ability, your next three basic attacks " +
                "are infused with the storm! Each basic attack you fire " +
                "will ricochet off the initial target and strike up to " +
                maxTargets + " additional targets within " + radius + " blocks! " +
                "The empowered arrows deal an additional (" +
                damage + " + &f" + damagePerLevel + "x&7 lvl) magicʔ damage!");
    }

    @Override
    public void loadRadiusData(Map<String, Object> spellData) {
        Number radius = (Number) spellData.getOrDefault("radius", 0);
        setRadius(radius.doubleValue());
        Number maxTargets = (Number) spellData.getOrDefault("max-targets", 0);
        setMaxTargets(maxTargets.doubleValue());
    }

    public void setMaxTargets(double maxTargets) {
        this.maxTargets = maxTargets;
    }

    @Override
    public double getMagicDamage() {
        return damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }

    private Arrow fireArrow(Player player, Vector vector) {
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setVelocity(vector);
        arrow.setShooter(player);
        arrow.setCustomNameVisible(false);
        arrow.setCustomName("autoAttack");
        arrow.setMetadata(ARROW_META_KEY, new FixedMetadataValue(RunicCore.getInstance(), ARROW_META_VALUE));
        arrow.setBounce(false);
        EntityTrail.entityTrail(arrow, Particle.CRIT_MAGIC);
        return arrow;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPhysicalDamage(RangedDamageEvent event) {
        if (!event.isRanged() || !event.isBasicAttack()) {
            return;
        }

        Arrow arrow = event.getArrow();
        if (!arrow.hasMetadata(ARROW_META_KEY) || !arrow.getMetadata(ARROW_META_KEY).get(0).asString().equalsIgnoreCase(ARROW_META_VALUE)) {
            return;
        }

        Long lastHit = this.hasAlreadyHit.get(event.getPlayer().getUniqueId());

        if (lastHit != null && lastHit + 400 > System.currentTimeMillis()) { //less than 8 tick delay (ticks * 50 = milliseconds : 8 x 50 = 400)
            return;
        }

        event.setCancelled(true);

        DamageUtil.damageEntitySpell(damage, event.getVictim(), event.getPlayer(), this);
        this.hasAlreadyHit.put(event.getPlayer().getUniqueId(), System.currentTimeMillis()); // prevent concussive hits

        ricochetEffect(event.getPlayer(), event.getVictim());
        Bukkit.getPluginManager().callEvent(new ArrowHitEvent(event.getPlayer(), event.getVictim()));
    }

    private void ricochetEffect(Player caster, LivingEntity victim) {
        int enemiesHit = 0;
        for (Entity entity : victim.getWorld().getNearbyEntities(victim.getLocation(), radius, radius, radius, target -> isValidEnemy(caster, target))) {
            if (enemiesHit >= maxTargets) break;
            if (entity.equals(victim)) continue;
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.25f, 1.0f);
            VectorUtil.drawLine(caster, Particle.CRIT_MAGIC, Color.BLUE, victim.getEyeLocation(), ((LivingEntity) entity).getEyeLocation(), 0.5D, 1, 0.25f);
            DamageUtil.damageEntitySpell(damage, (LivingEntity) entity, caster, this);
            enemiesHit++;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRunicBowEvent(RunicBowEvent event) {
        if (!stormPlayers.containsKey(event.getPlayer().getUniqueId())) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.25f, 0.75f);
        Vector vector = player.getEyeLocation().getDirection().normalize().multiply(2);
        fireArrow(player, vector);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.25f, 0.75f);
        stormPlayers.put(player.getUniqueId(), stormPlayers.get(player.getUniqueId()) - 1);
        if (stormPlayers.get(event.getPlayer().getUniqueId()) <= 0)
            stormPlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpellCast(SpellCastEvent event) {
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName()) || event.getSpell() instanceof Potion || event.getSpell() instanceof Combat) {
            return;
        }

        stormPlayers.put(event.getCaster().getUniqueId(), 3);
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.stormPlayers.remove(event.getPlayer().getUniqueId());
        this.hasAlreadyHit.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    public static class ArrowHitEvent extends Event {
        private final Player caster;
        private final LivingEntity victim;

        private static final HandlerList HANDLER_LIST = new HandlerList();

        public ArrowHitEvent(@NotNull Player caster, @NotNull LivingEntity victim) {
            this.caster = caster;
            this.victim = victim;
        }

        @NotNull
        public Player getCaster() {
            return this.caster;
        }

        @NotNull
        public LivingEntity getVictim() {
            return this.victim;
        }

        @NotNull
        @Override
        public HandlerList getHandlers() {
            return HANDLER_LIST;
        }

        @NotNull
        public static HandlerList getHandlerList() {
            return HANDLER_LIST;
        }
    }
}
