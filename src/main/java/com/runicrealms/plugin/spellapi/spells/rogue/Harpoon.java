package com.runicrealms.plugin.spellapi.spells.rogue;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Harpoon extends Spell implements DurationSpell, PhysicalDamageSpell {
    private final Map<UUID, Trident> tridentMap = new HashMap<>();
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double tridentSpeed;

    public Harpoon() {
        super("Harpoon", CharacterClass.ROGUE);
        this.setDescription("You launch a projectile harpoon of the sea! Upon hitting an enemy, " +
                "the trident deals (" + damage + " + &f" + damagePerLevel +
                "x&7 lvl) physical⚔ damage and pulls its target towards you, slowing them for " + duration + "s! " +
                "If an ally is hit, you are instead teleported to their location.");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        tridentMap.put(player.getUniqueId(), player.launchProjectile(Trident.class));
        Trident trident = tridentMap.get(player.getUniqueId());
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(tridentSpeed);
        trident.setDamage(0);
        trident.setVelocity(velocity);
        trident.setShooter(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.75f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 0.5f, 1.5f);

        // more particles
        new BukkitRunnable() {
            @Override
            public void run() {
                if (trident.isDead() || trident.isOnGround()) {
                    this.cancel();
                    trident.remove();
                }
                trident.getWorld().spawnParticle(Particle.REDSTONE, trident.getLocation(),
                        10, 0, 0, 0, 0, new Particle.DustOptions(Color.TEAL, 1));
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = (int) duration;
    }

    @Override
    public double getPhysicalDamage() {
        return damage;
    }

    @Override
    public void setPhysicalDamage(double physicalDamage) {
        this.damage = (int) physicalDamage;
    }

    @Override
    public double getPhysicalDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setPhysicalDamagePerLevel(double physicalDamagePerLevel) {
        this.damagePerLevel = (int) physicalDamagePerLevel;
    }

    @Override
    public void loadPhysicalData(Map<String, Object> spellData) {
        Number physicalDamage = (Number) spellData.getOrDefault("physical-damage", 0);
        setPhysicalDamage(physicalDamage.doubleValue());
        Number physicalDamagePerLevel = (Number) spellData.getOrDefault("physical-damage-per-level", 0);
        setPhysicalDamagePerLevel(physicalDamagePerLevel.doubleValue());
        Number tridentSpeed = (Number) spellData.getOrDefault("trident-speed", 0);
        setTridentSpeed(tridentSpeed.doubleValue());
    }

    @EventHandler
    public void onTridentDamage(ProjectileCollideEvent event) {
        if (tridentMap.isEmpty()) return;
        if (event.getEntity().getShooter() == null) return;
        if (!(event.getEntity().getShooter() instanceof Player player)) return;
        if (!tridentMap.containsKey(player.getUniqueId())) return;
        Trident trident = tridentMap.get(player.getUniqueId());
        trident.remove();
        tridentMap.remove(player.getUniqueId());

        // grab our variables
        LivingEntity victim = (LivingEntity) event.getCollidedWith();
        if (isValidAlly(player, victim)) {
            player.teleport(victim.getEyeLocation());
            final Vector velocity = player.getLocation().getDirection().add(new Vector(0, 0.5, 0)).normalize().multiply(0.5);
            player.setVelocity(velocity);
            return;
        }
        if (isValidEnemy(player, victim)) {

            // apply spell mechanics
            Location playerLoc = player.getLocation();
            Location targetLoc = victim.getLocation();

            Vector pushUpVector = new Vector(0.0D, 0.4D, 0.0D);
            victim.setVelocity(pushUpVector);

            final double xDir = (playerLoc.getX() - targetLoc.getX()) / 3.0D;
            double zDir = (playerLoc.getZ() - targetLoc.getZ()) / 3.0D;
            //final double hPower = 0.5D;

            DamageUtil.damageEntityPhysical(damage, victim, player, false, true, this);
            addStatusEffect(victim, RunicStatusEffect.SLOW_III, duration, false);

            new BukkitRunnable() {
                @Override
                public void run() {
                    Vector pushVector = new Vector(xDir, 0.0D, zDir).normalize().multiply(2).setY(0.4D);
                    victim.setVelocity(pushVector);
                    victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
                    victim.getWorld().spawnParticle(Particle.CRIT, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                }
            }.runTaskLater(RunicCore.getInstance(), 4L);

            Bukkit.getPluginManager().callEvent(new HarpoonHitEvent(player, victim));
        }
    }

    public void setTridentSpeed(double tridentSpeed) {
        this.tridentSpeed = tridentSpeed;
    }

    public static class HarpoonHitEvent extends Event {
        private final Player caster;
        private final LivingEntity victim;

        private static final HandlerList HANDLER_LIST = new HandlerList();

        public HarpoonHitEvent(@NotNull Player caster, @NotNull LivingEntity victim) {
            this.caster = caster;
            this.victim = victim;
        }

        @NotNull
        public Player getCaster() {
            return this.caster;
        }

        @NotNull
        public LivingEntity getVictim() {
            return this.victim;
        }

        @NotNull
        @Override
        public HandlerList getHandlers() {
            return HANDLER_LIST;
        }

        @NotNull
        public static HandlerList getHandlerList() {
            return HANDLER_LIST;
        }
    }
}

