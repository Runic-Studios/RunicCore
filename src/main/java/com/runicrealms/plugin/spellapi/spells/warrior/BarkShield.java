package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class BarkShield extends Spell {

    private static final int DURATION = 7;
    private List<UUID> evaders = new ArrayList<>();

    public BarkShield() {
        super("Bark Shield",
                "For " + DURATION + " seconds, you evade incoming" +
                        "\nattacks, causing you to become immune" +
                        "\nto all weapon⚔ damage! This ability has" +
                        "\nno effect versus monsters.",
                ChatColor.WHITE, ClassEnum.WARRIOR, 20, 25);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 0.5f);

        // apply effects
        evaders.add(pl.getUniqueId());
        new BukkitRunnable() {
            int count = 1;
            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                } else {
                    count += 1;
                    pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 2.0f);
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 20L);
        Cone.coneEffect(pl, Particle.CRIT, DURATION, 0, 20L, Color.WHITE);

        new BukkitRunnable() {
            @Override
            public void run() {
                evaders.remove(pl.getUniqueId());
            }
        }.runTaskLater(RunicCore.getInstance(), DURATION*20L);
    }

    /**
     * Reduce weapon damage taken
     */
    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        if (!evaders.contains(e.getEntity().getUniqueId())) return;
        e.setCancelled(true);
    }
}
