package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.outlaw.OutlawManager;
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

@SuppressWarnings("FieldCanBeLocal")
public class SearingShot extends Spell {

    private static final int DAMAGE = 35;
    private List<Arrow> searingArrows;

    // constructor
    public SearingShot() {
        super("Searing Shot", "You launch an enchanted, flaming arrow" +
                "\nwhich deals " + DAMAGE + " spellʔ damage on-hit!", ChatColor.WHITE, 8, 12);
        searingArrows = new ArrayList<>();
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1f);
        pl.getWorld().playSound(pl.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.5f, 2f);

        Arrow searing = pl.launchProjectile(Arrow.class);
        Vector vec = pl.getEyeLocation().getDirection().normalize().multiply(2);
        searing.setVelocity(vec);
        searing.setShooter(pl);
        searingArrows.add(searing);
        new BukkitRunnable() {
            @Override
            public void run() {
                Location arrowLoc = searing.getLocation();
                arrowLoc.getWorld().spawnParticle(Particle.FLAME, arrowLoc,
                        10, 0, 0, 0, 0);
                if (searing.isDead() || searing.isOnGround()) {
                    this.cancel();
                    pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_LAVA_POP, 0.5f, 2f);
                    pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 2f);
                    arrowLoc.getWorld().spawnParticle(Particle.LAVA, arrowLoc, 10, 0, 0, 0, 0);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
    }

    @EventHandler
    public void onSearingArrowHit(EntityDamageByEntityEvent e) {

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
        if (!searingArrows.contains(arrow)) return;

        e.setCancelled(true);

        if (!(e.getEntity() instanceof LivingEntity)) return;
        Player pl = (Player) ((Arrow) e.getDamager()).getShooter();
        assert pl != null;
        LivingEntity le = (LivingEntity) e.getEntity();

        // ignore NPCs
        if (le.hasMetadata("NPC")) {
            return;
        }

        // outlaw check
        if (le instanceof Player && (!OutlawManager.isOutlaw(((Player) le)) || !OutlawManager.isOutlaw(pl))) {
            return;
        }

        // skip party members
        if (RunicCore.getPartyManager().getPlayerParty(pl) != null
                && RunicCore.getPartyManager().getPlayerParty(pl).hasMember(le.getUniqueId())) { return; }

        // spell effect
        DamageUtil.damageEntitySpell(DAMAGE, le, pl, false);
    }
}
