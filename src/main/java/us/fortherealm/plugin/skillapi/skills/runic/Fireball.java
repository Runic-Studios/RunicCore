package us.fortherealm.plugin.skillapi.skills.runic;

import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skillapi.skilltypes.Skill;
import us.fortherealm.plugin.skillapi.skilltypes.SkillItemType;
import us.fortherealm.plugin.skillapi.skilltypes.skillutil.KnockbackUtil;

@SuppressWarnings("FieldCanBeLocal")
public class Fireball extends Skill {

    // globals
    private static final double FIREBALL_SPEED = 2;
    private static final int DAMAGE_AMOUNT = 20;
    private SmallFireball fireball;

    // constructor
    public Fireball() {
        super ("Fireball",
                "You launch a projectile fireball" +
                        "\nwhich deals " + DAMAGE_AMOUNT + " damage on impact!",
                ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY, 1, 5);
    }

    // skill execute code
    @Override
    public void onRightClick(Player player, SkillItemType type) {
        fireball = player.launchProjectile(SmallFireball.class);
        fireball.setIsIncendiary(false);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(FIREBALL_SPEED);
        fireball.setVelocity(velocity);
        fireball.setShooter(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1);
    }

    @EventHandler
    public void onFireballDamage(EntityDamageByEntityEvent event) {

        // only listen for our fireball
        if (!(event.getDamager().equals(this.fireball))) {
            return;
        }

        // grab our variables
        Player player = (Player) fireball.getShooter();
        LivingEntity victim = (LivingEntity) event.getEntity();

        // skip party members
        if (Main.getPartyManager().getPlayerParty(player) != null
                && Main.getPartyManager().getPlayerParty(player).hasMember(victim.getUniqueId())) { return; }

        // cancel the event, apply skill mechanics
        event.setCancelled(true);
        victim.damage(DAMAGE_AMOUNT, player);
        victim.setLastDamageCause(event);
        KnockbackUtil.knockback(player, victim);

        // particles, sounds
        victim.getWorld().spawnParticle(Particle.FLAME, victim.getLocation(), 5, 0.5F, 0.5F, 0.5F);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
    }
}

