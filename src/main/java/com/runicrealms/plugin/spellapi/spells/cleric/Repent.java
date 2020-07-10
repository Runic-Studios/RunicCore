package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.Bubble;
import com.runicrealms.plugin.utilities.DirectionEnum;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class Repent extends Spell {

    private static final int MAX_DIST = 10;
    private static final float RADIUS = 5f;

    public Repent() {
        super("Repent",
                "You mark a location on the ground" +
                        "\nwith holy magic! After a short delay," +
                        "\nenemies within " + (int) RADIUS + " blocks are thrown" +
                        "\naway from the mark!",
                ChatColor.WHITE, ClassEnum.CLERIC, 1, 20);// todo 15s
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
        pl.getWorld().playSound(ground, Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 0.5f, 1.0f);
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
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () ->
                Bubble.bubbleEffect(ground, Particle.SPELL_INSTANT, 4, 0, 20, RADIUS), 30L);


//        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
//        pl.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, pl.getEyeLocation(), 15, 0.75F, 0.5F, 0.75F, 0);
//
//        for (Entity en : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
//
//            // skip non-living, armor stands
//            if (!(en instanceof LivingEntity)) continue;
//            LivingEntity le = (LivingEntity) en;
//
//            // heal party members and the caster
//            int damage = DAMAGE_AMT;
//            if (MythicMobs.inst().getMobManager().getActiveMob(en.getUniqueId()).isPresent()) {
//                ActiveMob am = MythicMobs.inst().getMobManager().getActiveMob(en.getUniqueId()).get();
//                if (am.getFaction() != null && am.getFaction().equalsIgnoreCase("undead")) {
//                    damage = 2*DAMAGE_AMT;
//                }
//            }
//            if (verifyEnemy(pl, le)) {
//                DamageUtil.damageEntitySpell(damage, le, pl, 100);
//            }
//        }
    }
}
