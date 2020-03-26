package com.runicrealms.plugin.spellapi.spells.runic.passive;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
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
public class Siphon extends Spell {

    private static final int AMOUNT = 5;
    private static final int PERCENT = 10;

    public Siphon() {
        super ("Siphon",
                "Damaging an enemy has a " + (int) PERCENT + "% chance" +
                        "\nto restore✦ " + AMOUNT + " of your mana!",
                ChatColor.WHITE, ClassEnum.RUNIC, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onDrainingHit(SpellDamageEvent e) {
        restoreMana(e.getPlayer(), e.getEntity());
    }

    @EventHandler
    public void onDrainingHit(WeaponDamageEvent e) {
        restoreMana(e.getPlayer(), e.getEntity());
    }

    private void restoreMana(Player pl, Entity en) {

        if (getRunicPassive(pl) == null) return;
        if (!getRunicPassive(pl).equals(this)) return;

        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > PERCENT) return;

        if (verifyEnemy(pl, en)) {
            LivingEntity victim = (LivingEntity) en;
            RunicCore.getManaManager().addMana(pl, AMOUNT, true);
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WITCH_DRINK, 0.25f, 2f);
            victim.getWorld().spawnParticle(Particle.SPELL_WITCH, victim.getEyeLocation(), 3, 0.3F, 0.3F, 0.3F, 0);
        }
    }
}

