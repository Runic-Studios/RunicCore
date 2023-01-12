package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Stormshot extends Spell implements MagicDamageSpell {

    private static final int DAMAGE = 20;
    private static final int DURATION = 7;
    private static final double DAMAGE_PER_LEVEL = 1.0;
    private final Set<UUID> stormPlayers = new HashSet<>();

    public Stormshot() {
        super("Stormshot",
                "You channel the storm for " + DURATION + "s! " +
                        "While the storm persists, each basic attack you fire " +
                        "will instead launch 3 empowered arrows in a cone! " +
                        "The empowered arrows deal an additional (" +
                        DAMAGE + " + &f" + DAMAGE_PER_LEVEL + "x&7 lvl) physical⚔ damage!",
                ChatColor.WHITE, CharacterClass.ARCHER, 14, 30);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.0f);
        stormPlayers.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                () -> stormPlayers.remove(player.getUniqueId()), DURATION * 20L);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isRanged()) return;
        if (!event.isBasicAttack()) return;
        // todo: if arrow is not custom meta data
    }

}
