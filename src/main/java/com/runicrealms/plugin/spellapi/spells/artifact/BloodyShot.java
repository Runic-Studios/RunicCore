package com.runicrealms.plugin.spellapi.spells.artifact;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.ArtifactSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.runicitems.item.event.RunicArtifactOnHitEvent;
import com.runicrealms.runicitems.item.event.RunicItemArtifactTriggerEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.concurrent.ThreadLocalRandom;

public class BloodyShot extends Spell implements ArtifactSpell {

    private static final int DAMAGE_AMOUNT = 10;
    private static final double CHANCE = 0.15;
    private static final String ARTIFACT_ID = "sanguine-longbow";

    public BloodyShot() {
        super("Bloody Shot", "", ChatColor.WHITE, CharacterClass.ARCHER, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onArtifactUse(RunicItemArtifactTriggerEvent e) {
        if (!e.getRunicItemArtifact().getTemplateId().equals(getArtifactId())) return;
        if (!(e instanceof RunicArtifactOnHitEvent)) return;
        RunicArtifactOnHitEvent onHitEvent = (RunicArtifactOnHitEvent) e;
        if (onHitEvent.getVictim() == null) return;
        if (!(onHitEvent.getVictim() instanceof LivingEntity)) return;
        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll > getChance()) return;
        DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, (LivingEntity) onHitEvent.getVictim(), e.getPlayer());
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

