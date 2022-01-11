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

public class Bloodlust extends Spell implements ArtifactSpell {

    private static final int HEAL_AMOUNT = 10;
    private static final double CHANCE = 0.25;
    private static final String ARTIFACT_ID = "corruption";

    public Bloodlust() {
        super("Bloodlust", "", ChatColor.WHITE, ClassEnum.MAGE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onArtifactUse(RunicItemArtifactTriggerEvent e) {
        if (!e.getRunicItemArtifact().getTemplateId().equals(getArtifactId())) return;
        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll > getChance()) return;
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

