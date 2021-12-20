//package com.runicrealms.plugin.spellapi.spells.artifact;
//
//import com.runicrealms.plugin.RunicCore;
//import com.runicrealms.plugin.classes.ClassEnum;
//import com.runicrealms.plugin.spellapi.spelltypes.ArtifactSpell;
//import com.runicrealms.plugin.spellapi.spelltypes.Spell;
//import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
//import com.runicrealms.runicitems.item.event.RunicItemArtifactTriggerEvent;
//import org.bukkit.ChatColor;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
//import org.bukkit.scheduler.BukkitRunnable;
//
//import java.util.concurrent.ThreadLocalRandom;
//
//public class Bloodlust extends Spell implements ArtifactSpell {
//
//    private static final double CHANCE = 0.35;
//    private static final double DURATION = 4;
//    private static final double HEAL_AMOUNT = 20;
//    private static final String ARTIFACT_ID = "corruption";
//
//    public Bloodlust() {
//        super("Bloodlust", "", ChatColor.WHITE, ClassEnum.WARRIOR, 0, 0);
//        this.setIsPassive(true);
//    }
//
//    @EventHandler(priority = EventPriority.LOWEST) // first
//    public void onArtifactUse(RunicItemArtifactTriggerEvent e) {
//        if (!e.getRunicItemArtifact().getTemplateId().equals(getArtifactId())) return;
//        double roll = ThreadLocalRandom.current().nextDouble();
//        if (roll > getChance()) return;
//        healOverTime(e.getPlayer());
//    }
//
//    private void healOverTime(Player player) {
//        double healAmount = HEAL_AMOUNT / DURATION;
//        new BukkitRunnable() {
//            int count = 1;
//
//            @Override
//            public void run() {
//                if (count > 4) this.cancel();
//                count++;
//                HealUtil.healPlayer((int) healAmount, player, player, false);
//            }
//        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
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
//}
//
