package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Random;

@SuppressWarnings("FieldCanBeLocal")
public class Dissonance extends Spell {

    private static final int DURATION = 3;
    private static final double PERCENT = 10;

    public Dissonance() {
        super("Dissonance",
                "Damaging an enemy player has a " + (int) PERCENT + "% chance " +
                        "to swap their held item to a random hotbar slot! " +
                        "Against monsters, the effect silences for " + DURATION + "s instead.",
                ChatColor.WHITE, ClassEnum.CLERIC, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onBlindingHit(SpellDamageEvent e) {
        if (!hasPassive(e.getPlayer().getUniqueId(), this.getName())) return;
        swapHotBar(e.getPlayer(), e.getVictim());
    }

    @EventHandler
    public void onBlindingHit(WeaponDamageEvent e) {
        if (!hasPassive(e.getPlayer().getUniqueId(), this.getName())) return;
        swapHotBar(e.getPlayer(), e.getVictim());
    }

    private void swapHotBar(Player pl, Entity en) {

        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > PERCENT) return;

        // particles, sounds
        if (verifyEnemy(pl, en)) {
            LivingEntity victim = (LivingEntity) en;
            victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_NOTE_BLOCK_COW_BELL, 0.5f, 1.75f);
            pl.getWorld().spawnParticle(Particle.CRIT_MAGIC, victim.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f);
            int slot = rand.nextInt(10);
            if (en instanceof Player) {
                while (((Player) en).getInventory().getHeldItemSlot() == slot)
                    slot = rand.nextInt(10); // make sure it doesn't pick their current slot
                ((Player) en).getInventory().setHeldItemSlot(slot);
            } else
                addStatusEffect(en, EffectEnum.SILENCE, DURATION);
        }
    }
}

