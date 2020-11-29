package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.SubClassEnum;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.UUID;

public class Leech extends Spell<SubClassEnum> {

    private static final int BUFF_DURATION = 6;
    private static final int HEAL_AMT = 10;
    private final HashSet<UUID> leechers = new HashSet<>();

    public Leech() {
        super("Leech",
                "For " + BUFF_DURATION + " seconds, your weapon⚔" +
                        "\nattacks restore✦ " + HEAL_AMT + " of your" +
                        "\nhealth!",
                ChatColor.WHITE, SubClassEnum.BERSERKER, 20, 35);
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
        new BukkitRunnable() {
            @Override
            public void run() {
                leechers.remove(pl.getUniqueId());
            }
        }.runTaskLater(plugin, BUFF_DURATION * 20);
    }

    /*
     * Activate on-hit effects
     */
    @EventHandler
    public void onSuccessfulHit(WeaponDamageEvent e) {
        if (!leechers.contains(e.getPlayer().getUniqueId()))
            return;
        if(e.isCancelled())
            return;
        Player pl = e.getPlayer();
        HealUtil.healPlayer(HEAL_AMT, pl, pl, true, false, false);
    }
}

