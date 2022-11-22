package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CureScurvy extends Spell {

    private static final int DURATION = 5;
    private static final double PERCENT = .25;
    private static final Set<PotionEffectType> POTION_EFFECT_SET = new HashSet<PotionEffectType>() {{
        add(PotionEffectType.BLINDNESS);
        add(PotionEffectType.SLOW);
    }};
    private final HashSet<UUID> damageReductionPlayers;

    public CureScurvy() {
        super("Cure Scurvy",
                "You cleanse negative effects " +
                        "on you, freeing you from " +
                        "blindness, slows, silences, and stuns! After, " +
                        "you gain a " + (int) (PERCENT * 100) + "% damage " +
                        "reduction buff for " + DURATION + "s!",
                ChatColor.WHITE, ClassEnum.ROGUE, 15, 15);
        damageReductionPlayers = new HashSet<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0F, 2.0F);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
        // Remove negative potion effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (POTION_EFFECT_SET.contains(effect.getType()))
                player.removePotionEffect(effect.getType());
        }
        // Remove negative runic effects
        for (RunicStatusEffect runicStatusEffect : RunicStatusEffect.values()) {
            if (runicStatusEffect.isBuff()) continue;
            removeStatusEffect(player, runicStatusEffect);
        }
        Cone.coneEffect(player, Particle.REDSTONE, DURATION, 0, 20L, Color.ORANGE);
        damageReductionPlayers.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
                () -> damageReductionPlayers.remove(player.getUniqueId()), DURATION * 20L);
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent event) {
        if (!damageReductionPlayers.contains(event.getVictim().getUniqueId())) return;
        double newAmt = event.getAmount() * (1 - PERCENT);
        event.setAmount((int) newAmt);
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!damageReductionPlayers.contains(event.getVictim().getUniqueId())) return;
        double newAmt = event.getAmount() * (1 - PERCENT);
        event.setAmount((int) newAmt);
    }

    @EventHandler
    public void onSpellDamage(MagicDamageEvent event) {
        if (!damageReductionPlayers.contains(event.getVictim().getUniqueId())) return;
        double newAmt = event.getAmount() * (1 - PERCENT);
        event.setAmount((int) newAmt);
    }
}

