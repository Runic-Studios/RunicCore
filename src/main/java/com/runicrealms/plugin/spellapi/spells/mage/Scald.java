package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

/**
 * Logic for hit found in Fireball.
 */
public class Scald extends Spell {
    private static final double DAMAGE_PERCENT = .5;
    private static final int RADIUS = 4;

    public Scald() {
        super("Scald",
                "Your &aFireball &7spell now deals " +
                        (int) (DAMAGE_PERCENT * 100) + "% magicʔ damage to enemies within " +
                        RADIUS + " blocks!",
                ChatColor.WHITE, CharacterClass.MAGE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMagicDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!(event.getSpell() instanceof Fireball || event.getSpell() instanceof Frostbolt)) return;
        Player player = event.getPlayer();
        Entity victim = event.getVictim();
        for (Entity entity : victim.getWorld().getNearbyEntities(victim.getLocation(), RADIUS, RADIUS, RADIUS, target -> isValidEnemy(player, target))) {
            DamageUtil.damageEntitySpell(event.getAmount() * DAMAGE_PERCENT, (LivingEntity) entity, player);
        }
    }
}

