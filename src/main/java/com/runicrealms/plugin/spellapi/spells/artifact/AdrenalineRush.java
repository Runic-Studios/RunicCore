package com.runicrealms.plugin.spellapi.spells.artifact;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.ArtifactSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import com.runicrealms.runicitems.item.event.RunicItemArtifactTriggerEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.concurrent.ThreadLocalRandom;

public class AdrenalineRush extends Spell implements ArtifactSpell {

    private static final int HEAL_AMOUNT = 10;
    private static final double CHANCE = 0.15;
    private static final String ARTIFACT_ID = "scarlet-rapier";

    public AdrenalineRush() {
        super("Adrenaline Rush", "", ChatColor.WHITE, ClassEnum.ROGUE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onArtifactUse(RunicItemArtifactTriggerEvent e) {
        if (!e.getRunicItemArtifact().getTemplateId().equals(ARTIFACT_ID)) return;
        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll > CHANCE) return;
        HealUtil.healPlayer(HEAL_AMOUNT, e.getPlayer(), e.getPlayer(), false);
    }

    @Override
    public String getArtifactId() {
        return ARTIFACT_ID;
    }

    @Override
    public double getChance() {
        return CHANCE;
    }
}

