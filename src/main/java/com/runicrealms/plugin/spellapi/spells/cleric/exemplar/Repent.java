package com.runicrealms.plugin.spellapi.spells.cleric.exemplar;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.DirectionEnum;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Repent extends Spell {

    private static final int DURATION = 4;
    private static final int MAX_DIST = 10;
    private static final float RADIUS = 5f;

    public Repent() {
        super("Repent",
                "You mark a location with" +
                        "\nholy magic! After a short delay," +
                        "\nenemies within " + (int) RADIUS + " blocks are" +
                        "\nthrown away from the mark" +
                        "\nand slowed for " + DURATION + " seconds!",
                ChatColor.WHITE, ClassEnum.CLERIC, 15, 20);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.swingMainHand();

        // ensure target block is on ground
        Block lookBlock = pl.getTargetBlock(null, MAX_DIST);
        while (lookBlock.getRelative(BlockFace.DOWN).getType() == Material.AIR) {
            lookBlock = lookBlock.getRelative(BlockFace.DOWN);
        }

        Location ground = lookBlock.getLocation();
        pl.getWorld().playSound(ground, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5f, 2.0f);
        Location air;

        if (DirectionEnum.getDirection(pl) == DirectionEnum.EAST)
            air = ground.clone().add(0, 10, 6);
        else if (DirectionEnum.getDirection(pl) == DirectionEnum.WEST)
            air = ground.clone().add(0, 10, -6);
        else if (DirectionEnum.getDirection(pl) == DirectionEnum.SOUTH)
            air = ground.clone().add(-6, 10, 0);
        else
            air = ground.clone().add(6, 10, 0);

        VectorUtil.drawLine(pl, Particle.SPELL_INSTANT, Color.WHITE, ground, air, 1.0);

        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {

            pl.getWorld().playSound(ground, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
            pl.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, ground.clone().add(0, 1, 0), 25, 0.2, 0.2, 0.2, 0);
            for (Entity en : pl.getWorld().getNearbyEntities(ground, RADIUS, RADIUS, RADIUS)) {
                if (!(en instanceof LivingEntity))
                    continue;
                if (verifyEnemy(pl, en)) {
                    // thrown away
                    Vector force = pl.getLocation().toVector().subtract(en.getLocation().toVector()).multiply(-0.25).setY(0.3);
                    en.setVelocity(force);
                    en.getWorld().playSound(en.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5F, 0.5F);
                    ((LivingEntity) en).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, DURATION * 20, 2));
                }
            }
        } , 20L);
    }
}
