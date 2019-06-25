package com.runicrealms.plugin.spellapi.spells.runic;

import com.runicrealms.plugin.outlaw.OutlawManager;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HelixParticleFrame;
import com.runicrealms.plugin.spellapi.spellutil.particles.HelixReverseParticleFrame;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizCircleFrame;
import com.runicrealms.plugin.spellapi.spellutil.particles.VertCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Petrify extends Spell {

    private static final int BEAM_LENGTH = 8;
    private static final int DURATION = 2;
    private static final int RADIUS = 1;
    private List<LivingEntity> victims = new ArrayList<>();

    public Petrify() {
        super("Petrify",
                "You launch three beams of unholy" +
                        "\nmagic, rooting all enemies hit for" +
                        "\n" + DURATION + " second(s).", ChatColor.WHITE,10, 15);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // sound effects
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 0.5f);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_GHAST_SCREAM, 0.5f, 0.75f);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_SLIME_BLOCK_BREAK, 0.5f, 0.5f);

        // create three beams
        Vector middle = pl.getEyeLocation().getDirection().normalize();
        Vector left = rotateVectorAroundY(middle, -15.0);
        Vector right = rotateVectorAroundY(middle, 15.0);

        // begin particle effect & entity check tasks
        startTask(pl, new Vector[]{middle, left, right});
    }

    // particles, vectors
    private void startTask(Player player, Vector[] vectors) {

        for(Vector vector : vectors) {
            Location location = player.getEyeLocation();
            for (double t = 0; t < BEAM_LENGTH; t += 1) {
                location.add(vector);
                player.getWorld().spawnParticle(Particle.SLIME, location, 2, 0, 0, 0, 0);
                player.getWorld().spawnParticle(Particle.REDSTONE, location, 5, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.YELLOW, 1));
                entityCheck(location, player);
                if (location.getBlock().getType().isSolid()) {
                    break;
                }
            }
        }

        new BukkitRunnable() {
            int count = 1;
            @Override
            public void run() {

                if (count > DURATION) {
                    this.cancel();
                    victims.clear();
                } else {

                    count += 1;
                    for (LivingEntity victim : victims) {

                        victim.setFallDistance(-512f);

                        victim.getWorld().spawnParticle(Particle.SLIME, victim.getLocation(), 2, 0, 0, 0, 0);
                        victim.getWorld().spawnParticle(Particle.REDSTONE, victim.getLocation(),
                                1, 0.25f, 0.25f, 0.25f, new Particle.DustOptions(Color.YELLOW, 1));
                    }
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 20L);
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

            // apply skill effect
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.25f, 2.0f);
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, DURATION*20, 6));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, DURATION*20, 128));
            victims.add(victim);
        }
    }
}

