package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * pyromancer spell 2
 *
 * @author BoBoBalloon
 */
public class Erupt extends Spell implements MagicDamageSpell, RadiusSpell {
    private static final int MAX_DIST = 10;
    private static final double RAY_SIZE = 1.5D;
    private static final int IGNITE_DURATION = 2000; //milliseconds
    private final Map<UUID, Pair<UUID, Long>> ignited;
    private double knockupMultiplier;
    private double damage;
    private double damagePerLevel;
    private double radius;

    public Erupt() {
        super("Erupt", CharacterClass.MAGE);
        this.setDescription("You erupt a powerful blast of fire at " +
                "your target enemy or location that deals " +
                "(" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage to enemies within " + radius + " blocks and " +
                "knocks them up!\n" +
                "Enemies you hit are marked with fire for the next " + (IGNITE_DURATION / 1000) + "s.\n" +
                "Igniting this mark with another &6Pyromancer&7 spell deals a bonus 5% max HP magicʔ damage instantly.");
        this.ignited = new HashMap<>();
    }

    @Override
    public void loadRadiusData(Map<String, Object> spellData) {
        Number knockupMultiplier = (Number) spellData.getOrDefault("knockup-multiplier", 0);
        setKnockupMultiplier(knockupMultiplier.doubleValue());
        Number radius = (Number) spellData.getOrDefault("radius", 0);
        setRadius(radius.doubleValue());
    }

    public void setKnockupMultiplier(double knockupMultiplier) {
        this.knockupMultiplier = knockupMultiplier;
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        MAX_DIST,
                        RAY_SIZE,
                        entity -> isValidEnemy(player, entity)
                );
        Location location;
        if (rayTraceResult == null) {
            location = player.getTargetBlock(null, MAX_DIST).getLocation();
        } else if (rayTraceResult.getHitEntity() != null) {
            location = rayTraceResult.getHitEntity().getLocation();
        } else if (rayTraceResult.getHitBlock() != null) {
            location = rayTraceResult.getHitBlock().getLocation();
        } else {
            location = player.getTargetBlock(null, MAX_DIST).getLocation();
        }
        fireBlast(player, location);
    }

    /**
     * Erupts a column of flame at the given location and knocks up all enemies in the radius
     *
     * @param player        who cast the spell
     * @param blastLocation to erupt the flame
     */
    private void fireBlast(Player player, Location blastLocation) {
        player.getWorld().playSound(blastLocation, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 0.5f);
        player.getWorld().spawnParticle(Particle.LAVA, blastLocation, 25, 0.3f, 0.3f, 0.3f, 0);

        for (Entity entity : player.getWorld().getNearbyEntities(blastLocation, radius, radius, radius, target -> isValidEnemy(player, target))) {
            LivingEntity livingEntity = (LivingEntity) entity;
            DamageUtil.damageEntitySpell(damage, livingEntity, player, this);
            this.ignited.put(livingEntity.getUniqueId(), Pair.pair(player.getUniqueId(), System.currentTimeMillis()));
            entity.getWorld().spawnParticle(Particle.FLAME, livingEntity.getEyeLocation(), 15, 0.5f, 0.5f, 0.5f, 0);
            entity.setVelocity(new Vector(0, 1, 0).normalize().multiply(knockupMultiplier));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        if (!(event.getSpell() instanceof DragonsBreath || event.getSpell() instanceof Erupt || event.getSpell() instanceof Meteor)) {
            return;
        }

        Pair<UUID, Long> ignite = this.ignited.get(event.getVictim().getUniqueId());

        if (ignite == null || !event.getPlayer().getUniqueId().equals(ignite.first) || ignite.second + IGNITE_DURATION > System.currentTimeMillis()) {
            return;
        }

        this.ignited.remove(event.getVictim().getUniqueId());

        int damage = (int) (event.getVictim().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * 0.05);

        event.setAmount(event.getAmount() + damage);
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.ignited.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onMythicMobDeath(MythicMobDeathEvent event) {
        this.ignited.remove(event.getEntity().getUniqueId());
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

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }
}

