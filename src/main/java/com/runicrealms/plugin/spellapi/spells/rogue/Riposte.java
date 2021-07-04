package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashSet;

@SuppressWarnings({"FieldCanBeLocal", "deprecation"})
public class Riposte extends Spell {

    private static final int DAMAGE_CAP = 50;
    private static final int DURATION = 2;
    private static final double PERCENT_DMG = .65;
    private final HashSet<Entity> ripostePlayers;

    public Riposte() {
        super ("Riposte",
                "For " + DURATION + "s, you parry and counter " +
                        "incoming attacks, avoiding the hit and dealing " +
                        (int) (PERCENT_DMG * 100) + "% damage back to your attacker! " +
                        "Capped at " + DAMAGE_CAP + " against monsters.",
                ChatColor.WHITE, ClassEnum.ROGUE, 20, 20);
        ripostePlayers = new HashSet<>();
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        ripostePlayers.add(pl);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1.5f);
        Cone.coneEffect(pl, Particle.REDSTONE, DURATION, 0, 20, Color.fromRGB(210, 180, 140));
        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, () -> ripostePlayers.remove(pl), DURATION * 20L);
    }

    @EventHandler
    public void onRiposteHit(SpellDamageEvent e) {
        if (!ripostePlayers.contains(e.getEntity())) return;
        Player hurtPl = (Player) e.getEntity();
        double newDamage = e.getAmount() * PERCENT_DMG;
        e.setCancelled(true);
        DamageUtil.damageEntitySpell(newDamage, e.getPlayer(), hurtPl, this);
    }

    @EventHandler
    public void onRiposteHit(WeaponDamageEvent e) {
        if (!ripostePlayers.contains(e.getEntity())) return;
        Player hurtPl = (Player) e.getEntity();
        double newDamage = e.getAmount() * PERCENT_DMG;
        e.setCancelled(true);
        DamageUtil.damageEntityWeapon(newDamage, e.getPlayer(), hurtPl, false, e.isRanged(), true);
    }

    @EventHandler
    public void onMobHit(MobDamageEvent e) {
        if (!ripostePlayers.contains(e.getVictim())) return;
        if (!(e.getDamager() instanceof LivingEntity)) return;
        Player hurtPl = (Player) e.getVictim();
        double newDamage = e.getAmount() * PERCENT_DMG;
        e.setCancelled(true);
        DamageUtil.damageEntityWeapon(newDamage, (LivingEntity) e.getDamager(), hurtPl, false, false, true);
    }
}

