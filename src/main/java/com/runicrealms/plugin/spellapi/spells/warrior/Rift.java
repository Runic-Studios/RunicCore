package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("FieldCanBeLocal")
public class Rift extends Spell {
    private static final int DURATION = 4;
    private static final int RADIUS = 5;

    public Rift() {
        super("Rift", CharacterClass.WARRIOR);
        this.setDescription("You summon a portal of punishing magic, " +
                "drawing in all enemies within " + RADIUS + " blocks " +
                "for " + DURATION + "s!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Location castLocation = player.getLocation();
        while (castLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
            castLocation = castLocation.getBlock().getRelative(BlockFace.DOWN).getLocation();
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5F, 2.0F);
        Location finalCastLocation = castLocation;
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION)
                    this.cancel();
                else {
                    count++;
                    spawnRift(player, finalCastLocation);
                }
            }
        }.runTaskTimer(plugin, 0, 20L);
    }

    private void spawnRift(Player player, Location castLocation) {

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 2.0f);

        // create circle
        new HorizontalCircleFrame(RADIUS, false).playParticle(player, Particle.SPELL_WITCH, castLocation, Color.FUCHSIA);

        // create smaller circles
        new HorizontalCircleFrame((int) (RADIUS * 0.6), false).playParticle(player, Particle.SPELL_WITCH, castLocation, Color.FUCHSIA);
        new HorizontalCircleFrame((int) (RADIUS * 0.2), false).playParticle(player, Particle.SPELL_WITCH, castLocation, Color.FUCHSIA);

        for (Entity entity : player.getWorld().getNearbyEntities(castLocation, RADIUS, RADIUS, RADIUS, target -> isValidEnemy(player, target))) {
            LivingEntity victim = (LivingEntity) entity;
            victim.teleport(castLocation);
        }
    }

}

