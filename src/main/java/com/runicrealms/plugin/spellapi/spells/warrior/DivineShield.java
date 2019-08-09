package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class DivineShield extends Spell {

    private static final int DURATION = 7;
    private List<UUID> shielded = new ArrayList<>();

    public DivineShield() {
        super("Divine Shield",
                "For " + DURATION + " seconds, you gain a shield of holy" +
                        "\nmagic, causing you to become immune" +
                        "\nto all spellʔ damage! This ability has" +
                        "\nno effect versus monsters.", ChatColor.WHITE,20, 20);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // apply effects
        shielded.add(pl.getUniqueId());
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 2.0f);
        Cone.coneEffect(pl, Particle.REDSTONE, DURATION, 0, 20L, Color.WHITE);
        Cone.coneEffect(pl, Particle.SPELL_INSTANT, DURATION, 0, 20L, Color.WHITE);

        new BukkitRunnable() {
            @Override
            public void run() {
                shielded.remove(pl.getUniqueId());
            }
        }.runTaskLater(RunicCore.getInstance(), DURATION*20L);
    }

    /**
     * Reduce damage taken
     */
    @EventHandler
    public void onMagicDamage(SpellDamageEvent e) {
        if (!shielded.contains(e.getEntity().getUniqueId())) return;
        e.setCancelled(true);
    }
}

