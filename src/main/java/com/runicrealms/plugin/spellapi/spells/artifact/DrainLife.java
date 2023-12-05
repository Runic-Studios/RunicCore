//package com.runicrealms.plugin.spellapi.spells.artifact;
//
//import com.runicrealms.plugin.RunicCore;
//import com.runicrealms.plugin.item.artifact.event.RunicItemArtifactTriggerEvent;
//import com.runicrealms.plugin.common.CharacterClass;
//import com.runicrealms.plugin.spellapi.spelltypes.ArtifactSpell;
//import com.runicrealms.plugin.spellapi.spelltypes.Spell;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
//import org.bukkit.scheduler.BukkitRunnable;
//
//import java.util.concurrent.ThreadLocalRandom;
//
//public class DrainLife extends Spell implements ArtifactSpell {
//    private static final double CHANCE = 0.35;
//    private static final double DURATION = 4;
//    private static final double HEAL_AMOUNT = 20;
//    private static final String ARTIFACT_ID = "bloodmoon";
//
//    public DrainLife() {
//        super("Drain Life", CharacterClass.WARRIOR);
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
//    private void healOverTime(Player player) {
//        double healAmount = HEAL_AMOUNT / DURATION;
//        new BukkitRunnable() {
//            int count = 1;
//
//            @Override
//            public void run() {
//                if (count > DURATION) {
//                    this.cancel();
//                } else {
//                    count++;
//                    healPlayer(player, player, (int) healAmount);
//                }
//            }
//        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
//    }
//
//    @EventHandler(priority = EventPriority.LOWEST) // first
//    public void onArtifactUse(RunicItemArtifactTriggerEvent e) {
//        if (!e.getRunicItemArtifact().getTemplateId().equals(getArtifactId())) return;
//        double roll = ThreadLocalRandom.current().nextDouble();
//        if (roll > getChance()) return;
//        healOverTime(e.getPlayer());
//    }
//}
//
