package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

@SuppressWarnings("FieldCanBeLocal")
public class Seasick extends Spell {

    private static final int DURATION = 6;
    private static final int PERCENT = 10;
    private static final int NAUSEA_MULT = 2;

    public Seasick() {
        super("Seasick",
                "Damaging an enemy has a " + PERCENT + "% chance " +
                        "to cause nausea for " + DURATION + "s!",
                ChatColor.WHITE, ClassEnum.ROGUE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onNauseaHit(MagicDamageEvent e) {
        applyNausea(e.getPlayer(), e.getVictim());
    }

    @EventHandler
    public void onNauseaHit(PhysicalDamageEvent e) {
        applyNausea(e.getPlayer(), e.getVictim());
    }

    private void applyNausea(Player player, Entity entity) {

        if (!hasPassive(player.getUniqueId(), this.getName())) return;

        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > PERCENT) return;

        // particles, sounds
        if (isValidEnemy(player, entity)) {
            LivingEntity victim = (LivingEntity) entity;
            victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.25f, 1.75f);
            victim.getWorld().spawnParticle(Particle.SPELL_WITCH, victim.getEyeLocation(),
                    5, 0.5F, 0.5F, 0.5F, 0);
            victim.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, DURATION * 20, NAUSEA_MULT));
        }
    }
}

