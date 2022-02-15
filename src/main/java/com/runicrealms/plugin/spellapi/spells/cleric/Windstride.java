package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@SuppressWarnings("FieldCanBeLocal")
public class Windstride extends Spell {

    private static final int BUFF_DURATION = 10;
    private static final int SPEED_AMPLIFIER = 2;
    private static final int RADIUS = 10;

    public Windstride() {
        super("Windstride",
                "For " + BUFF_DURATION + "s, you grant a massive " +
                        "speed boost to yourself and all " +
                        "allies within " + RADIUS + " blocks!",
                ChatColor.WHITE, ClassEnum.CLERIC, 20, 15);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        applySpell(pl);
        for (Entity en : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!verifyAlly(pl, en)) continue;
            applySpell((Player) en);
        }
    }

    private void applySpell(Player pl) {

        // begin sound effects
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5F, 0.7F);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5F, 0.7F);

        // add player effects
        pl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, BUFF_DURATION * 20, SPEED_AMPLIFIER));
        pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation(),
                25, 0.5f, 0.5f, 0.5f, 0, new Particle.DustOptions(Color.WHITE, 20));
    }
}
