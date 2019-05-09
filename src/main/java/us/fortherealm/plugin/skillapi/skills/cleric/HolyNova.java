package us.fortherealm.plugin.skillapi.skills.cleric;

import org.bukkit.*;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.FTRCore;
import us.fortherealm.plugin.skillapi.skilltypes.Skill;
import us.fortherealm.plugin.skillapi.skilltypes.SkillItemType;
import us.fortherealm.plugin.utilities.DamageUtil;

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
        nova.runTaskTimer(FTRCore.getInstance(), 0, 20);

        // cancel effect
        new BukkitRunnable() {
            @Override
            public void run() {
                nova.cancel();
            }
        }.runTaskLater(FTRCore.getInstance(), DURATION*20);
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

            // skip NPCs
            if (entity.hasMetadata("NPC")) { continue; }

            // skip the caster
            if(entity.equals(pl)) { continue; }

            // skip party members
            if (FTRCore.getPartyManager().getPlayerParty(pl) != null
                    && FTRCore.getPartyManager().getPlayerParty(pl).hasMember(entity.getUniqueId())) { continue; }

            // Executes the skill
            if (entity.getType().isAlive()) {
                LivingEntity victim = (LivingEntity) entity;
                DamageUtil.damageEntityMagic(DAMAGE_AMT, victim, pl);
            }
        }
    }
}
