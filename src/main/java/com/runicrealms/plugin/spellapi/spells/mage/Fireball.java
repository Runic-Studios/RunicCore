package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.TeleportUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Fireball extends Spell {

    private final boolean fireCone;
    private final boolean applyBurn;
    private final boolean iceBolt;
    private static final double FIREBALL_SPEED = 2;
    private static final int DAMAGE_AMOUNT = 25;
    private static final int CHILL_DURATION = 8;
    private static final int FREEZE_DURATION = 4;

    private SmallFireball fireball;
    private SmallFireball fireballLeft;
    private SmallFireball fireballRight;
    private Snowball snowball;
    private static Map<UUID, BukkitTask> chilledPlayers;

    public Fireball() {
        super ("Fireball",
                "You launch a projectile fireball" +
                        "\nthat deals " + DAMAGE_AMOUNT + " spellʔ damage on" +
                        "\nimpact!",
                ChatColor.WHITE, ClassEnum.MAGE, 5, 15);
        fireCone = false;
        applyBurn = false;
        iceBolt = false;
    }

    /**
     * Overriden method for tier set bonuses
     * @param fireCone 2-set bonus to apply a cone of projectiles
     * @param applyBurn 4-set bonus to apply burn effect
     */
    public Fireball(boolean fireCone, boolean applyBurn) {
        super ("Fireball",
                "You launch a projectile fireball" +
                        "\nthat deals " + DAMAGE_AMOUNT + " spellʔ damage on" +
                        "\nimpact!",
                ChatColor.WHITE, ClassEnum.MAGE, 5, 15);
        this.fireCone = fireCone;
        this.applyBurn = applyBurn;
        this.iceBolt = false;
    }

    /**
     * Overriden method for tier set bonuses
     * @param iceBolt swap spell
     */
    public Fireball(boolean iceBolt) {
        super ("Fireball",
                "You launch a projectile fireball" +
                        "\nthat deals " + DAMAGE_AMOUNT + " spellʔ damage on" +
                        "\nimpact!",
                ChatColor.WHITE, ClassEnum.MAGE, 5, 15);
        this.fireCone = false;
        this.applyBurn = false;
        this.iceBolt = iceBolt;
        chilledPlayers = new HashMap<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        if (iceBolt) {
            snowball = player.launchProjectile(Snowball.class);
            final Vector velocity = player.getLocation().getDirection().normalize().multiply(FIREBALL_SPEED);
            snowball.setVelocity(velocity);
            snowball.setShooter(player);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1);
            EntityTrail.entityTrail(snowball, Particle.SNOWBALL);
            return;
        }
        fireball = player.launchProjectile(SmallFireball.class);
        fireball.setIsIncendiary(false);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(FIREBALL_SPEED);
        fireball.setVelocity(velocity);
        fireball.setShooter(player);
        EntityTrail.entityTrail(fireball, Particle.FLAME);
        if (fireCone) {
            Vector left = rotateVectorAroundY(velocity, -22.5);
            Vector right = rotateVectorAroundY(velocity, 22.5);
            fireballLeft = player.launchProjectile(SmallFireball.class);
            fireballLeft.setIsIncendiary(false);
            fireballLeft.setVelocity(left);
            fireballLeft.setShooter(player);
            EntityTrail.entityTrail(fireballLeft, Particle.FLAME);
            fireballRight = player.launchProjectile(SmallFireball.class);
            fireballRight.setIsIncendiary(false);
            fireballRight.setVelocity(right);
            fireballRight.setShooter(player);
            EntityTrail.entityTrail(fireballRight, Particle.FLAME);
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFireballDamage(EntityDamageByEntityEvent e) {

        if (e.getDamager().equals(snowball)) {
            e.setCancelled(true);
            Player player = (Player) snowball.getShooter();
            if (player == null) return;
            if (!(e.getEntity() instanceof LivingEntity)) return;
            LivingEntity victim = (LivingEntity) e.getEntity();

            if (verifyEnemy(player, victim)) {
                DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, victim, player, 100);
                victim.getWorld().spawnParticle(Particle.SNOWBALL, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
                if (!chilledPlayers.containsKey(victim.getUniqueId())) {
                    chilledPlayers.put(victim.getUniqueId(), Cone.coneEffect(victim, Particle.REDSTONE, CHILL_DURATION, 0, 20L, Color.AQUA));
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, CHILL_DURATION * 20, 2));
                    Bukkit.getScheduler().scheduleAsyncDelayedTask(RunicCore.getInstance(), () -> chilledPlayers.remove(victim.getUniqueId()), CHILL_DURATION * 20L);
                } else {
                    chilledPlayers.get(victim.getUniqueId()).cancel(); // cancel particle task
                    chilledPlayers.remove(victim.getUniqueId());
                    Location toBeTrapped = victim.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
                    trapEntity(toBeTrapped, Material.ICE, FREEZE_DURATION);
                    Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> TeleportUtil.teleportEntity(victim, toBeTrapped), 2L);
                }
            }
            return;
        }

        // only listen for our fireball (or icebolt)
        if (!e.getDamager().equals(fireball)
                && !e.getDamager().equals(fireballLeft)
                && !e.getDamager().equals(fireballRight)) return;

        e.setCancelled(true);

        // grab our variables
        Player player = (Player) fireball.getShooter();
        if (player == null) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;
        LivingEntity victim = (LivingEntity) e.getEntity();

        if (verifyEnemy(player, victim)) {
            DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, victim, player, 100);
            victim.getWorld().spawnParticle(Particle.FLAME, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);

            if (hasPassive(player, "Scald")) {
                for (Entity en : fireball.getNearbyEntities(Scald.getRadius(), Scald.getRadius(), Scald.getRadius())) {
                    if (!verifyEnemy(player, en)) continue;
                    DamageUtil.damageEntitySpell(DAMAGE_AMOUNT * Scald.getDamagePercent(), (LivingEntity) en, player, 100);
                }
            }

            if (applyBurn) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
                    DamageUtil.damageEntitySpell((DAMAGE_AMOUNT / 2), victim, player, 50);
                    victim.getWorld().spawnParticle
                            (Particle.LAVA, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
                }, 20L);
            }
        }
    }

    private final BlockFace[] cage = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    private void trapEntity(Location location, Material material, int duration) {
        Map<Block, BlockData> changedBlocks = new HashMap<>();
        Location[] locs = new Location[]{location, location.clone().add(0,1,0)};
        for (Location loc : locs) {
            for (BlockFace bf : cage) {
                Block block = loc.getBlock().getRelative(bf, 1);
                changedBlocks.put(block, block.getBlockData());
                block.setType(material);
            }
        }
        // also block above the player
        Block top = location.clone().add(0,2,0).getBlock();
        changedBlocks.put(top, top.getBlockData());
        top.setType(material);
        // todo: add ice block below their feet?
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
            for (Block block : changedBlocks.keySet()) {
                block.setType(changedBlocks.get(block).getMaterial());
            }
        }, duration * 20L);
    }

    public static int getChillDuration() {
        return CHILL_DURATION;
    }

    public static int getFreezeDuration() {
        return FREEZE_DURATION;
    }

    public static Map<UUID, BukkitTask> getChilledPlayers() {
        return chilledPlayers;
    }
}

