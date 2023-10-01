package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Random;

public class Kneebreak extends Spell {
    private static final int DURATION = 2;
    private static final int PERCENT = 25;
    private static final int SLOW_MULT = 2;

    public Kneebreak() {
        super("Kneebreak", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("Damaging an enemy has a " + PERCENT + "% chance " +
                "to slow them for " + DURATION + "s!");
    }

    @EventHandler(priority = EventPriority.HIGH) // late
    public void onKneebreakHit(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        tryToApplySlow(event.getPlayer(), event.getVictim());
    }

    @EventHandler(priority = EventPriority.HIGH) // late
    public void onKneebreakHit(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        tryToApplySlow(event.getPlayer(), event.getVictim());
    }

    /**
     * @param player
     * @param en
     */
    private void tryToApplySlow(Player player, Entity en) {

        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > PERCENT) return;

        // particles, sounds
        if (isValidEnemy(player, en)) {
            LivingEntity victim = (LivingEntity) en;
            victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.25f, 1.75f);
            victim.getWorld().spawnParticle(Particle.REDSTONE, victim.getLocation(),
                    25, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.fromRGB(210, 180, 140), 3));
            addStatusEffect(victim, RunicStatusEffect.SLOW_III, DURATION, false);
        }
    }
}

