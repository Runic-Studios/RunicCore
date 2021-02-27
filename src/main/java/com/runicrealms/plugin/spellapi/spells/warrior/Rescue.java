package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import com.runicrealms.plugin.utilities.FloatingItemUtil;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Rescue extends Spell {

    private static final int DURATION = 3;
    private static final double PERCENT = .25;
    private final HashMap<UUID, UUID> hasBeenHit;
    private final boolean canHitAllies;

    public Rescue() {
        super("Rescue",
                "You throw your weapon, stopping at " +
                        "the first ally hit and pulling them to you! " +
                        "You and your ally both gain a shield■ " +
                        "equal to " + (int) (PERCENT * 100) + "% of your health!",
                ChatColor.WHITE, ClassEnum.WARRIOR, 10, 20);
        hasBeenHit = new HashMap<>();
        this.canHitAllies = false;
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

                for (Entity entity : projectile.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
                    if (canHitAllies) {
                        if (entity.equals(pl)) continue;
                        if (hasBeenHit.containsKey(entity.getUniqueId())) continue;
                        if (verifyAlly(pl, entity)) {
                            if (entity instanceof Player && RunicCore.getPartyManager().getPlayerParty(pl).hasMember((Player) entity)) { // normal ally check allows for non-party spells, so this prevents axe trolling
                                hasBeenHit.put(pl.getUniqueId(), entity.getUniqueId()); // prevent concussive hits
                                entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.2f);
                                entity.getWorld().spawnParticle
                                        (Particle.SPELL_INSTANT, entity.getLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                                entity.teleport(pl);
                                entity.getWorld().spawnParticle
                                        (Particle.SPELL_INSTANT, entity.getLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                                projectile.remove();
                                shieldCasterAndAlly(pl, (Player) entity);
                                return;
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);

        Bukkit.getScheduler().scheduleAsyncDelayedTask(RunicCore.getInstance(), hasBeenHit::clear, DURATION * 20L);
    }

    private void shieldCasterAndAlly(Player caster, Player ally) {
        double amount = caster.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * PERCENT;
        HealUtil.shieldPlayer(amount, caster, caster, true, false, false);
        HealUtil.shieldPlayer(amount, ally, caster, true, false, false);
    }
}

