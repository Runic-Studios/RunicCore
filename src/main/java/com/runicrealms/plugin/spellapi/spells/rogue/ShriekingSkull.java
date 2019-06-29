package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.outlaw.OutlawManager;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.KnockbackUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import com.runicrealms.plugin.RunicCore;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class ShriekingSkull extends Spell {

    // globals
    private static final int POTION_DURATION = 5;
    private static final double LAUNCH_MULT = 1.5;
    private static final double SKULL_SPEED = 0.8;
    private WitherSkull skull;
    private List<WitherSkull> hit = new ArrayList<>();

    // constructor
    public ShriekingSkull() {
        super ("Shrieking Skull",
                "You launch a projectile skull of shadow," +
                        "\nlaunching the first enemy hit into the air and" +
                        "\nforcing them to fall slowly to the ground!",
                ChatColor.WHITE, 5, 15);
    }

    // spell execute code
    @Override
    public void executeSpell(Player player, SpellItemType type) {

        skull = player.launchProjectile(WitherSkull.class);
        skull.setCharged(false);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(SKULL_SPEED);
        skull.setVelocity(velocity);
        skull.setShooter(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.5f, 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSkullDamage(EntityDamageByEntityEvent event) {

        // only listen for our fireball
        if (!(event.getDamager().equals(this.skull))
                || event.getDamager() instanceof WitherSkull
                && hit.contains(event.getDamager())) return;

        // skip the caster
        if (event.getEntity() == skull.getShooter()) return;

        event.setCancelled(true);

        // prevent multiple enemies from being hit
        event.getDamager().remove();
        hit.add((WitherSkull) event.getDamager());

        // grab our variables
        Player player = (Player) skull.getShooter();
        LivingEntity victim = (LivingEntity) event.getEntity();
        if (player == null) return;

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
        Vector launch = new Vector(0, 10.0f, 0).normalize().multiply(LAUNCH_MULT);
        victim.setVelocity(launch);
        victim.addPotionEffect
                (new PotionEffect(PotionEffectType.SLOW_FALLING, POTION_DURATION * 20, 0));

        // particles, sounds
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.0f);
        victim.getWorld().spawnParticle(Particle.REDSTONE, victim.getLocation(),
                15, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.BLACK, 2));
    }

    /**
     * Prevent block damage, AoE
     */
    @EventHandler
    public void onBlockDamage(ExplosionPrimeEvent e) {
        if (e.getEntity() == this.skull) {
            e.setCancelled(true);
        }
    }
}

