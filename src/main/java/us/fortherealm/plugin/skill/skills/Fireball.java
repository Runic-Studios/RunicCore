package us.fortherealm.plugin.skill.skills;

import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.parties.Party;
import us.fortherealm.plugin.skill.skills.formats.HorizCircleFrame;
import us.fortherealm.plugin.skill.skills.formats.VertCircleFrame;
import us.fortherealm.plugin.skill.skilltypes.Skill;
import us.fortherealm.plugin.skill.skilltypes.SkillItemType;
import us.fortherealm.plugin.skill.skilltypes.skillutil.KnockbackUtil;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

// TODO: if hit enemy is a player, check if enemy player is in OUTLAW MODE for damage check
public class Fireball extends Skill{

    public Fireball() {
        super ("Fireball", "Shoots a fireball.", ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY, 4);
    }

    @Override
    public void onRightClick(Player player, SkillItemType type) {
        SmallFireball fireball = player.launchProjectile(SmallFireball.class);
        fireball.setIsIncendiary(false);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(2);
        fireball.setVelocity(velocity);
        fireball.setShooter(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1);

        new BukkitRunnable() {
            public void run() {
                new VertCircleFrame(0.5F).playParticle(Particle.SMOKE_NORMAL, fireball.getLocation());
                if (fireball.isOnGround() || fireball.isDead()) {
                    this.cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }

    @EventHandler
    public void onFireballDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof SmallFireball) {
            SmallFireball smfb = (SmallFireball) event.getDamager();
            if (smfb.getShooter() instanceof Player) {
                Player player = (Player) smfb.getShooter();
                LivingEntity victim = (LivingEntity) event.getEntity();
                Party party = Main.getPartyManager().getPlayerParty(player);
                event.setCancelled(true);
                if (party != null && party.getMembers().contains(victim.getUniqueId())) {
                    return;
                }
                victim.damage(20, player);
                victim.setLastDamageCause(event);
                KnockbackUtil.knockback(player, victim);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
                victim.getWorld().spigot().playEffect(victim.getEyeLocation(),
                        Effect.FLAME, 0, 0, 0.3F, 0.3F, 0.3F, 0.01F, 50, 16);
            }
        }
    }

}
