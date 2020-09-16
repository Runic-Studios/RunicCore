package com.runicrealms.plugin.spellapi.spells.runic.active;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.player.outlaw.OutlawManager;
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
public class Frostbolt extends Spell {

    private static final double SPEED = 2;
    private static final int DAMAGE_AMT = 15;
    private Snowball snowball;

    public Frostbolt() {
        super("Frostbolt",
                "You launch a projectile bolt of ice" +
                        "\nthat deals " + DAMAGE_AMT + " spellʔ damage on" +
                        "\nimpact and slows its target!",
                ChatColor.WHITE, ClassEnum.RUNIC, 5, 20);
    }

    // spell execute code
    @Override
    public void executeSpell(Player player, SpellItemType type) {
        snowball = player.launchProjectile(Snowball.class);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(SPEED);
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

        // ignore NPCs
        if (victim.hasMetadata("NPC")) return;

        // outlaw check
        if (victim instanceof Player && (!OutlawManager.isOutlaw(((Player) victim)) || !OutlawManager.isOutlaw(pl))) {
            return;
        }

        // skip party members
        if (RunicCore.getPartyManager().getPlayerParty(pl) != null) {
            if (victim instanceof Player) {
                if (RunicCore.getPartyManager().getPlayerParty(pl).hasMember((Player) victim)) {
                    return;
                }
            }
        }

        // cancel the event, apply spell mechanics
        DamageUtil.damageEntitySpell(DAMAGE_AMT, victim, pl, 100);

        // slow
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2));

        // particles, sounds
        victim.getWorld().spawnParticle(Particle.BLOCK_DUST, victim.getEyeLocation(),
                5, 0.5F, 0.5F, 0.5F, 0, Material.PACKED_ICE.createBlockData());
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 1);
    }
}

