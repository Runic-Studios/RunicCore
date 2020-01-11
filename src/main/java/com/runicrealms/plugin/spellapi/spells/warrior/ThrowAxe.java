package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.professions.utilities.FloatingItemUtil;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

@SuppressWarnings("FieldCanBeLocal")
public class ThrowAxe extends Spell {

    private static final int DAMAGE = 10;
    private static final int DURATION = 3;
    private HashMap<UUID, UUID> hasBeenHit;
    private List<UUID> silenced;

    public ThrowAxe() {
        super("Throw Axe",
                "You throw your artifact, dealing" +
                        "\n" + DAMAGE + " spellʔ damage to the first enemy" +
                        "\nhit and silencing it, preventing it" +
                        "\nfrom dealing damage for " + DURATION + " seconds!",
                ChatColor.WHITE,10, 10);
        hasBeenHit = new HashMap<>();
        silenced = new ArrayList<>();
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        ItemStack artifact = pl.getInventory().getItemInMainHand();
        Material artifactType = artifact.getType();
        int durability = ((Damageable) Objects.requireNonNull(artifact.getItemMeta())).getDamage();

        Vector path = pl.getEyeLocation().getDirection().normalize().multiply(1.5);

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_SHULKER_SHOOT, 0.5f, 1.0f);
        Entity projectile = FloatingItemUtil.spawnFloatingItem(pl.getEyeLocation(), artifactType, 50, path, durability);

        new BukkitRunnable() {
            @Override
            public void run() {

                if (projectile.isOnGround() || projectile.isDead()) {
                    if (projectile.isOnGround()) {
                        projectile.remove();
                    }
                    this.cancel();
                    return;
                }

                Location loc = projectile.getLocation();
                projectile.getWorld().spawnParticle(Particle.CRIT, projectile.getLocation(), 1, 0, 0, 0, 0);

                for (Entity en : projectile.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
                    if (verifyEnemy(pl, en)) {
                        if (hasBeenHit.get(pl.getUniqueId()) == en.getUniqueId()) continue;
                        hasBeenHit.put(pl.getUniqueId(), en.getUniqueId()); // prevent concussive hits
                        silenced.add(en.getUniqueId());
                        en.getWorld().playSound(en.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.2f);
                        en.getWorld().spawnParticle
                                (Particle.VILLAGER_ANGRY, en.getLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                        DamageUtil.damageEntitySpell(DAMAGE, (LivingEntity) en, pl, false);
                        projectile.remove();
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);

        new BukkitRunnable() {
            @Override
            public void run() {
                hasBeenHit.clear();
                silenced.clear();
            }
        }.runTaskLaterAsynchronously(RunicCore.getInstance(), DURATION*20L);
    }

    /**
     * Debuff our enemy
     */
    @EventHandler
    public void onMobDamage(MobDamageEvent e) {
        if (silenced.contains(e.getDamager().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        if (silenced.contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {
        if (silenced.contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }
}

