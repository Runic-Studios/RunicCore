package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

public class DivineShield extends Spell {

    private static final int DURATION = 2;
    private static final double PERCENT = .10;
    private static final double PERCENT_REDUCTION = .25;
    private final HashSet<UUID> shieldedPlayers;

    public DivineShield() {
        super ("Divine Shield",
                "Your healing✦ spells have a " + (int) (PERCENT * 100) + "% " +
                        "chance to grant a divine shield to your allies, " +
                        "granting them " + (int) (PERCENT_REDUCTION * 100) + "% damage " +
                        "reduction for " + DURATION + "s!",
                ChatColor.WHITE, ClassEnum.CLERIC, 0, 0);
        this.setIsPassive(true);
        shieldedPlayers = new HashSet<>();
    }

    @EventHandler
    public void onHealingSpell(SpellHealEvent e) {
        if (!hasPassive(e.getPlayer(), this.getName())) return;
        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > (PERCENT * 100)) return;
        e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 2.0f);
        shieldedPlayers.add(e.getEntity().getUniqueId());
        Cone.coneEffect((LivingEntity) e.getEntity(), Particle.REDSTONE, DURATION, 0, 20, Color.WHITE);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> shieldedPlayers.remove(e.getEntity().getUniqueId()), DURATION * 20L);
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent e) {
        if (!shieldedPlayers.contains(e.getVictim().getUniqueId())) return;
        e.setAmount((int) (e.getAmount() * (1 - PERCENT_REDUCTION)));
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {
        if (!shieldedPlayers.contains(e.getEntity().getUniqueId())) return;
        e.setAmount((int) (e.getAmount() * (1 - PERCENT_REDUCTION)));
    }

    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        if (!shieldedPlayers.contains(e.getEntity().getUniqueId())) return;
        e.setAmount((int) (e.getAmount() * (1 - PERCENT_REDUCTION)));
    }
}

