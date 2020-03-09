package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellDamageEvent;
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
public class RunicShield extends Spell {

    private static final int DURATION = 7;
    private List<UUID> shielded = new ArrayList<>();

    public RunicShield() {
        super("Runic Shield",
                "For " + DURATION + " seconds, you gain a shield of runic" +
                        "\nmagic, causing you to become immune" +
                        "\nto all spellʔ damage! This ability has" +
                        "\nno effect versus monsters.",
                ChatColor.WHITE, ClassEnum.ROGUE, 20, 20);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // apply effects
        shielded.add(pl.getUniqueId());
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 2.0f);
        Cone.coneEffect(pl, Particle.REDSTONE, DURATION, 0, 20L, Color.FUCHSIA);
        Cone.coneEffect(pl, Particle.SPELL_WITCH, DURATION, 0, 20L, Color.WHITE);

        new BukkitRunnable() {
            @Override
            public void run() {
                shielded.remove(pl.getUniqueId());
            }
        }.runTaskLater(RunicCore.getInstance(), DURATION*20L);
    }

    /**
     * Reduce damage taken
     */
    @EventHandler
    public void onMagicDamage(SpellDamageEvent e) {
        if (!shielded.contains(e.getEntity().getUniqueId())) return;
        e.setCancelled(true);
    }
}

