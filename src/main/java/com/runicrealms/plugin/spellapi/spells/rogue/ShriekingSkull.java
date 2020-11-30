package com.runicrealms.plugin.spellapi.spells.rogue;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class ShriekingSkull extends Spell {

    private final boolean willSilence;
    private static final int POTION_DURATION = 5;
    private static final double LAUNCH_MULT = 1.5;
    private static final double SKULL_SPEED = 0.8;
    private WitherSkull skull;

    public ShriekingSkull() {
        super ("Shrieking Skull",
                "You launch a projectile skull of shadow," +
                        "\nlaunching the first enemy hit into the air," +
                        "\nblinding them, and forcing them to fall" +
                        "\nslowly to the ground!",
                ChatColor.WHITE, ClassEnum.ROGUE, 8, 15);
        this.willSilence = false;
    }

    public ShriekingSkull(boolean willSilence) {
        super ("Shrieking Skull",
                "You launch a projectile skull of shadow," +
                        "\nlaunching the first enemy hit into the air," +
                        "\nblinding them, and forcing them to fall" +
                        "\nslowly to the ground!",
                ChatColor.WHITE, ClassEnum.ROGUE, 8, 15);
        this.willSilence = willSilence;
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        player.swingMainHand();
        skull = player.launchProjectile(WitherSkull.class);
        skull.setCharged(false);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(SKULL_SPEED);
        skull.setVelocity(velocity);
        skull.setShooter(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.5f, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                skull.getWorld().spawnParticle(Particle.SNOWBALL, skull.getLocation(), 1, 0, 0, 0, 0);
                if (skull.isDead() || skull.isOnGround()) {
                    this.cancel();
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
    }

    @EventHandler
    public void onTridentDamage(ProjectileCollideEvent e) {

        if (!e.getEntity().equals(this.skull)) return;
        e.setCancelled(true);
        e.getEntity().remove();
        e.getEntity().remove();

        // grab our variables
        Player player = (Player) skull.getShooter();
        LivingEntity victim = (LivingEntity) e.getCollidedWith();
        if (player == null) return;

        if (!verifyEnemy(player, victim)) return;

        // cancel the event, apply spell mechanics
        Vector launch = new Vector(0, 10.0f, 0).normalize().multiply(LAUNCH_MULT);
        victim.setVelocity(launch);
        victim.addPotionEffect
                (new PotionEffect(PotionEffectType.BLINDNESS, POTION_DURATION * 20, 0));
        victim.addPotionEffect
                (new PotionEffect(PotionEffectType.SLOW_FALLING, POTION_DURATION * 20, 0));

        // particles, sounds
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.0f);
        victim.getWorld().spawnParticle(Particle.REDSTONE, victim.getLocation(),
                15, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.BLACK, 2));

        if (willSilence)
            addStatusEffect(victim, EffectEnum.SILENCE, POTION_DURATION);
    }
}

