package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;

public class TippedArrows extends Spell implements MagicDamageSpell {

    private static final int DAMAGE = 4;
    private static final int DURATION = 2;
    private static final int PERIOD = 1;
    private static final double DAMAGE_PER_LEVEL = 0.5;

    public TippedArrows() {
        super("Tipped Arrows",
                "Your basic attack arrows are now tipped with poison, " +
                        "causing the target to suffer an additional (" +
                        DAMAGE + " + &f" + DAMAGE_PER_LEVEL + "x&7 lvl) " +
                        "magicʔ damage every " + PERIOD + "s for " + DURATION + "s!",
                ChatColor.WHITE, ClassEnum.ARCHER, 0, 0);
        this.setIsPassive(true);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRangedHit(PhysicalDamageEvent event) {
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!event.isBasicAttack()) return;
        Spell spell = this;
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                } else {
                    count += PERIOD;
                    event.getVictim().getWorld().spawnParticle(Particle.REDSTONE, event.getVictim().getLocation(),
                            5, 0.2f, 0.2f, 0.2f, 0.2f, new Particle.DustOptions(Color.GREEN, 1));
                    DamageUtil.damageEntitySpell(DAMAGE, event.getVictim(), event.getPlayer(), spell);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, PERIOD * 20L);
    }
}

