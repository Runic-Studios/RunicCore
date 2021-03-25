package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;

@SuppressWarnings("FieldCanBeLocal")
public class Taunt extends Spell {

    public Taunt() {
        super ("Taunt",
                "Your basic weapon⚔ attacks passively " +
                        "generate threat against monsters, " +
                        "causing them to attack you!",
                ChatColor.WHITE, ClassEnum.WARRIOR, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onDrainingHit(WeaponDamageEvent e) {
        if (!hasPassive(e.getPlayer(), this.getName())) return;
        if (!e.isAutoAttack()) return; // only listen for auto attacks
        generateThreat(e.getPlayer(), e.getEntity());
    }

    private void generateThreat(Player pl, Entity en) {
        if (verifyEnemy(pl, en)) {
            LivingEntity victim = (LivingEntity) en;
            if (!(victim instanceof Monster || victim instanceof Wolf || victim instanceof PolarBear)) return;
            en.getWorld().playSound(en.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.05f, 0.2f);
            victim.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, victim.getEyeLocation(), 1, 0.3F, 0.3F, 0.3F, 0);
            if (victim instanceof Monster) ((Monster) en).setTarget(pl);
            MythicMobs.inst().getAPIHelper().addThreat(en, pl, 1000);
        }
    }
}

