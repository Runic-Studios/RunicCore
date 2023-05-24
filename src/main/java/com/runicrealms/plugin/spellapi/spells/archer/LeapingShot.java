package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.GenericDamageEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LeapingShot extends Spell implements DurationSpell, PhysicalDamageSpell {
    private static final String ARROW_META_KEY = "data";
    private static final String ARROW_META_VALUE = "leaping shot";
    private static final Map<UUID, BukkitTask> LEAP_TASKS = new HashMap<>();
    private final HashMap<UUID, UUID> hasBeenHit = new HashMap<>();
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double launchMultiplier;
    private double verticalPower;

    public LeapingShot() {
        super("Leaping Shot", CharacterClass.ARCHER);
        this.setDescription("You fire a spread of 3 arrows that each " +
                "(" + damage + " + &f" + damagePerLevel +
                "x &7lvl) physical⚔ damage while simultaneously leaping backwards! " +
                "During your leap, you are granted fall damage immunity for " + duration + "s.");
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean attemptToExecute(Player player) {
        if (!player.isOnGround()) {
            player.sendMessage(ChatColor.RED + "You must be on the ground to cast " + this.getName() + "!");
            return false;
        }
        return true;
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        leap(player);
        Vector middle = player.getEyeLocation().getDirection().normalize().multiply(2);
        Vector leftMid = rotateVectorAroundY(middle, -10);
        Vector rightMid = rotateVectorAroundY(middle, 10);
        Vector[] vectors = new Vector[]{middle, leftMid, rightMid};
        for (Vector vector : vectors) {
            fireArrow(player, vector);
        }
    }

    private void fireArrow(Player player, Vector vector) {
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setVelocity(vector);
        arrow.setShooter(player);
        arrow.setCustomNameVisible(false);
        arrow.setMetadata(ARROW_META_KEY, new FixedMetadataValue(RunicCore.getInstance(), ARROW_META_VALUE));
        arrow.setBounce(false);
        EntityTrail.entityTrail(arrow, Particle.CRIT);
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
        setDuration(duration.doubleValue());
        Number launchMultiplier = (Number) spellData.getOrDefault("launch-multiplier", 0);
        setLaunchMultiplier(launchMultiplier.doubleValue());
        Number verticalPower = (Number) spellData.getOrDefault("vertical-power", 0);
        setVerticalPower(verticalPower.doubleValue());
    }

    @Override
    public double getPhysicalDamage() {
        return damage;
    }

    @Override
    public void setPhysicalDamage(double physicalDamage) {
        this.damage = physicalDamage;
    }

    @Override
    public double getPhysicalDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setPhysicalDamagePerLevel(double physicalDamagePerLevel) {
        this.damagePerLevel = physicalDamagePerLevel;
    }

    /**
     * Performs the lunge effect, and caps the Y component of the vector to prevent odd behavior
     *
     * @param player who cast the spell
     */
    public void leap(Player player) {
        // Spell variables, vectors
        Location location = player.getLocation();
        Vector look = location.getDirection();
        Vector launchPath = new Vector(-look.getX(), verticalPower, -look.getZ()).normalize();
        launchPath.multiply(launchMultiplier);
        double cappedY = Math.min(launchPath.getY(), 0.95);
        launchPath.setY(cappedY);
        // Particles, sounds
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1.2f);
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation(),
                25, 0.5f, 0.5f, 0.5f, 0, new Particle.DustOptions(Color.fromRGB(210, 180, 140), 20));
        player.setVelocity(launchPath);
        BukkitTask lungeDamageTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                () -> LEAP_TASKS.remove(player.getUniqueId()), (int) duration * 20L);
        LEAP_TASKS.put(player.getUniqueId(), lungeDamageTask);
    }

    /**
     * Disable fall damage for players who are lunging
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onFallDamage(GenericDamageEvent event) {
        if (!LEAP_TASKS.containsKey(event.getVictim().getUniqueId())) return;
        if (event.getCause() == GenericDamageEvent.DamageCauses.FALL_DAMAGE)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPhysicalDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        if (!arrow.hasMetadata(ARROW_META_KEY)) return;
        if (!arrow.getMetadata(ARROW_META_KEY).get(0).asString().equalsIgnoreCase(ARROW_META_VALUE))
            return;
        if (!(arrow.getShooter() instanceof Player player)) return;
        if (hasBeenHit.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;
        event.setCancelled(true);
        DamageUtil.damageEntityPhysical(damage, livingEntity, player, false, true, this);
        hasBeenHit.put(player.getUniqueId(), livingEntity.getUniqueId()); // prevent concussive hits
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> hasBeenHit.remove(player.getUniqueId()), 8L);
    }

    public void setLaunchMultiplier(double launchMultiplier) {
        this.launchMultiplier = launchMultiplier;
    }

    public void setVerticalPower(double verticalPower) {
        this.verticalPower = verticalPower;
    }

}
