package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.GenericDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.SlashEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Lunge extends Spell implements DurationSpell {
    private static final Map<UUID, BukkitTask> LUNGE_TASKS = new HashMap<>();
    private double duration;
    private double launchMultiplier;
    private double verticalPower;
    private double percent;

    public Lunge() {
        super("Lunge", CharacterClass.ROGUE);
        this.setDescription("You lunge forward into the air! " +
                "Your next basic attack within " + duration + "s deals " +
                (percent * 100) + "% damage!");
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
        BukkitTask lungeDamageTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                () -> LUNGE_TASKS.remove(player.getUniqueId()), (int) duration * 20L);
        LUNGE_TASKS.put(player.getUniqueId(), lungeDamageTask);
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
        Number percent = (Number) spellData.getOrDefault("percent", 0);
        setPercent(percent.doubleValue());
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
    public void onFallDamage(GenericDamageEvent event) {
        if (!LUNGE_TASKS.containsKey(event.getVictim().getUniqueId())) return;
        if (event.getCause() == GenericDamageEvent.DamageCauses.FALL_DAMAGE)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST) // fires FIRST
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!LUNGE_TASKS.containsKey(event.getPlayer().getUniqueId())) return;
        event.setAmount((int) (event.getAmount() * percent));
        event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 2.0f);
        SlashEffect.slashHorizontal(event.getPlayer());
        LUNGE_TASKS.remove(event.getPlayer().getUniqueId());
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }
}
