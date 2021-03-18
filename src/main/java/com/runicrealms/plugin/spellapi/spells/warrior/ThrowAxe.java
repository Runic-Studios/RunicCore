package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.plugin.utilities.FloatingItemUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class ThrowAxe extends Spell {

    private static final int DAMAGE = 20;
    private static final int DURATION = 3;
    private final HashMap<UUID, UUID> hasBeenHit;
    private final boolean canHitAllies;

    public ThrowAxe() {
        super("Throw Axe",
                "You throw your weapon, dealing " +
                        DAMAGE + " weapon⚔ damage to the first enemy " +
                        "hit and silencing it, preventing it " +
                        "from dealing damage for " + DURATION + "s!",
                ChatColor.WHITE, ClassEnum.WARRIOR, 10, 20);
        hasBeenHit = new HashMap<>();
        this.canHitAllies = false;
    }

    public ThrowAxe(boolean canHitAllies) {
        super("Throw Axe",
                "You throw your weapon, dealing " +
                        DAMAGE + " weapon⚔ damage to the first enemy " +
                        "hit and silencing it, preventing it " +
                        "from dealing damage for " + DURATION + "s!",
                ChatColor.WHITE, ClassEnum.WARRIOR, 10, 20);
        hasBeenHit = new HashMap<>();
        this.canHitAllies = canHitAllies;
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
                                return;
                            }
                        }
                    }
                    if (verifyEnemy(pl, entity)) {
                        if (hasBeenHit.get(pl.getUniqueId()) == entity.getUniqueId()) continue;
                        hasBeenHit.put(pl.getUniqueId(), entity.getUniqueId()); // prevent concussive hits
                        addStatusEffect(entity, EffectEnum.SILENCE, DURATION);
                        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.2f);
                        entity.getWorld().spawnParticle
                                (Particle.VILLAGER_ANGRY, entity.getLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                        DamageUtil.damageEntityWeapon(DAMAGE, (LivingEntity) entity, pl, false, false, true);
                        projectile.remove();
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);

        Bukkit.getScheduler().scheduleAsyncDelayedTask(RunicCore.getInstance(), hasBeenHit::clear, DURATION * 20L);
    }
}

