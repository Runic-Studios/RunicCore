package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.EnvironmentDamage;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Leap extends Spell implements DurationSpell, PhysicalDamageSpell, RadiusSpell {
    private static final Set<Player> LUNGE_SET = new HashSet<>();
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double launchMultiplier;
    private double radius;
    private double verticalPower;

    public Leap() {
        super("Leap", CharacterClass.ROGUE);
        this.setDescription("You leap forward into the air! Upon landing, you deal " +
                "(" + damage + " + &f" + damagePerLevel + "x&7 lvl) physical⚔ damage to nearby &7&omonsters&7.");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        // Spell variables, vectors
        Location location = player.getLocation();
        Vector look = location.getDirection();
        Vector launchPath = new Vector(look.getX(), verticalPower, look.getZ()).normalize();
        launchPath.multiply(launchMultiplier);
        double cappedY = Math.min(launchPath.getY(), 0.95);
        launchPath.setY(cappedY);
        // Particles, sounds
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1.2f);
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation(),
                25, 0.5f, 0.5f, 0.5f, 0, new Particle.DustOptions(Color.fromRGB(210, 180, 140), 20));
        player.setVelocity(launchPath); // .multiply(launchMultiplier)
        LUNGE_SET.add(player);
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                () -> LUNGE_SET.remove(player), (int) duration * 20L);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (LUNGE_SET.isEmpty()) return;
        if (!LUNGE_SET.contains(event.getPlayer())) return;
        Player player = event.getPlayer();
        if (!player.isOnGround()) return;
        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
            LUNGE_SET.remove(player);
            for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius, target -> isValidEnemy(player, target))) {
                if (entity instanceof Player) continue;
                // todo: landing particle
                DamageUtil.damageEntityPhysical(damage, (LivingEntity) entity, player, false, false, this);
            }
        });
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

    public void setLaunchMultiplier(double launchMultiplier) {
        this.launchMultiplier = launchMultiplier;
    }

    public void setVerticalPower(double verticalPower) {
        this.verticalPower = verticalPower;
    }

    /**
     * Disable fall damage for players who are lunging
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onFallDamage(EnvironmentDamage event) {
        if (!(event.getVictim() instanceof Player player)) return;
        if (!LUNGE_SET.contains(player)) return;
        if (event.getCause() == EnvironmentDamage.DamageCauses.FALL_DAMAGE)
            event.setCancelled(true);
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

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }
}
