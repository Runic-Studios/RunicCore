package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

public class Cocoon extends Spell implements PhysicalDamageSpell {
    public static final int DURATION = 3;
    private static final int DAMAGE = 20;
    private static final int MAX_DIST = 6;
    private static final double BEAM_WIDTH = 1.0D;
    private static final double DAMAGE_PER_LEVEL = 1.5;

    public Cocoon() {
        super("Cocoon",
                "You launch a short-range string of web " +
                        "that deals (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL + "x&7 lvl) physical⚔ " +
                        "damage and slows the first enemy hit within " + MAX_DIST + " blocks " +
                        "for " + DURATION + "s!",
                ChatColor.WHITE, CharacterClass.ROGUE, 12, 25);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        MAX_DIST,
                        BEAM_WIDTH,
                        entity -> isValidEnemy(player, entity)
                );

        if (rayTraceResult == null) {
            Location location = player.getTargetBlock(null, MAX_DIST).getLocation();
            VectorUtil.drawLine(player, Material.COBWEB, player.getEyeLocation(),
                    location, 0.5D, 5, 0.05f);
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            VectorUtil.drawLine(player, Material.COBWEB, player.getEyeLocation(),
                    livingEntity.getLocation(), 0.5D, 5, 0.05f);
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 0.25f, 2.0f);
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, DURATION * 20,
                    2));
            DamageUtil.damageEntityPhysical(DAMAGE, livingEntity, player, false, false, this);
        }
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

}

