package com.runicrealms.plugin.spellapi.spells.runic;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizCircleFrame;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import io.lumine.xikage.mythicmobs.skills.SkillCaster;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.SkillTrigger;
import org.bukkit.*;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@SuppressWarnings("FieldCanBeLocal")
public class Sprint extends Spell {

    // global variables
    private static int BUFF_DURATION = 7;
    private static int SPEED_AMPLIFIER = 2;

    // constructor
    public Sprint() {
        super("Sprint",
                "For " + BUFF_DURATION + " seconds, you gain a" +
                        "\nmassive boost to your speed!", ChatColor.WHITE,10, 10);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // apply effects
        pl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, BUFF_DURATION*20, SPEED_AMPLIFIER));
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5F, 1.0F);
        new HorizCircleFrame(1).playParticle(Particle.TOTEM, pl.getLocation(), Color.FUCHSIA);
        new HorizCircleFrame(1).playParticle(Particle.TOTEM, pl.getEyeLocation(), Color.FUCHSIA);
        pl.sendMessage(ChatColor.GREEN + "You gain increased speed!");

        // after the end of the buff
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
            pl.sendMessage(ChatColor.GRAY + "Your speed effect has worn off!");
        }, BUFF_DURATION*20);
    }
}

