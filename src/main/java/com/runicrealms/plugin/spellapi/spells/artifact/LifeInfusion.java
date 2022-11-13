package com.runicrealms.plugin.spellapi.spells.artifact;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.ArtifactSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import com.runicrealms.runicitems.item.event.RunicArtifactOnKillEvent;
import com.runicrealms.runicitems.item.event.RunicItemArtifactTriggerEvent;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class LifeInfusion extends Spell implements ArtifactSpell {

    private static final int HEAL_AMOUNT = 20;
    private static final double CHANCE = 1.0;
    private static final String ARTIFACT_ID = "crimson-maul";

    public LifeInfusion() {
        super("Life Infusion", "", ChatColor.WHITE, ClassEnum.CLERIC, 0, 0);
        this.setIsPassive(true);
    }

    @Override
    public String getArtifactId() {
        return ARTIFACT_ID;
    }

    @Override
    public double getChance() {
        return CHANCE;
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onArtifactUse(RunicItemArtifactTriggerEvent e) {
        if (!e.getRunicItemArtifact().getTemplateId().equals(getArtifactId())) return;
        if (!(e instanceof RunicArtifactOnKillEvent)) return;
        RunicArtifactOnKillEvent onKillEvent = (RunicArtifactOnKillEvent) e;
        if (onKillEvent.getVictim() == null) return;
        if (!(onKillEvent.getVictim() instanceof LivingEntity)) return;
        onKillEvent.getVictim().getWorld().spawnParticle
                (
                        Particle.REDSTONE,
                        onKillEvent.getVictim().getLocation(),
                        15,
                        0.25f,
                        0.25f,
                        0.25f,
                        0,
                        new Particle.DustOptions(Color.RED, 3)
                );
        HealUtil.healPlayer(HEAL_AMOUNT, e.getPlayer(), e.getPlayer(), false);
    }
}

