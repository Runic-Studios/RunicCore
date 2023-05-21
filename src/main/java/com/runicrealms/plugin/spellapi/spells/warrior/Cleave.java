package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class Cleave extends Spell implements RadiusSpell {
    private static final double PERCENT = .35;
    private static final int MAX_TARGETS = 3;
    private double radius;

    public Cleave() {
        super("Cleave", CharacterClass.MAGE);
        this.setIsPassive(true);
        this.setDescription("While your &aWhirlwind &7spell is active, " +
                "your basic attacks cleave enemies within " + radius + " " +
                "blocks for " + (int) (PERCENT * 100) + "% damage! Max " + MAX_TARGETS +
                " additional targets.");
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!Whirlwind.getUuidSet().contains(event.getPlayer().getUniqueId())) return;
        if (!event.isBasicAttack()) return; // Only listen for basic attacks
        // Cleave!
        int targetsHit = 0;
        Player player = event.getPlayer();
        for (Entity en : player.getNearbyEntities(radius, radius, radius)) {
            if (!isValidEnemy(player, en)) continue;
            if (en.equals(event.getVictim())) continue;
            if (targetsHit > MAX_TARGETS) return;
            targetsHit++;
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.5f, 1.0f);
            DamageUtil.damageEntityPhysical(event.getAmount() * PERCENT, (LivingEntity) en, player, false, false);
        }
    }
}

