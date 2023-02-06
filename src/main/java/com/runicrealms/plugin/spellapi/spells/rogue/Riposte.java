package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashSet;

public class Riposte extends Spell {

    private static final int DURATION = 4;
    private static final double DURATION_STUN = 2.5;
    private final HashSet<Entity> ripostePlayers;

    public Riposte() {
        super("Riposte",
                "For " + DURATION + "s, you evade and counter " +
                        "incoming attacks, avoiding all incoming damage. " +
                        "Any enemy that attacks you during this time is " +
                        "stunned for " + DURATION_STUN + "s!",
                ChatColor.WHITE, CharacterClass.ROGUE, 18, 20);
        ripostePlayers = new HashSet<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        ripostePlayers.add(player);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1.5f);
        Cone.coneEffect(player, Particle.REDSTONE, DURATION, 0, 20, Color.fromRGB(210, 180, 140));
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> ripostePlayers.remove(player), DURATION * 20L);
    }

    @EventHandler
    public void onMobHit(MobDamageEvent event) {
        if (!ripostePlayers.contains(event.getVictim())) return;
        if (!(event.getDamager() instanceof LivingEntity)) return;
        event.setCancelled(true);
        addStatusEffect((LivingEntity) event.getDamager(), RunicStatusEffect.STUN, DURATION_STUN, true);
    }

    @EventHandler
    public void onRiposteHit(PhysicalDamageEvent event) {
        if (!ripostePlayers.contains(event.getVictim())) return;
        event.setCancelled(true);
        addStatusEffect(event.getPlayer(), RunicStatusEffect.STUN, DURATION_STUN, true);
    }

    @EventHandler
    public void onRiposteHit(MagicDamageEvent event) {
        if (!ripostePlayers.contains(event.getVictim())) return;
        event.setCancelled(true);
        addStatusEffect(event.getPlayer(), RunicStatusEffect.STUN, DURATION_STUN, true);
    }
}

