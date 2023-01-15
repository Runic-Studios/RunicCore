package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
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
                        "your basic attacks cleave enemies within " + RADIUS + " " +
                        "blocks for " + (int) (PERCENT * 100) + "% damage! Max " + MAX_TARGETS +
                        " additional targets.",
                ChatColor.WHITE, CharacterClass.MAGE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!Enrage.getRagers().contains(event.getPlayer().getUniqueId())) return;
        if (!event.isBasicAttack()) return; // only listen for basic attacks
        // aoe
        int targetsHit = 0;
        Player player = event.getPlayer();
        for (Entity en : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!isValidEnemy(player, en)) continue;
            if (en.equals(event.getVictim())) continue;
            if (targetsHit > MAX_TARGETS) return;
            targetsHit++;
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.5f, 1.0f);
            DamageUtil.damageEntityPhysical(event.getAmount() * PERCENT, (LivingEntity) en, player, false, false);
        }
    }
}

