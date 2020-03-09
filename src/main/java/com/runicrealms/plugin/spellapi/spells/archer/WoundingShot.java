package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class WoundingShot extends Spell {

    private static final int DAMAGE = 3;
    private static final int DURATION = 5;
    private static final double PERCENT = 50;
    private List<Arrow> cripplingArrs = new ArrayList<>();
    private List<UUID> crippledPlrs = new ArrayList<>();

    public WoundingShot() {
        super("Wounding Shot",
                "You launch an enchanted arrow which" +
                "\ndeals " + DAMAGE + " spellʔ damage to its target and" +
                "\nreduces all ✦spell healing on the target" +
                "\nby " + (int) PERCENT + "%" + " for " + DURATION + " seconds!",
                ChatColor.WHITE, ClassEnum.ARCHER, 15, 20);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1f);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_LAVA_AMBIENT, 2f, 2f);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 0.5f, 2f);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_LAVA_POP, 2f, 2f);

        Arrow crippler = pl.launchProjectile(Arrow.class);
        Vector vec = pl.getEyeLocation().getDirection().normalize().multiply(2);
        crippler.setVelocity(vec);
        crippler.setShooter(pl);
        cripplingArrs.add(crippler);
        new BukkitRunnable() {
            @Override
            public void run() {
                Location arrowLoc = crippler.getLocation();
                arrowLoc.getWorld().spawnParticle(Particle.REDSTONE, arrowLoc,
                        10, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 2));
                if (crippler.isDead() || crippler.isOnGround()) {
                    this.cancel();
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
    }

    @EventHandler
    public void onPoisArrowHit(EntityDamageByEntityEvent e) {

        // only listen for arrows
        if (!(e.getDamager() instanceof Arrow)) {
            return;
        }

        // listen for player fired arrow
        Arrow arrow = (Arrow) e.getDamager();
        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }

        // deal magic damage if arrow in in the barrage hashmap
        if (!cripplingArrs.contains(arrow)) return;

        e.setCancelled(true);

        if (!(e.getEntity() instanceof LivingEntity)) return;
        Player pl = (Player) ((Arrow) e.getDamager()).getShooter();
        assert pl != null;
        LivingEntity le = (LivingEntity) e.getEntity();

        if (!verifyEnemy(pl, le)) return;

        // spell effect
        crippledPlrs.add(le.getUniqueId());
        DamageUtil.damageEntitySpell(DAMAGE, le, pl, false);

        // particles, sounds
        le.sendMessage(ChatColor.RED + "You have been " + ChatColor.DARK_RED + ChatColor.BOLD + "wounded! "
                + ChatColor.RED + "Healing spells are " + (int) PERCENT + "% effective on you for " + DURATION
                + " second(s)!");

        Cone.coneEffect(le, Particle.REDSTONE, DURATION, 0, 20L, Color.RED);
        le.getWorld().playSound(le.getLocation(), Sound.BLOCK_SLIME_BLOCK_BREAK, 0.5f, 0.5f);
        le.getWorld().playSound(le.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 0.5f);

        new BukkitRunnable() {
            @Override
            public void run() {
                crippledPlrs.remove(le.getUniqueId());
            }
        }.runTaskLaterAsynchronously(RunicCore.getInstance(), DURATION*20L);
    }

    @EventHandler
    public void onCrippledHeal(SpellHealEvent e) {

        if (crippledPlrs.contains(e.getEntity().getUniqueId())) {
            double percent = PERCENT/100;
            int newAmt = (int) (e.getAmount()*percent);
            e.setAmount(newAmt);
        }
    }
}
