package us.fortherealm.plugin.skills.skilltypes.runic.defensive;

import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.skillutil.HealUtil;
import us.fortherealm.plugin.skills.skillutil.formats.HelixParticleFrame;

public class Heal extends Skill {

    // global variables
    private static final double HEAL_AMT = 25;
    private HealUtil hu = new HealUtil();

    // constructor matching super
    public Heal() {
        super("Heal", "You restore" + HEAL_AMT + "of your health", 5);
    }

    @Override
    public void executeSkill() {

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