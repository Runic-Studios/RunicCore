package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
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

    private int hawkeyeDamage(Player player, Entity en, int originalAmt) {

        int distance = (int) player.getLocation().distanceSquared(en.getLocation());
        if (distance < DISTANCE * DISTANCE) return originalAmt;

        // particles, sounds
        if (!isValidEnemy(player, en)) return originalAmt;
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.25f, 2.0f);
        LivingEntity victim = (LivingEntity) en;
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 0.25f, 2.0f);
        victim.getWorld().spawnParticle(Particle.CRIT, victim.getEyeLocation(),
                5, 0.5F, 0.5F, 0.5F, 0);
        return (int) (originalAmt + DAMAGE + (DAMAGE_PER_LEVEL * player.getLevel()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRangedHit(PhysicalDamageEvent e) {
        if (!hasPassive(e.getPlayer().getUniqueId(), this.getName())) return;
        e.setAmount(hawkeyeDamage(e.getPlayer(), e.getVictim(), e.getAmount()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRangedHit(MagicDamageEvent e) {
        if (!hasPassive(e.getPlayer().getUniqueId(), this.getName())) return;
        e.setAmount(hawkeyeDamage(e.getPlayer(), e.getVictim(), e.getAmount()));
    }

//    @Override
//    public double getDamagePerLevel() {
//        return 0;
//    }
}

