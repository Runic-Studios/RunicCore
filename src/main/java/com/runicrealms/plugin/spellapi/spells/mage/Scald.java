package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

/**
 * Logic for hit found in Fireball.
 */
public class Scald extends Spell {

    private static final double DAMAGE_PERCENT = .25;
    private static final int RADIUS = 4;

    public Scald() {
        super("Scald",
                "Your &aFireball &7spell now deals " +
                        (int) (DAMAGE_PERCENT * 100) + "% magicʔ damage to enemies within " +
                        RADIUS + " blocks!",
                ChatColor.WHITE, ClassEnum.MAGE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMagicDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!(event.getSpell() instanceof Fireball)) return;
        for (Entity entity : event.getVictim().getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!isValidEnemy(event.getPlayer(), entity)) continue;
            DamageUtil.damageEntitySpell(event.getAmount() * DAMAGE_PERCENT, (LivingEntity) entity, event.getPlayer());
        }
    }
}

