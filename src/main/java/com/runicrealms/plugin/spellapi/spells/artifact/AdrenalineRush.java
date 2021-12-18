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

    public AdrenalineRush() {
        super("Adrenaline Rush", "", ChatColor.WHITE, ClassEnum.ROGUE, 0, 0);
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
        return "scarlet-rapier";
    }

    @Override
    public double getChance() {
        return 0.15;
    }
}

