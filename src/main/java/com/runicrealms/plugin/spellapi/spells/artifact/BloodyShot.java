package com.runicrealms.plugin.spellapi.spells.artifact;

import com.runicrealms.plugin.item.artifact.event.RunicArtifactOnHitEvent;
import com.runicrealms.plugin.item.artifact.event.RunicItemArtifactTriggerEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.ArtifactSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.concurrent.ThreadLocalRandom;

public class BloodyShot extends Spell implements ArtifactSpell {

    private static final int DAMAGE_AMOUNT = 10;
    private static final double CHANCE = 0.15;
    private static final String ARTIFACT_ID = "sanguine-longbow";

    public BloodyShot() {
        super("Bloody Shot", CharacterClass.ARCHER);
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
        if (!(event instanceof RunicArtifactOnHitEvent onHitEvent)) return;
        if (onHitEvent.getVictim() == null) return;
        if (!(onHitEvent.getVictim() instanceof LivingEntity)) return;
        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll > getChance()) return;
        DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, (LivingEntity) onHitEvent.getVictim(), event.getPlayer());
    }
}

