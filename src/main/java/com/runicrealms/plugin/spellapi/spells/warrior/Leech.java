package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.UUID;

public class Leech extends Spell implements HealingSpell {

    private static final int BUFF_DURATION = 6;
    private static final int HEAL_AMT = 20;
    private static final double HEALING_PER_LEVEL = 1.25;
    private final HashSet<UUID> leechers = new HashSet<>();

    public Leech() {
        super("Leech",
                "For " + BUFF_DURATION + " seconds, your weapon⚔ " +
                        "attacks restore✦ (" + HEAL_AMT + " + &f" + HEALING_PER_LEVEL +
                        "x&7 lvl) of your health!",
                ChatColor.WHITE, ClassEnum.WARRIOR, 20, 35);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // particles, sounds
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_WANDERING_TRADER_DRINK_POTION, 0.5f, 0.5f);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 0.5f, 1.0f);
        Cone.coneEffect(pl, Particle.REDSTONE, BUFF_DURATION, 0, 20L, Color.RED);

        // apply effect
        leechers.add(pl.getUniqueId());

        // remove buff
        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, () -> {
            pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 0.5f, 1.0f);
            leechers.remove(pl.getUniqueId());
        }, BUFF_DURATION * 20L);
    }

    /*
     * Activate on-hit effects
     */
    @EventHandler
    public void onSuccessfulHit(WeaponDamageEvent e) {
        if (!leechers.contains(e.getPlayer().getUniqueId())) return;
        if (e.isCancelled()) return;
        Player pl = e.getPlayer();
        HealUtil.healPlayer(HEAL_AMT, pl, pl, false, this);
    }

    @Override
    public double getHealingPerLevel() {
        return HEALING_PER_LEVEL;
    }
}

