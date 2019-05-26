package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.*;
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
public class CripplingShot extends Spell {

    private static final int DAMAGE = 3;
    private static final int DURATION = 8;
    private static final double PERCENT = 50;
    private List<Arrow> cripplingArrs = new ArrayList<>();
    private List<Entity> crippledPlrs = new ArrayList<>();

    // constructor
    public CripplingShot() {
        super("Crippling Shot", "You launch an enchanted arrow which" +
                "\ndeals " + DAMAGE + " damage to its target and reduces" +
                "\nall ✦spell healing on the target by " + (int) PERCENT + "%" +
                "\nfor " + DURATION + "seconds.", ChatColor.WHITE, 1, 1);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1);
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
                        50, 1f, 1f, 1f, new Particle.DustOptions(Color.YELLOW, 20));
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

        // ignore NPCs
        if (le.hasMetadata("NPC")) {
            return;
        }

        // skip party members
        if (RunicCore.getPartyManager().getPlayerParty(pl) != null
                && RunicCore.getPartyManager().getPlayerParty(pl).hasMember(le.getUniqueId())) { return; }

        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_SLIME_BLOCK_BREAK, 0.5f, 0.5f);
    }

    @EventHandler
    public void onCrippledHeal(SpellHealEvent e) {

        if (crippledPlrs.contains(e.getEntity())) {

        }
    }
}
