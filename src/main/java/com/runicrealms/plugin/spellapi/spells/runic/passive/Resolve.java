package com.runicrealms.plugin.spellapi.spells.runic.passive;

import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@SuppressWarnings("FieldCanBeLocal")
public class Resolve extends Spell {

    private static final int PERCENT_HP = 25;
    private static final int PERCENT_DMG = 50;

    public Resolve() {
        super ("Resolve",
                "While below " + PERCENT_HP + "% health, you" +
                        "\ntake " + PERCENT_DMG + "% damage!",
                ChatColor.WHITE, 12, 15);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onSpeedyHit(SpellDamageEvent e) {

        if (getRunicPassive(e.getPlayer()) == null) return;
        if (!getRunicPassive(e.getPlayer()).equals(this)) return;

        int threshold = (int) e.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * PERCENT_HP;
        if (e.getPlayer().getHealth() > threshold) return;

        e.setAmount(getResolved(e.getPlayer(), e.getAmount()));
    }

    @EventHandler
    public void onSpeedyHit(WeaponDamageEvent e) {

        if (getRunicPassive(e.getPlayer()) == null) return;
        if (!getRunicPassive(e.getPlayer()).equals(this)) return;

        int threshold = (int) e.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * PERCENT_HP;
        if (e.getPlayer().getHealth() > threshold) return;

        e.setAmount(getResolved(e.getPlayer(), e.getAmount()));
    }

    private int getResolved(Player pl, int damage) {

        // reduce damage
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 2.0f);
        pl.getWorld().spawnParticle(Particle.BLOCK_DUST, pl.getEyeLocation(),
                5, 0.5F, 0.5F, 0.5F, 0, Material.OAK_WOOD.createBlockData());
        return damage*PERCENT_DMG;
    }
}

