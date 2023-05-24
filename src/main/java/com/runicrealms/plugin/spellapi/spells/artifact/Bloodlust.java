package com.runicrealms.plugin.spellapi.spells.artifact;

import com.runicrealms.plugin.item.artifact.event.RunicItemArtifactTriggerEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.ArtifactSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.concurrent.ThreadLocalRandom;

public class Bloodlust extends Spell implements ArtifactSpell {

    private static final int HEAL_AMOUNT = 10;
    private static final double CHANCE = 0.25;
    private static final String ARTIFACT_ID = "corruption";

    public Bloodlust() {
        super("Bloodlust", CharacterClass.MAGE);
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
    public void onArtifactUse(RunicItemArtifactTriggerEvent event) {
        if (!event.getRunicItemArtifact().getTemplateId().equals(getArtifactId())) return;
        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll > getChance()) return;
        healPlayer(event.getPlayer(), event.getPlayer(), HEAL_AMOUNT);
    }
}

