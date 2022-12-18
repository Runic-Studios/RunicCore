package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
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
public class ThrowAxe extends Spell implements PhysicalDamageSpell {

    private static final int DAMAGE = 20;
    private static final double DAMAGE_PER_LEVEL = 1.5;
    private static final int DURATION = 3;
    private final HashMap<UUID, UUID> hasBeenHit;
    private final boolean canHitAllies;

    public ThrowAxe() {
        super("Throw Axe",
                "You throw your weapon, dealing (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL +
                        "x&7 lvl) physical⚔ damage to the first enemy " +
                        "hit and silencing it, preventing it " +
                        "from dealing damage for " + DURATION + "s!",
                ChatColor.WHITE, CharacterClass.WARRIOR, 10, 20);
        hasBeenHit = new HashMap<>();
        this.canHitAllies = false;
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        ItemStack artifact = player.getInventory().getItemInMainHand();
        Material artifactType = artifact.getType();
        int durability = ((Damageable) Objects.requireNonNull(artifact.getItemMeta())).getDamage();

        Vector path = player.getEyeLocation().getDirection().normalize().multiply(1.5);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHULKER_SHOOT, 0.5f, 1.0f);
        Entity projectile = FloatingItemUtil.spawnFloatingItem(player.getEyeLocation(), artifactType, 50, path, durability);

        Spell spell = this;
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
                        if (entity.equals(player)) continue;
                        if (isValidAlly(player, entity)) {
                            if (entity instanceof Player && RunicCore.getPartyAPI().getParty(player.getUniqueId()).hasMember((Player) entity)) { // normal ally check allows for non-party spells, so this prevents axe trolling
                                hasBeenHit.put(player.getUniqueId(), entity.getUniqueId()); // prevent concussive hits
                                entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.2f);
                                entity.getWorld().spawnParticle
                                        (Particle.SPELL_INSTANT, entity.getLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                                entity.teleport(player);
                                entity.getWorld().spawnParticle
                                        (Particle.SPELL_INSTANT, entity.getLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                                projectile.remove();
                                return;
                            }
                        }
                    }
                    if (isValidEnemy(player, entity)) {
                        if (hasBeenHit.get(player.getUniqueId()) == entity.getUniqueId()) continue;
                        hasBeenHit.put(player.getUniqueId(), entity.getUniqueId()); // prevent concussive hits
                        addStatusEffect(entity, RunicStatusEffect.SILENCE, DURATION);
                        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.2f);
                        entity.getWorld().spawnParticle
                                (Particle.VILLAGER_ANGRY, entity.getLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                        DamageUtil.damageEntityPhysical(DAMAGE, (LivingEntity) entity, player, false, false, spell);
                        projectile.remove();
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);

        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), hasBeenHit::clear, DURATION * 20L);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}

