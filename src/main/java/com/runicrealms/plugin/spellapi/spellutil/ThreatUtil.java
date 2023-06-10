package com.runicrealms.plugin.spellapi.spellutil;

import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.entity.Wolf;

public class ThreatUtil {

    public static void generateThreat(Player player, Entity entity) {
        LivingEntity victim = (LivingEntity) entity;
        if (!(victim instanceof Monster || victim instanceof Wolf || victim instanceof PolarBear)) return;
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.05f, 0.2f);
        victim.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, victim.getEyeLocation(), 1, 0.3F, 0.3F, 0.3F, 0);
        if (victim instanceof Monster) ((Monster) entity).setTarget(player);
        MythicMobs.inst().getAPIHelper().addThreat(entity, player, 1000000);
    }
}
