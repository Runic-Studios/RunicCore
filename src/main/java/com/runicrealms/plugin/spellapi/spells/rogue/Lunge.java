package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.GenericDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.SlashEffect;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Lunge extends Spell {

    private static final int DURATION = 4; // seconds
    private static final double LAUNCH_MULTIPLIER = 1.75;
    private static final double PERCENT = 2.0;
    private static final double VERTICAL_POWER = 0.5;
    private static final Map<UUID, BukkitTask> LUNGE_TASKS = new HashMap<>();

    public Lunge() {
        super("Lunge",
                "You lunge forward into the air! " +
                        "Your next basic attack within " + DURATION + "s deals " +
                        (int) (PERCENT * 100) + "% damage!",
                ChatColor.WHITE, CharacterClass.ROGUE, 8, 15);
    }

    /**
     * Performs the lunge effect
     *
     * @param player who cast the spell
     */
    public static void lunge(Player player) {
        // spell variables, vectors
        Location location = player.getLocation();
        Vector look = location.getDirection();
        Vector launchPath = new Vector(look.getX(), VERTICAL_POWER, look.getZ()).normalize();

        // particles, sounds
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1.2f);
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation(),
                25, 0.5f, 0.5f, 0.5f, 0, new Particle.DustOptions(Color.fromRGB(210, 180, 140), 20));

        player.setVelocity(launchPath.multiply(LAUNCH_MULTIPLIER));

        BukkitTask lungeDamageTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                () -> LUNGE_TASKS.remove(player.getUniqueId()), DURATION * 20L);
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
        lunge(player);
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
        event.setAmount((int) (event.getAmount() * PERCENT));
        event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 2.0f);
        SlashEffect.slashHorizontal(event.getPlayer());
        LUNGE_TASKS.remove(event.getPlayer().getUniqueId());
    }
}
