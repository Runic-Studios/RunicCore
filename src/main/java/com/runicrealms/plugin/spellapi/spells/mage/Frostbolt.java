package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class Frostbolt extends Spell implements MagicDamageSpell {

    private static final int DAMAGE_AMT = 25;
    private static final double DAMAGE_PER_LEVEL = 2.75;
    private static final double SPEED_MULT = 2.5;
    private Snowball snowball;

    public Frostbolt() {
        super("Frostbolt",
                "You launch a projectile bolt of ice " +
                        "that deals (" + DAMAGE_AMT + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage on " +
                        "impact and slows its target!",
                ChatColor.WHITE, ClassEnum.MAGE, 5, 20);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        snowball = player.launchProjectile(Snowball.class);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(SPEED_MULT);
        snowball.setVelocity(velocity);
        snowball.setShooter(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1);
        EntityTrail.entityTrail(snowball, Particle.SNOWBALL);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onsnowballDamage(EntityDamageByEntityEvent event) {

        // only listen for our snowball
        if (!(event.getDamager().equals(this.snowball))) return;

        event.setCancelled(true);

        // grab our variables
        Player pl = (Player) snowball.getShooter();
        if (pl == null) return;

        LivingEntity victim = (LivingEntity) event.getEntity();
        if (!isValidEnemy(pl, victim)) return;

        // cancel the event, apply spell mechanics
        DamageUtil.damageEntitySpell(DAMAGE_AMT, victim, pl, this);

        // slow
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2));

        // particles, sounds
        victim.getWorld().spawnParticle(Particle.BLOCK_DUST, victim.getEyeLocation(),
                5, 0.5F, 0.5F, 0.5F, 0, Material.PACKED_ICE.createBlockData());
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 1);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}

