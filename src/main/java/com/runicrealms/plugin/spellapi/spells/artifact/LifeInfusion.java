//package com.runicrealms.plugin.spellapi.spells.artifact;
//
//import com.runicrealms.plugin.item.artifact.event.RunicArtifactOnKillEvent;
//import com.runicrealms.plugin.item.artifact.event.RunicItemArtifactTriggerEvent;
//import com.runicrealms.plugin.common.CharacterClass;
//import com.runicrealms.plugin.spellapi.spelltypes.ArtifactSpell;
//import com.runicrealms.plugin.spellapi.spelltypes.Spell;
//import org.bukkit.Color;
//import org.bukkit.Particle;
//import org.bukkit.entity.LivingEntity;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
//
//public class LifeInfusion extends Spell implements ArtifactSpell {
//    private static final int HEAL_AMOUNT = 20;
//    private static final double CHANCE = 1.0;
//    private static final String ARTIFACT_ID = "crimson-maul";
//
//    public LifeInfusion() {
//        super("Life Infusion", CharacterClass.CLERIC);
//        this.setIsPassive(true);
//    }
//
//    @Override
//    public String getArtifactId() {
//        return ARTIFACT_ID;
//    }
//
//    @Override
//    public double getChance() {
//        return CHANCE;
//    }
//
//    @EventHandler(priority = EventPriority.LOWEST) // first
//    public void onArtifactUse(RunicItemArtifactTriggerEvent event) {
//        if (!event.getRunicItemArtifact().getTemplateId().equals(getArtifactId())) return;
//        if (!(event instanceof RunicArtifactOnKillEvent)) return;
//        RunicArtifactOnKillEvent onKillEvent = (RunicArtifactOnKillEvent) event;
//        if (onKillEvent.getVictim() == null) return;
//        if (!(onKillEvent.getVictim() instanceof LivingEntity)) return;
//        onKillEvent.getVictim().getWorld().spawnParticle
//                (
//                        Particle.REDSTONE,
//                        onKillEvent.getVictim().getLocation(),
//                        5,
//                        0.25f,
//                        0.25f,
//                        0.25f,
//                        0,
//                        new Particle.DustOptions(Color.RED, 3)
//                );
//        healPlayer(event.getPlayer(), event.getPlayer(), HEAL_AMOUNT);
//    }
//}
//
