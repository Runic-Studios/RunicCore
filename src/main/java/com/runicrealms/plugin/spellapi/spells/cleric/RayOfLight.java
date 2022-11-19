package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class RayOfLight extends Spell implements MagicDamageSpell {

    private static final int DAMAGE_AMOUNT = 25;
    private static final double DAMAGE_PER_LEVEL = 4.0;
    private static final int DURATION = 5;
    private static final int MAX_DIST = 10;
    private static final int RADIUS = 4;
    private static final int STUN_DURATION = 2;
    private final HashSet<UUID> hitEntities;

    public RayOfLight() {
        super("Ray of Light",
                "You summon an orb of holy magic at " +
                        "your target location that persists for " + DURATION +
                        "s and deals (" + DAMAGE_AMOUNT + " + &f" +
                        DAMAGE_PER_LEVEL + "x&7 lvl) spellʔ " +
                        "damage to enemies within " + RADIUS + " blocks, " +
                        "stunning them for " + STUN_DURATION + "s! " +
                        "Enemies cannot be hit more than once.",
                ChatColor.WHITE, ClassEnum.CLERIC, 20, 15);
        hitEntities = new HashSet<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        Spell spell = this;

        Location orbLocation = player.getTargetBlock(null, MAX_DIST).getLocation();
        while (orbLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
            orbLocation = orbLocation.getBlock().getRelative(BlockFace.DOWN).getLocation(); // ensure location on ground
        orbLocation.add(0, 2, 0); // raise orb up

        player.getWorld().playSound(orbLocation, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 0.1f);
        player.getWorld().spawnParticle(Particle.SPELL_INSTANT, orbLocation, 25, 0.3f, 0.3f, 0.3f, 0);

        Location finalOrbLocation = orbLocation;
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION)
                    this.cancel();
                else {
                    count += 1;
                    Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> spawnSphere(finalOrbLocation));
                    for (Entity en : player.getWorld().getNearbyEntities(finalOrbLocation, RADIUS, RADIUS, RADIUS)) {
                        if (!isValidEnemy(player, en)) continue;
                        if (hitEntities.contains(en.getUniqueId())) continue;
                        hitEntities.add(en.getUniqueId());
                        LivingEntity le = (LivingEntity) en;
                        le.getWorld().playSound(le.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 0.5f);
                        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> VectorUtil.drawLine(player, Particle.SPELL_INSTANT, Color.WHITE, finalOrbLocation, le.getEyeLocation(), 1.0));
                        DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, le, player, spell);
                        addStatusEffect(en, EffectEnum.STUN, STUN_DURATION);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20L);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, hitEntities::clear, DURATION * 20L);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    private void spawnSphere(Location loc) {
        for (double i = 0; i <= Math.PI; i += Math.PI / 12) {
            double radius = Math.sin(i);
            double y = Math.cos(i);
            for (double a = 0; a < Math.PI * 2; a += Math.PI / 12) {
                double x = .9 * Math.cos(a) * radius;
                double z = .9 * Math.sin(a) * radius;
                loc.add(x, y, z);
                Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.WHITE, 1));
                loc.subtract(x, y, z);
            }
        }
    }
}

