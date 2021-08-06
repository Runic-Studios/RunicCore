package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@SuppressWarnings("FieldCanBeLocal")
public class Hawkeye extends Spell { // implements MagicDamageSpell, WeaponDamageSpell

    private static final int DAMAGE = 4;
    private static final int DISTANCE = 10;
    private static final double DAMAGE_PER_LEVEL = 0.5;

    public Hawkeye() {
        super("Hawkeye",
                "Damaging an enemy from " + DISTANCE + " blocks " +
                        "away or farther deals (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL +
                        "x&7 lvl) additional damage!",
                ChatColor.WHITE, ClassEnum.ARCHER, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRangedHit(SpellDamageEvent e) {
        if (!hasPassive(e.getPlayer(), this.getName())) return;
        e.setAmount(hawkeyeDamage(e.getPlayer(), e.getVictim(), e.getAmount()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRangedHit(WeaponDamageEvent e) {
        if (!hasPassive(e.getPlayer(), this.getName())) return;
        e.setAmount(hawkeyeDamage(e.getPlayer(), e.getVictim(), e.getAmount()));
    }

    private int hawkeyeDamage(Player pl, Entity en, int originalAmt) {

        int distance = (int) pl.getLocation().distance(en.getLocation());
        if (distance < DISTANCE) return originalAmt;

        // particles, sounds
        if (!verifyEnemy(pl, en)) return originalAmt;
        pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.25f, 2.0f);
        LivingEntity victim = (LivingEntity) en;
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 0.25f, 2.0f);
        victim.getWorld().spawnParticle(Particle.CRIT, victim.getEyeLocation(),
                5, 0.5F, 0.5F, 0.5F, 0);
        return (int) (originalAmt + DAMAGE + (DAMAGE_PER_LEVEL * pl.getLevel()));
    }

//    @Override
//    public double getDamagePerLevel() {
//        return 0;
//    }
}

