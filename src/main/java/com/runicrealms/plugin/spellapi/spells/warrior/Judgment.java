package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.PlayerSpeedStorage;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import com.runicrealms.plugin.RunicCore;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Judgment extends Spell {

    // globals
    private static List<PlayerSpeedStorage> ahFukAStaticVar = new ArrayList<>();
    private static final int BUBBLE_DURATION = 8;
    private static final int BUBBLE_SIZE = 5;
    private static final double UPDATES_PER_SECOND = 10;

    // constructor
    public Judgment() {
        super("Judgment",
                "You summon a barrier of magic" +
                        "\naround yourself for " + BUBBLE_DURATION + " seconds!" +
                        "\nThe barrier repels enemies and prevents" +
                        "\nthem from entering, but allies may pass" +
                        "\nthrough it. During this time, you may not" +
                        "\nmove.",
                ChatColor.WHITE, 9, 25);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // This is necessary because players could (theoretically) cast deliverance multiple times
        // before the first cool down ends
        // which would result in players initial walk speed appearing to be 0 because that is their
        // walk speed when this check is done and if initial walk speed is 0, their walk speed when the
        // spell ends would be set to 0
        boolean isFound = false;
        for (PlayerSpeedStorage psStorage : ahFukAStaticVar) {
            if(!(psStorage.getPlayer().equals(pl)))
                continue;
            isFound = true;
            psStorage.setExpirationTime(System.currentTimeMillis() + (BUBBLE_DURATION * 1000));
            break;
        }
        if (!isFound) {
            ahFukAStaticVar.add(
                    new PlayerSpeedStorage(
                            pl,
                            pl.getWalkSpeed(),
                            System.currentTimeMillis() + (BUBBLE_DURATION * 1000)
                    )
            );
            pl.setWalkSpeed(0);
        }

        // Set player effects
        pl.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.JUMP,
                        BUBBLE_DURATION*20,
                        -10,
                        false,
                        false
                )
        );

        // Play sound effects
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5F, 1.0F);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
        pl.getLocation().getWorld().spigot().strikeLightningEffect(pl.getLocation(), true);

        // Begin spell event
        final long startTime = System.currentTimeMillis();
        new BukkitRunnable() {
            double phi = 0;
            @Override
            public void run() {

                // create visual bubble
                phi += Math.PI/10;
                Location loc = pl.getLocation();
                for (double theta = 0; theta <= 2*Math.PI; theta += Math.PI/40) {
                    double x = BUBBLE_SIZE*cos(theta)*sin(phi);
                    double y = BUBBLE_SIZE*cos(phi) + 1.5;
                    double z = BUBBLE_SIZE*sin(theta)*sin(phi);
                    loc.add(x,y,z);
                    loc.getWorld().spawnParticle(Particle.SPELL_INSTANT, loc, 1, 0, 0, 0, 0);
                    loc.subtract(x,y,z);
                }

                // Spell duration
                long timePassed = System.currentTimeMillis() - startTime;
                if (timePassed > BUBBLE_DURATION * 1000) {
                    this.cancel();
                    pl.removePotionEffect(PotionEffectType.SLOW);
                    for(PlayerSpeedStorage psStorage : ahFukAStaticVar) {
                        if(!(psStorage.getPlayer().equals(pl)))
                            continue;
                        if(System.currentTimeMillis() + 100 < psStorage.getExpirationTime() /* .1 second lag cushion */)
                            continue;
                        pl.setWalkSpeed(psStorage.getOriginalSpeed());
                        ahFukAStaticVar.remove(psStorage);
                        break;
                    }
                    return;
                }

                // More effect noises
                pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_CAT_HISS, 0.01F, 0.5F);

                // Look for targets nearby
                for (Entity entity : pl.getNearbyEntities(BUBBLE_SIZE, BUBBLE_SIZE, BUBBLE_SIZE)) {

                    // ignore NPCs
                    if (entity.hasMetadata("NPC")) { continue; }

                    // skip the caster
                    if(entity.equals(pl)) { continue; }

                    // skip party members
                    if (RunicCore.getPartyManager().getPlayerParty(pl) != null
                            && RunicCore.getPartyManager().getPlayerParty(pl).hasMember(entity.getUniqueId())) { continue; }

                    // Executes the spell
                    Vector force = (pl.getLocation().toVector().subtract(entity.getLocation().toVector()).multiply(-0.75).setY(0.3));
                    entity.setVelocity(force);
                    entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.01F, 0.5F);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, (int) (20/UPDATES_PER_SECOND));
    }
}

