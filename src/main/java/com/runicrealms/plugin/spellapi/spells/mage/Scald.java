package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

/**
 * Logic for hit found in Fireball.
 */
public class Scald extends Spell implements RadiusSpell {
    private static final double DAMAGE_PERCENT = .5;
    private double radius;

    public Scald() {
        super("Scald", CharacterClass.MAGE);
        this.setIsPassive(true);
        this.setDescription("Your &aFireball &7spell now deals " +
                (int) (DAMAGE_PERCENT * 100) + "% magicʔ damage to enemies within " +
                radius + " blocks!");
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMagicDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!(event.getSpell() instanceof Fireball || event.getSpell() instanceof Frostbolt))
            return;
        Player player = event.getPlayer();
        Entity victim = event.getVictim();
        for (Entity entity : victim.getWorld().getNearbyEntities(victim.getLocation(), radius, radius, radius, target -> isValidEnemy(player, target))) {
            if (entity.equals(victim)) continue;
            DamageUtil.damageEntitySpell(event.getAmount() * DAMAGE_PERCENT, (LivingEntity) entity, player);
        }
    }
}

