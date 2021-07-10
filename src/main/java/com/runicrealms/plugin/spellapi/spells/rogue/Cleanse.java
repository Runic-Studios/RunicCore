package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;

import java.util.HashSet;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Cleanse extends Spell {

    private static final int DURATION = 5;
    private static final double PERCENT = .25;
    private final HashSet<UUID> damageReductionPlayers;

    public Cleanse() {
        super("Cleanse",
                "You cleanse negative effects " +
                        "on you, freeing you from " +
                        "blindness, slows, silences, and stuns! After, " +
                        "you gain a " + (int) (PERCENT * 100) + "% damage " +
                        "reduction buff for " + DURATION + "s!",
                ChatColor.WHITE, ClassEnum.ROGUE, 15, 15);
        damageReductionPlayers = new HashSet<>();
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0F, 2.0F);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
        for (PotionEffect effect : pl.getActivePotionEffects()) {
            pl.removePotionEffect(effect.getType());
        }
        RunicCore.getSpellManager().getSilencedEntities().remove(pl.getUniqueId());
        RunicCore.getSpellManager().getStunnedEntities().remove(pl.getUniqueId());
        Cone.coneEffect(pl, Particle.REDSTONE, DURATION, 0, 20L, Color.TEAL);
        Cone.coneEffect(pl, Particle.SPELL_INSTANT, DURATION, 0, 20L, Color.WHITE);
        damageReductionPlayers.add(pl.getUniqueId());
        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin,
                () -> damageReductionPlayers.remove(pl.getUniqueId()), DURATION * 20L);
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {
        if (!damageReductionPlayers.contains(e.getEntity().getUniqueId())) return;
        double newAmt = e.getAmount() * (1 - PERCENT);
        e.setAmount((int) newAmt);
    }

    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        if (!damageReductionPlayers.contains(e.getEntity().getUniqueId())) return;
        double newAmt = e.getAmount() * (1 - PERCENT);
        e.setAmount((int) newAmt);
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent e) {
        if (!damageReductionPlayers.contains(e.getVictim().getUniqueId())) return;
        double newAmt = e.getAmount() * (1 - PERCENT);
        e.setAmount((int) newAmt);
    }
}

