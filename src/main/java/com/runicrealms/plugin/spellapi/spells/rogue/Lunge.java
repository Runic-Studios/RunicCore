package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.EnvironmentDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Lunge extends Spell implements DurationSpell {
    private static final Set<UUID> LUNGE_SET = new HashSet<>();
    private double duration;
    private double launchMultiplier;
    private double verticalPower;

    public Lunge() {
        super("Lunge", CharacterClass.ROGUE);
        this.setDescription("You lunge forward into the air!");
    }

    /**
     * Performs the lunge effect, and caps the Y component of the vector to prevent odd behavior
     *
     * @param player who cast the spell
     */
    public static void lunge(Player player, double duration, double launchMultiplier, double verticalPower) {
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
        LUNGE_SET.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                () -> LUNGE_SET.remove(player.getUniqueId()), (int) duration * 20L);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        lunge(player, duration, launchMultiplier, verticalPower);
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

    public double getLaunchMultiplier() {
        return launchMultiplier;
    }

    public void setLaunchMultiplier(double launchMultiplier) {
        this.launchMultiplier = launchMultiplier;
    }

    public double getVerticalPower() {
        return verticalPower;
    }

    public void setVerticalPower(double verticalPower) {
        this.verticalPower = verticalPower;
    }

    /**
     * Disable fall damage for players who are lunging
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onFallDamage(EnvironmentDamageEvent event) {
        if (!LUNGE_SET.contains(event.getVictim().getUniqueId())) return;
        if (event.getCause() == EnvironmentDamageEvent.DamageCauses.FALL_DAMAGE)
            event.setCancelled(true);
    }

}
