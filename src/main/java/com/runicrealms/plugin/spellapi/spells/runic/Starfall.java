package com.runicrealms.plugin.spellapi.spells.runic;

import com.runicrealms.plugin.outlaw.OutlawManager;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.plugin.utilities.DirectionUtil;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;

import java.util.*;

public class Starfall extends Spell {

    // globals
    private static final int DAMAGE_AMOUNT = 15;
    private static final int DURATION = 1;
    private static final int RADIUS = 2;
    private static final double STAR_SPEED = 0.6;
    private static final int MAX_DIST = 10;
    private HashMap<UUID, List<UUID>> hasBeenHit;

    // constructor
    public Starfall() {
        super("Starfall",
                "You conjure astral magic at your" +
                        "\ntarget location, causing three stars" +
                        "\nto fall from the sky, each dealing" +
                        "\n" + DAMAGE_AMOUNT + " spellʔ damage to enemies within" +
                        "\n" + RADIUS + " blocks and blinding them for " + DURATION +
                        "\nsecond(s).",
                ChatColor.WHITE, 15, 12);
        this.hasBeenHit = new HashMap<>();
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5f, 0.2f);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_TNT_PRIMED, 0.5f, 2.0f);

        Location targetLoc = pl.getTargetBlock(null, MAX_DIST).getLocation().add(0.5, 0.5, 0.5);
        Vector launchPath = new Vector(0, -1.0, 0).normalize().multiply(STAR_SPEED);

        Location middle = new Location(pl.getWorld(), targetLoc.getX(), pl.getLocation().getY(), targetLoc.getZ()).add(0, 10, 0);

        Location left;
        Location right;
        if (DirectionUtil.getDirection(pl).equals("N") || DirectionUtil.getDirection(pl).equals("S")) {
            left = new Location(pl.getWorld(), targetLoc.getX(), pl.getLocation().getY(), targetLoc.getZ()).add(1, 10, 1);
            right = new Location(pl.getWorld(), targetLoc.getX(), pl.getLocation().getY(), targetLoc.getZ()).add(-1, 10, 1);
        } else {
            left = new Location(pl.getWorld(), targetLoc.getX(), pl.getLocation().getY(), targetLoc.getZ()).add(-1, 10, 1);
            right = new Location(pl.getWorld(), targetLoc.getX(), pl.getLocation().getY(), targetLoc.getZ()).add(-1, 10, -1);
        }

        Location[] locations = new Location[]{middle, left, right};

        for (Location loc : locations) {
            new BukkitRunnable() {
                @Override
                public void run() {

                    loc.add(launchPath);

                    if (loc.getBlock().getType().isSolid()) {
                        this.cancel();
                        entityCheck(loc, pl);
                    }

                    pl.getWorld().spawnParticle(Particle.REDSTONE, loc, 10, 0, 0, 0, 0, new Particle.DustOptions(Color.YELLOW, 1));
                    pl.getWorld().spawnParticle(Particle.SPELL_INSTANT, loc, 10, 0, 0, 0, 0);
                }
            }.runTaskTimer(RunicCore.getInstance(), 0L, 1L);
        }

    }

    // prevents players from being hit twice by a single beam
    private void entityCheck(Location location, Player player) {

        for (Entity e : Objects.requireNonNull(location.getWorld()).getNearbyEntities(location, RADIUS, RADIUS, RADIUS)) {

            // skip our player
            if (e == (player)) {
                continue;
            }

            if (!e.getType().isAlive()) continue;
            LivingEntity victim = (LivingEntity) e;

            // ignore NPCs
            if (victim.hasMetadata("NPC")) {
                continue;
            }

            // outlaw check
            if (victim instanceof Player && (!OutlawManager.isOutlaw(((Player) victim)) || !OutlawManager.isOutlaw(player))) {
                continue;
            }

            // skip party members
            if (RunicCore.getPartyManager().getPlayerParty(player) != null
                    && RunicCore.getPartyManager().getPlayerParty(player).hasMember(e.getUniqueId())) {
                continue;
            }

            if (this.hasBeenHit.containsKey(victim.getUniqueId())) {
                List<UUID> uuids = hasBeenHit.get(victim.getUniqueId());
                if (uuids.contains(player.getUniqueId())) {
                    continue;
                } else {
                    uuids.add(player.getUniqueId());
                    hasBeenHit.put(victim.getUniqueId(), uuids);
                }
            } else {
                List<UUID> uuids = new ArrayList<>();
                uuids.add(player.getUniqueId());
                hasBeenHit.put(victim.getUniqueId(), uuids);
            }

            // can't be hit by the same player's beam for 3 secs
            new BukkitRunnable() {
                @Override
                public void run() {
                    List<UUID> uuids = hasBeenHit.get(victim.getUniqueId());
                    uuids.remove(player.getUniqueId());
                    hasBeenHit.put(victim.getUniqueId(), uuids);
                }
            }.runTaskLater(RunicCore.getInstance(), 60L);

            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.5F, 1.0F);
            DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, victim, player);
            if (victim instanceof Player) {
                victim.addPotionEffect
                        (new PotionEffect(PotionEffectType.BLINDNESS, DURATION * 20, 0));
            }
        }
    }
}

