package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;

public class Taunt extends Spell {

    public Taunt() {
        super("Taunt", CharacterClass.WARRIOR);
        this.setIsPassive(true);
        this.setDescription("Your basic attacks passively " +
                "generate threat against monsters, " +
                "causing them to attack you!");
    }

    private void generateThreat(Player player, Entity entity) {
        if (isValidEnemy(player, entity)) {
            LivingEntity victim = (LivingEntity) entity;
            if (!(victim instanceof Monster || victim instanceof Wolf || victim instanceof PolarBear))
                return;
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.05f, 0.2f);
            victim.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, victim.getEyeLocation(), 1, 0.3F, 0.3F, 0.3F, 0);
            if (victim instanceof Monster) ((Monster) entity).setTarget(player);
            MythicMobs.inst().getAPIHelper().addThreat(entity, player, 1000);
        }
    }

    @EventHandler
    public void onDrainingHit(PhysicalDamageEvent event) {
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!event.isBasicAttack()) return; // only listen for basic attacks
        generateThreat(event.getPlayer(), event.getVictim());
    }
}

