package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Purify extends Spell implements HealingSpell {

    private static final int HEAL_AMT = 45;
    private static final double HEALING_PER_LEVEL = 2.5;
    private static final int RANGE = 15;
    private static final int BEAM_SPEED = 3;
    private final int SUCCESSIVE_COOLDOWN = 2; // in seconds
    private final double RADIUS = 1.5;
    private final HashMap<UUID, List<UUID>> hasBeenHit;

    public Purify() {
        super("Purify",
                "You launch a beam of healing magic, " +
                        "restoring✦ (" + HEAL_AMT + " + &f" + HEALING_PER_LEVEL +
                        "x&7 lvl) health to yourself and " +
                        "all allies it passes through and removing silences!",
                ChatColor.WHITE, CharacterClass.CLERIC, 12, 25);
        this.hasBeenHit = new HashMap<>();
    }

    // checks for allies near the beam, stops multiple healing of the same player
    @SuppressWarnings("deprecation")
    private void allyCheck(Player player, Location location) {
        for (Entity e : player.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS)) {
            if (isValidAlly(player, e)) {
                // a bunch of fancy checks to make sure one player can't be spam healed by the same effect
                // multiple times
                Player ally = (Player) e;
                if (hasBeenHit.containsKey(ally.getUniqueId())) {
                    List<UUID> uuids = hasBeenHit.get(ally.getUniqueId());
                    if (uuids.contains(player.getUniqueId())) {
                        break;
                    } else {
                        uuids.add(player.getUniqueId());
                        hasBeenHit.put(ally.getUniqueId(), uuids);
                    }
                } else {
                    List<UUID> uuids = new ArrayList<>();
                    uuids.add(player.getUniqueId());
                    hasBeenHit.put(ally.getUniqueId(), uuids);
                }

                // can't be hit by the same player's beam for SUCCESSIVE_COOLDOWN secs
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        List<UUID> uuids = hasBeenHit.get(ally.getUniqueId());
                        uuids.remove(player.getUniqueId());
                        hasBeenHit.put(ally.getUniqueId(), uuids);
                    }
                }.runTaskLater(RunicCore.getInstance(), (SUCCESSIVE_COOLDOWN * 20));

                if (ally.getHealth() == ally.getMaxHealth()) {
                    ally.sendMessage(
                            ChatColor.WHITE + player.getName()
                                    + ChatColor.GRAY + " tried to heal you, but you are at full health.");
                    ally.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);

                } else {
                    HealUtil.healPlayer(HEAL_AMT, ally, player, false, this);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
                    removeStatusEffect(ally, RunicStatusEffect.SILENCE);
                    // stop the beam if it hits a player
                    break;
                }
            }
        }
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.swingMainHand();

        // heal the caster
        HealUtil.healPlayer(HEAL_AMT, pl, pl, false, this);

        // sound effect
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);

        // particle effect, spell effects
        Vector middle = pl.getEyeLocation().getDirection().normalize().multiply(BEAM_SPEED);
        startTask(pl, new Vector[]{middle});
    }

    @Override
    public int getHeal() {
        return HEAL_AMT;
    }

    @Override
    public double getHealingPerLevel() {
        return HEALING_PER_LEVEL;
    }

    // particle effect
    private void startTask(Player player, Vector[] vectors) {
        for (Vector vector : vectors) {
            new BukkitRunnable() {
                final Location location = player.getEyeLocation();
                final Location startLoc = player.getLocation();

                @Override
                public void run() {
                    location.add(vector);
                    // 10 block range before spell dies out naturally
                    if (location.getBlock().getType().isSolid() || location.distanceSquared(startLoc) >= RANGE * RANGE) {
                        this.cancel();
                        player.getWorld().spawnParticle(Particle.SPELL_INSTANT, location, 15, 0.5f, 0.5f, 0.5f, 0);
                    }
                    player.getWorld().spawnParticle(Particle.SPELL_INSTANT, location, 10, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.REDSTONE, location, 10, 0.1f, 0.1f, 0.1f, 0, new Particle.DustOptions(Color.YELLOW, 1));
                    allyCheck(player, location);
                }
            }.runTaskTimer(RunicCore.getInstance(), 0L, 1L);
        }
    }
}
