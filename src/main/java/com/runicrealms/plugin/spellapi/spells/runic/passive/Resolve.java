package com.runicrealms.plugin.spellapi.spells.runic.passive;

import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@SuppressWarnings({"FieldCanBeLocal", "deprecation"})
public class Resolve extends Spell {

    private static final double PERCENT_HP = 25;
    private static final double PERCENT_DMG = 50;

    public Resolve() {
        super ("Resolve",
                "While below " + PERCENT_HP + "% health, you" +
                        "\ntake " + PERCENT_DMG + "% damage!",
                ChatColor.WHITE, 12, 15);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onResolvedHit(SpellDamageEvent e) {

        if (!(e.getEntity() instanceof Player)) return;
        Player hurtPl = (Player) e.getEntity();

        if (getRunicPassive(hurtPl) == null) return;
        if (!getRunicPassive(hurtPl).equals(this)) return;

        double percent = PERCENT_HP / 100;
        double threshold = percent * hurtPl.getMaxHealth();
        if (hurtPl.getHealth() > threshold) return;

        e.setAmount(getResolved(hurtPl, e.getAmount()));
    }

    @EventHandler
    public void onResolvedHit(WeaponDamageEvent e) {

        if (!(e.getEntity() instanceof Player)) return;
        Player hurtPl = (Player) e.getEntity();

        if (getRunicPassive(hurtPl) == null) return;
        if (!getRunicPassive(hurtPl).equals(this)) return;

        double percent = PERCENT_HP / 100;
        double threshold = percent * hurtPl.getMaxHealth();
        if (hurtPl.getHealth() > threshold) return;

        e.setAmount(getResolved(hurtPl, e.getAmount()));
    }

    @EventHandler
    public void onMobHit(MobDamageEvent e) {

        if (!(e.getVictim() instanceof Player)) return;
        Player hurtPl = (Player) e.getVictim();

        if (getRunicPassive(hurtPl) == null) return;
        if (!getRunicPassive(hurtPl).equals(this)) return;

        double percent = PERCENT_HP / 100;
        double threshold = percent * hurtPl.getMaxHealth();
        if (hurtPl.getHealth() > threshold) return;

        e.setAmount(getResolved(hurtPl, e.getAmount()));
    }

    private int getResolved(Player pl, int damage) {

        // reduce damage
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 2.0f);
        pl.getWorld().spawnParticle(Particle.BLOCK_DUST, pl.getEyeLocation(),
                5, 0.5F, 0.5F, 0.5F, 0, Material.OAK_WOOD.createBlockData());

        double percent = PERCENT_DMG / 100;
        return (int) (damage * percent);
    }
}

