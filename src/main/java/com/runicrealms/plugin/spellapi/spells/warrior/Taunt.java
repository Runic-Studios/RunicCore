package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;

public class Taunt extends Spell {

    public Taunt() {
        super("Taunt",
                "Your basic attacks passively " +
                        "generate threat against monsters, " +
                        "causing them to attack you!",
                ChatColor.WHITE, CharacterClass.WARRIOR, 0, 0);
        this.setIsPassive(true);
    }

    private void generateThreat(Player player, Entity entity) {
        if (isValidEnemy(player, entity)) {
            LivingEntity victim = (LivingEntity) entity;
            if (!(victim instanceof Monster || victim instanceof Wolf || victim instanceof PolarBear)) return;
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

