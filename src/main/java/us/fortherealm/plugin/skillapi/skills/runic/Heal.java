package us.fortherealm.plugin.skillapi.skills.runic;

import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.FTRCore;
import us.fortherealm.plugin.skillapi.skilltypes.Skill;
import us.fortherealm.plugin.skillapi.skilltypes.SkillItemType;
import us.fortherealm.plugin.skillapi.skilltypes.skillutil.HealUtil;
import us.fortherealm.plugin.skillapi.skillutil.formats.HelixParticleFrame;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Heal extends Skill {

    // globals
    private static final double HEAL_AMT = 25;
    private HealUtil hu = new HealUtil();

    // constructor
    public Heal() {
        super("Heal",
                "You restore " + (int) HEAL_AMT + " of your own health!",
                ChatColor.WHITE, 1, 10);
    }

    // skill execute code
    @Override
    public void executeSkill(Player pl, SkillItemType type) {

        // heal the player
        hu.healPlayer(HEAL_AMT, pl, pl, "");

        // particle effects
        new BukkitRunnable() {
            @Override
            public void run() {
                new HelixParticleFrame(0.5F, 3, 2.5F).playParticle(Particle.HEART, pl.getLocation());
                new HelixParticleFrame(0.5F, 3, 2.5F).playParticle(Particle.FIREWORKS_SPARK, pl.getLocation());
            }
        }.runTaskLater(FTRCore.getInstance(), 1);
    }
}

