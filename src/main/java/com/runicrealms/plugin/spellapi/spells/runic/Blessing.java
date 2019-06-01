package com.runicrealms.plugin.spellapi.spells.runic;

import com.runicrealms.plugin.parties.Party;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizCircleFrame;
import com.runicrealms.plugin.utilities.HologramUtil;
import io.lumine.xikage.mythicmobs.skills.mechanics.ParticleEffect;
import org.bukkit.*;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Blessing extends Spell {

    private static final int RADIUS = 1;
    private static final int RING_DURATION = 7;
    private static final int SHIELD_DURATION = 10;
    private static final int SHIELD_AMT = 10;

    public Blessing() {
        super("Blessing",
                "You spawn a ring of holy magic" +
                        "\non the ground which persists for" +
                        "\n" + RING_DURATION + " seconds. The first ally who steps" +
                        "\nover the ring gains a holy" +
                        "\nshield for " + SHIELD_DURATION + " seconds, blocking" +
                        "\nup to " + SHIELD_AMT + " damage!", ChatColor.WHITE,15, 15);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        Location loc = pl.getLocation();
        pl.getWorld().playSound(loc, Sound.ENTITY_CAT_HISS, 0.25f, 0.1f);

        new BukkitRunnable() {
            double count = 1;
            boolean hasBeenUsed = false;
            @Override
            public void run() {
                if (count > RING_DURATION) {
                    this.cancel();

                } else {

                    count += 1;
                    createCircle(pl, loc, RADIUS);
                    for (Entity en : loc.getWorld().getNearbyEntities(loc, RADIUS, RADIUS, RADIUS)) {

                        if (hasBeenUsed) return;
                        if (!(en instanceof Player)) continue;

                        Player ally = (Player) en;

                        // skip the caster
                        if (ally.equals(pl)) {
                            continue;
                        }

                        // heal nobody if we don't have a party
                        if (RunicCore.getPartyManager().getPlayerParty(pl) == null) return;

                        // skip non-party members
                        if (RunicCore.getPartyManager().getPlayerParty(pl) != null
                                && !RunicCore.getPartyManager().getPlayerParty(pl).hasMember(ally.getUniqueId())) {
                            continue;
                        }

                        // apply skill effect
                        this.cancel();
                        hasBeenUsed = true;
                        HealUtil.shieldPlayer(SHIELD_AMT, ally, pl);

                        // create bubble effect
                        Location loc = ally.getEyeLocation();
                        for (int i = 0; i < 100; i++) {
                            Vector vector = getRandomVector().multiply(1.5);
                            loc.add(vector);
                            ally.getWorld().spawnParticle(Particle.REDSTONE, loc,
                                    1, 0, 0, 0, 0, new Particle.DustOptions(Color.WHITE, 1));
                            ally.getWorld().spawnParticle(Particle.SPELL_INSTANT, loc, 1, 0, 0, 0, 0);
                            loc.subtract(vector);
                        }

                        new BukkitRunnable() {
                            @Override
                            public void run() {

                                if (!HealUtil.getShieldedPlayers().containsKey(ally.getUniqueId())) {
                                    this.cancel();

                                } else {

                                    int totalShield = HealUtil.getShieldedPlayers().get(ally.getUniqueId());

                                    if (totalShield - SHIELD_AMT > 0) {
                                        HealUtil.getShieldedPlayers().put(ally.getUniqueId(), totalShield - SHIELD_AMT);
                                    } else {
                                        HealUtil.getShieldedPlayers().remove(ally.getUniqueId());
                                        ally.sendMessage(ChatColor.RED + "You have lost your shield!");
                                    }
                                }
                            }
                        }.runTaskLaterAsynchronously(RunicCore.getInstance(), SHIELD_DURATION*20L);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }

    private Vector getRandomVector() {
        final Random random = new Random(System.nanoTime());
        double x, y, z;
        x = random.nextDouble() * 2 - 1;
        y = random.nextDouble() * 2 - 1;
        z = random.nextDouble() * 2 - 1;

        return new Vector(x, y, z).normalize();
    }

    private void createCircle(Player pl, Location loc, float radius) {

        int particles = 50;
        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * radius;
            z = Math.sin(angle) * radius;
            loc.add(x, 0, z);
            pl.getWorld().spawnParticle(Particle.REDSTONE, loc,
                    1, 0, 0, 0, 0, new Particle.DustOptions(Color.WHITE, 1));
            pl.getWorld().spawnParticle(Particle.SPELL_INSTANT, loc, 1, 0, 0, 0, 0);
            loc.subtract(x, 0, z);
        }
    }
}

