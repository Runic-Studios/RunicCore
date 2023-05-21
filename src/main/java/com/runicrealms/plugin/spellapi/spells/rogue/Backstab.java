package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@SuppressWarnings("FieldCanBeLocal")
public class Backstab extends Spell {
    private static final double PERCENT = .5;

    public Backstab() {
        super("Backstab", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("Damaging an enemy from behind " +
                "deals " + (int) (PERCENT * 100 + 100) + "% damage!");
    }

    private int doBackstab(Player pl, Entity en, int originalAmt) {

        /*
        if the dot-product of both entitys' vectors is greater than 0 (positive),
        then they're facing the same direction and it's a backstab
         */
        if (!(pl.getLocation().getDirection().dot(en.getLocation().getDirection()) >= 0.0D))
            return originalAmt;

        // execute skill effects
        if (isValidEnemy(pl, en)) {
            LivingEntity le = (LivingEntity) en;
            le.getWorld().spawnParticle(Particle.CRIT_MAGIC, le.getEyeLocation(), 25, 0.25, 0.25, 0.25, 0);
            le.getWorld().playSound(le.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 0.5f, 1.0f);
            return (int) (originalAmt + (originalAmt * PERCENT));
        }

        return originalAmt;
    }

    @EventHandler
    public void onDamage(PhysicalDamageEvent e) {
        if (!hasPassive(e.getPlayer().getUniqueId(), this.getName())) return;
        e.setAmount(doBackstab(e.getPlayer(), e.getVictim(), e.getAmount()));
    }

    @EventHandler
    public void onDamage(MagicDamageEvent e) {
        if (!hasPassive(e.getPlayer().getUniqueId(), this.getName())) return;
        e.setAmount(doBackstab(e.getPlayer(), e.getVictim(), e.getAmount()));
    }
}

