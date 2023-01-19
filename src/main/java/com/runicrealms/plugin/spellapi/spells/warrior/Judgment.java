package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Judgment extends Spell {

    private static final int BUBBLE_DURATION = 6;
    private static final int BUBBLE_SIZE = 5;
    private static final double KNOCKBACK = 0.15;
    private static final double PERCENT_REDUCTION = .75;
    private static final double UPDATES_PER_SECOND = 5;
    private final Map<Player, Location> judgmentLocationMap;

    public Judgment() {
        super("Judgment",
                "You summon a barrier of magic " +
                        "around yourself for " + BUBBLE_DURATION + "s! The barrier " +
                        "prevents enemies from entering, but allies may pass through freely! " +
                        "Allies within the barrier gain " + (int) (PERCENT_REDUCTION * 100) + "% damage " +
                        "reduction from all sources! During this time, you are rooted. " +
                        "Sneak to cancel the spell early.",
                ChatColor.WHITE, CharacterClass.WARRIOR, 60, 35);
        judgmentLocationMap = new HashMap<>();
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

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5F, 1.0F);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
        player.getWorld().spigot().strikeLightningEffect(player.getLocation(), true);
        addStatusEffect(player, RunicStatusEffect.ROOT, BUBBLE_DURATION, true);
        judgmentLocationMap.put(player, player.getLocation());

        // Begin spell event
        final long startTime = System.currentTimeMillis();
        new BukkitRunnable() {
            double phi = 0;

            @Override
            public void run() {

                // create visual bubble
                phi += Math.PI / 10;
                Location loc = player.getLocation();
                for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 40) {
                    double x = BUBBLE_SIZE * cos(theta) * sin(phi);
                    double y = BUBBLE_SIZE * cos(phi) + 1.5;
                    double z = BUBBLE_SIZE * sin(theta) * sin(phi);
                    loc.add(x, y, z);
                    player.getWorld().spawnParticle(Particle.SPELL_INSTANT, loc, 1, 0, 0, 0, 0);
                    loc.subtract(x, y, z);
                }

                // Spell duration, allow cancel by sneaking
                long timePassed = System.currentTimeMillis() - startTime;
                if (timePassed > BUBBLE_DURATION * 1000 || player.isSneaking()) {
                    this.cancel();
                    judgmentLocationMap.clear();
                    return;
                }

                // More effect noises
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CAMPFIRE_CRACKLE, 0.5f, 2.0f);

                // Look for targets nearby
                for (Entity entity : player.getNearbyEntities(BUBBLE_SIZE, BUBBLE_SIZE, BUBBLE_SIZE)) {
                    if (isValidEnemy(player, entity)) {
                        Vector force = player.getLocation().toVector().subtract(entity.getLocation().toVector()).multiply(-KNOCKBACK).setY(0.3);
                        entity.setVelocity(force);
                        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.01F, 0.5F);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, (int) (20 / UPDATES_PER_SECOND));
    }

    @EventHandler(priority = EventPriority.HIGH) // goes off after most calculations, so reduction is strong
    public void onMobDamage(MobDamageEvent event) {
        event.setAmount(reduceDamageInBubble(event.getVictim(), event.getAmount()));
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        event.setAmount(reduceDamageInBubble(event.getVictim(), event.getAmount()));
    }

    @EventHandler
    public void onSpellDamage(MagicDamageEvent event) {
        event.setAmount(reduceDamageInBubble(event.getVictim(), event.getAmount()));
    }

    /**
     * @param victim      of the damage event
     * @param eventAmount the amount of damage
     * @return the original amount, or the modified amount if applicable
     */
    private int reduceDamageInBubble(Entity victim, int eventAmount) {
        for (Player player : judgmentLocationMap.keySet()) {
            if (!isValidAlly(player, victim)) continue;
            Location bubbleLocation = judgmentLocationMap.get(player);
            if (bubbleLocation.distanceSquared(victim.getLocation()) > BUBBLE_SIZE * BUBBLE_SIZE)
                continue; // player is outside bubble
            return (int) (eventAmount * (1 - PERCENT_REDUCTION));
        }
        return eventAmount; // no modifier found
    }
}

