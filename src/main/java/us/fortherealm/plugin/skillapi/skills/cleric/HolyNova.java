package us.fortherealm.plugin.skillapi.skills.cleric;

import org.bukkit.*;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skillapi.skilltypes.Skill;
import us.fortherealm.plugin.skillapi.skilltypes.SkillItemType;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class HolyNova extends Skill {

    private static final int DAMAGE_AMT = 5;
    private static final int DURATION = 6;
    private static final float RADIUS = 2.5f;

    // constructor
    public HolyNova() {
        super("Holy Nova", "For " + DURATION + " seconds, you pulse with holy power," +
                        "\nconjuring rings of light magic which" +
                        "\ndeal " + DAMAGE_AMT + " damage to enemies!",
                ChatColor.WHITE, 1, 5);
    }

    @Override
    public void executeSkill(Player pl, SkillItemType type) {

        // begin effect
        BukkitRunnable nova = new BukkitRunnable() {
            @Override
            public void run() {
                spawnRing(pl);
            }
        };
        nova.runTaskTimer(Main.getInstance(), 0, 20);

        // cancel effect
        new BukkitRunnable() {
            @Override
            public void run() {
                nova.cancel();
            }
        }.runTaskLater(Main.getInstance(), DURATION*20);
    }

    private void spawnRing(Player pl) {

        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 1.0F);

        Location location1 = pl.getEyeLocation();
        int particles = 50;
        float radius = RADIUS;

        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * radius;
            z = Math.sin(angle) * radius;
            location1.add(x, 0, z);
            location1.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, location1, 1, 0, 0, 0, 0);
            location1.subtract(x, 0, z);
        }

        for (Entity entity : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {

            // only damageable entities
            if (!(entity instanceof Damageable)) { continue; }

            // skip the caster
            if(entity.equals(pl)) { continue; }

            // skip party members
            if (Main.getPartyManager().getPlayerParty(pl) != null
                    && Main.getPartyManager().getPlayerParty(pl).hasMember(entity.getUniqueId())) { continue; }

            // Executes the skill
            ((Damageable) entity).damage(DAMAGE_AMT, pl);
        }
    }
}
