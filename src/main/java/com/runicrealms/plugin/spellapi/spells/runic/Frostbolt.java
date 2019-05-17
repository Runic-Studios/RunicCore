package com.runicrealms.plugin.spellapi.spells.runic;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.KnockbackUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class Frostbolt extends Spell {

    private static final double SPEED = 2;
    private static final int DAMAGE_AMT = 8;
    private Snowball snowball;

    public Frostbolt() {
        super("Frostbolt",
                "You launch a projectile bolt of ice" +
                        "\nthat deals " + DAMAGE_AMT + " damage on impact" +
                        "\nand slows its target!",
                ChatColor.WHITE, 5, 15);
    }

    // spell execute code
    @Override
    public void executeSpell(Player player, SpellItemType type) {
        snowball = player.launchProjectile(Snowball.class);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(SPEED);
        snowball.setVelocity(velocity);
        snowball.setShooter(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1);

        // more particles
        new BukkitRunnable() {
            @Override
            public void run() {
                if (snowball.isDead()) {
                    this.cancel();
                }
                snowball.getWorld().spawnParticle(Particle.SNOWBALL, snowball.getLocation(), 1, 0, 0, 0, 0);
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onsnowballDamage(EntityDamageByEntityEvent event) {

        // only listen for our snowball
        if (!(event.getDamager().equals(this.snowball))) return;

        event.setCancelled(true);

        // grab our variables
        Player pl = (Player) snowball.getShooter();
        LivingEntity victim = (LivingEntity) event.getEntity();

        // ignore NPCs
        if (victim.hasMetadata("NPC")) return;

        // skip party members
        if (RunicCore.getPartyManager().getPlayerParty(pl) != null
                && RunicCore.getPartyManager().getPlayerParty(pl).hasMember(victim.getUniqueId())) { return; }

        // cancel the event, apply spell mechanics
        DamageUtil.damageEntityMagic(DAMAGE_AMT, victim, pl);
        KnockbackUtil.knockback(pl, victim);

        // slow
        if (victim instanceof Player) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2));
        }

        // particles, sounds
        victim.getWorld().spawnParticle(Particle.BLOCK_DUST, victim.getEyeLocation(),
                5, 0.5F, 0.5F, 0.5F, 0, Material.PACKED_ICE.createBlockData());
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 1);
    }
}

