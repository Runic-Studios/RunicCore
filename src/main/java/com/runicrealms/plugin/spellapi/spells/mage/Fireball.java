package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.outlaw.OutlawManager;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.KnockbackUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;
import com.runicrealms.plugin.RunicCore;

@SuppressWarnings("FieldCanBeLocal")
public class Fireball extends Spell {

    // globals
    private static final double FIREBALL_SPEED = 2;
    private static final int DAMAGE_AMOUNT = 20;
    private SmallFireball fireball;

    // constructor
    public Fireball() {
        super ("Fireball",
                "You launch a projectile fireball" +
                        "\nwhich deals " + DAMAGE_AMOUNT + " spellʔ damage on" +
                        "\nimpact and knocks your enemy" +
                        "\nback!",
                ChatColor.WHITE, 5, 10);
    }

    // spell execute code
    @Override
    public void executeSpell(Player player, SpellItemType type) {
        fireball = player.launchProjectile(SmallFireball.class);
        fireball.setIsIncendiary(false);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(FIREBALL_SPEED);
        fireball.setVelocity(velocity);
        fireball.setShooter(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFireballDamage(EntityDamageByEntityEvent event) {

        // only listen for our fireball
        if (!(event.getDamager().equals(this.fireball))) return;

        event.setCancelled(true);

        // grab our variables
        Player player = (Player) fireball.getShooter();
        if (player == null) return;
        LivingEntity victim = (LivingEntity) event.getEntity();

        // skip NPCs
        if (victim.hasMetadata("NPC")) return;

        // outlaw check
        if (victim instanceof Player && (!OutlawManager.isOutlaw(((Player) victim)) || !OutlawManager.isOutlaw(player))) {
            return;
        }

        // skip party members
        if (RunicCore.getPartyManager().getPlayerParty(player) != null
                && RunicCore.getPartyManager().getPlayerParty(player).hasMember(victim.getUniqueId())) { return; }

        // cancel the event, apply spell mechanics
        DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, victim, player, false);
        KnockbackUtil.knockbackPlayer(player, victim, 1.5);

        // particles, sounds
        victim.getWorld().spawnParticle(Particle.FLAME, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
    }
}

