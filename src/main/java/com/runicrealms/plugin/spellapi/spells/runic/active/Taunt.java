package com.runicrealms.plugin.spellapi.spells.runic.active;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Taunt extends Spell {

    private static final int DURATION = 4;
    private final List<UUID> taunters;

    public Taunt() {
        super ("Taunt",
                "For " + DURATION + " seconds, damaging monsters" +
                        "\ngenerates a large amount of threat!",
                ChatColor.WHITE, ClassEnum.RUNIC, 12, 20);
        taunters = new ArrayList<>();
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        taunters.add(pl.getUniqueId());
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 0.5f);

        new BukkitRunnable() {
            @Override
            public void run() {
                taunters.remove(pl.getUniqueId());
            }
        }.runTaskLaterAsynchronously(RunicCore.getInstance(), DURATION*20L);
    }

    @EventHandler
    public void onDrainingHit(SpellDamageEvent e) {
        generateThreat(e.getPlayer(), e.getEntity());
    }

    @EventHandler
    public void onDrainingHit(WeaponDamageEvent e) {
        generateThreat(e.getPlayer(), e.getEntity());
    }

    private void generateThreat(Player pl, Entity en) {

        if (!taunters.contains(pl.getUniqueId())) return;

        if (verifyEnemy(pl, en)) {
            LivingEntity victim = (LivingEntity) en;
            if (victim instanceof Monster) { //  || victim instanceof Wolf || victim instanceof PolarBear
                en.getWorld().playSound(en.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.1f, 0.2f);
                victim.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, victim.getEyeLocation(), 1, 0.3F, 0.3F, 0.3F, 0);
                ((Monster) en).setTarget(pl);
                //MythicMobs.inst().getAPIHelper().taunt(en, pl);
                //((ActiveMob) en).getThreatTable().asMap().put((AbstractEntity) pl, ((ActiveMob) en).getThreatTable().getTopTargetThreat() * 3.0D); //1.1
                MythicMobs.inst().getAPIHelper().addThreat(en, pl, 10000);
                ((ActiveMob) en).getThreatTable().threatGain((AbstractEntity) pl, ((ActiveMob) en).getThreatTable().getTopTargetThreat() * 3.0D);
            }
        }
    }
}

