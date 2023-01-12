package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class RunicArrow extends Spell implements MagicDamageSpell {

    private static final int DAMAGE = 15;
    private static final double DAMAGE_PER_LEVEL = 2.5;
    private static final int RADIUS = 3;
    private final List<Arrow> powerShots;

    public RunicArrow() {
        super("Runic Arrow",
                "You launch an enchanted arrow that " +
                        "deals (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl)" + " magicʔ damage on-hit to " +
                        "all enemies within " + RADIUS + " blocks!",
                ChatColor.WHITE, CharacterClass.ARCHER, 7, 20);
        powerShots = new ArrayList<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1f);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.5f, 2f);
        Vector vec = player.getEyeLocation().getDirection().normalize().multiply(2);
        startTask(player, vec);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler
    public void onSearingArrowHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow)) return;
        Arrow arrow = (Arrow) event.getDamager();
        if (!(arrow.getShooter() instanceof Player)) return;
        if (!powerShots.contains(arrow)) return;
        event.setCancelled(true);
    }

    private void startTask(Player player, Vector vector) {
        Arrow powerShot = player.launchProjectile(Arrow.class);
        powerShot.setVelocity(vector);
        powerShot.setShooter(player);
        powerShots.add(powerShot);
        Spell spell = this;
        EntityTrail.entityTrail(powerShot, Particle.CRIT_MAGIC);
        new BukkitRunnable() {
            @Override
            public void run() {
                Location arrowLoc = powerShot.getLocation();
                if (powerShot.isDead() || powerShot.isOnGround()) {
                    this.cancel();
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 0.5f, 2f);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 2f);
                    powerShot.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, arrowLoc, 10, 0, 0, 0, 0);
                    powerShot.getWorld().spawnParticle(Particle.CRIT_MAGIC, arrowLoc, 25, 0.5f, 0.5f, 0.5f, 0);
                    for (Entity entity : player.getWorld().getNearbyEntities(arrowLoc, RADIUS, RADIUS, RADIUS)) {
                        if (!isValidEnemy(player, entity)) continue;
                        DamageUtil.damageEntitySpell(DAMAGE, (LivingEntity) entity, player, spell);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
    }
}
