package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

public class DivineShield extends Spell {
    private static final double PERCENT = .10;
    private static final double PERCENT_REDUCTION = .25;
    private final HashSet<UUID> shieldedPlayers;
    private final int DURATION = 2;

    public DivineShield() {
        super("Divine Shield", CharacterClass.CLERIC);
        this.setIsPassive(true);
        shieldedPlayers = new HashSet<>();
        this.setDescription("Your healing✦ spells have a " + (int) (PERCENT * 100) + "% " +
                "chance to grant a divine shield to your allies, " +
                "granting them " + (int) (PERCENT_REDUCTION * 100) + "% damage " +
                "reduction for " + DURATION + "s!");
    }

    @EventHandler
    public void onHealingSpell(SpellHealEvent event) {
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > (PERCENT * 100)) return;
        event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 2.0f);
        shieldedPlayers.add(event.getEntity().getUniqueId());
        Cone.coneEffect((LivingEntity) event.getEntity(), Particle.REDSTONE, DURATION, 0, 20, Color.WHITE);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> shieldedPlayers.remove(event.getEntity().getUniqueId()), DURATION * 20L);
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent event) {
        if (!shieldedPlayers.contains(event.getVictim().getUniqueId())) return;
        event.setAmount((int) (event.getAmount() * (1 - PERCENT_REDUCTION)));
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!shieldedPlayers.contains(event.getVictim().getUniqueId())) return;
        event.setAmount((int) (event.getAmount() * (1 - PERCENT_REDUCTION)));
    }

    @EventHandler
    public void onSpellDamage(MagicDamageEvent event) {
        if (!shieldedPlayers.contains(event.getVictim().getUniqueId())) return;
        event.setAmount((int) (event.getAmount() * (1 - PERCENT_REDUCTION)));
    }
}

