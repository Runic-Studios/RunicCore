package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class Cleave extends Spell {

    private static final double PERCENT = .35;
    private static final int MAX_TARGETS = 3;
    private static final int RADIUS = 4;

    public Cleave() {
        super("Cleave",
                "While your &aEnrage &7spell is active, " +
                        "your basic weapon⚔ attacks cleave enemies within " + RADIUS + " " +
                        "blocks for " + (int) (PERCENT * 100) + "% damage! Max " + MAX_TARGETS +
                        " additional targets.",
                ChatColor.WHITE, ClassEnum.MAGE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        if (!hasPassive(e.getPlayer(), this.getName())) return;
        if (!Enrage.getRagers().contains(e.getPlayer().getUniqueId())) return;
        if (!e.isBasicAttack()) return; // only listen for basic attacks
        // aoe
        int targetsHit = 0;
        Player pl = e.getPlayer();
        for (Entity en : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!verifyEnemy(pl, en)) continue;
            if (en.equals(e.getVictim())) continue;
            if (targetsHit > MAX_TARGETS) return;
            targetsHit++;
            pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.5f, 1.0f);
            DamageUtil.damageEntityWeapon(e.getAmount() * PERCENT, (LivingEntity) en, pl, false, false);
        }
    }
}

