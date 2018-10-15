package us.fortherealm.plugin.skills.skilltypes.runic.defensive;

import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.formats.HelixParticleFrame;
import us.fortherealm.plugin.skills.util.HealUtil;

public class Heal extends Skill {

    private static final double HEAL_AMT = 25;

    private HealUtil hu = new HealUtil();

    public Heal() {
        super("Heal", "You restore" + HEAL_AMT + "of your health");
    }

    @Override
    public void executeSkill() {

        // TODO: add colldown check if the player is at full hp
        //if (player.getHealth() == player.getMaxHealth()) {
        //this.doCooldown = false;
        //player.sendMessage(ChatColor.GRAY + "You are currently at full health.");
        //player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
        //} else {
        //this.doCooldown = true;

        // heal the player
        hu.healPlayer(HEAL_AMT, getPlayer(), "");

        // particle effects
        new BukkitRunnable() {
            @Override
            public void run() {
                new HelixParticleFrame(0.5F, 3, 2.5F).playParticle(Particle.HEART, getPlayer().getLocation());
                new HelixParticleFrame(0.5F, 3, 2.5F).playParticle(Particle.FIREWORKS_SPARK, getPlayer().getLocation());
            }
        }.runTaskLater(Main.getInstance(), 1);
    }
}