package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.BasicAttackEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class Diminuendo extends Spell implements DurationSpell, RadiusSpell {
    Map<UUID, Set<UUID>> affectedEnemiesMap = new HashMap<>();
    private double attackSpeedReduction;
    private double duration;
    private double mobDamageReduction;
    private double radius;

    public Diminuendo() {
        super("Diminuendo", CharacterClass.CLERIC);
        this.setDescription("You place down a jukebox " +
                "that plays music in a " + radius + " block radius for " + duration + "s. " +
                "Enemies inside the radius have their " +
                "attack speed reduced by " + (attackSpeedReduction * 100) + "%! " +
                "Mobs inside the radius have their damage " +
                "reduced by " + (mobDamageReduction * 100) + "% instead.");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Location location = player.getLocation();
        Location center = new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY(), location.getBlockZ() + 0.5);
        if (center.getWorld() == null) return;
        spawnJukebox(player, center);
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                affectedEnemiesMap.remove(player.getUniqueId());
                if (count >= duration) {
                    this.cancel();
                    player.getWorld().playSound(center, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5f, 1.0f);
                    player.getWorld().playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                } else {
                    count += 1;
                    player.getWorld().playSound(center, Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, 1.0f);
                    new HorizontalCircleFrame((float) radius, false).playParticle(player, Particle.CRIT, center, Color.GREEN);
                    affectedEnemiesMap.put(player.getUniqueId(), new HashSet<>());
                    for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius, target -> isValidEnemy(player, target))) {
                        affectedEnemiesMap.get(player.getUniqueId()).add(entity.getUniqueId());
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }

    public double getAttackSpeedReduction() {
        return attackSpeedReduction;
    }

    public void setAttackSpeedReduction(double attackSpeedReduction) {
        this.attackSpeedReduction = attackSpeedReduction;
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getMobDamageReduction() {
        return mobDamageReduction;
    }

    public void setMobDamageReduction(double mobDamageReduction) {
        this.mobDamageReduction = mobDamageReduction;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public void loadRadiusData(Map<String, Object> spellData) {
        Number attackSpeedReduction = (Number) spellData.getOrDefault("attack-speed-reduction", 0);
        setAttackSpeedReduction(attackSpeedReduction.doubleValue());
        Number mobDamageReduction = (Number) spellData.getOrDefault("mob-damage-reduction", 0);
        setMobDamageReduction(mobDamageReduction.doubleValue());
        Number radius = (Number) spellData.getOrDefault("radius", 0);
        setRadius(radius.doubleValue());
    }

    @EventHandler
    public void onBasicAttack(BasicAttackEvent event) {
        if (affectedEnemiesMap.isEmpty()) return;
        AtomicBoolean affected = new AtomicBoolean(false);
        affectedEnemiesMap.forEach((uuid, uuids) -> {
            if (uuids.contains(event.getPlayer().getUniqueId()))
                affected.set(true);
        });
        if (!affected.get()) return;
        double addedTicks = event.getOriginalCooldownTicks() * attackSpeedReduction;
        event.setCooldownTicks(event.getUnroundedCooldownTicks() + addedTicks);
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent event) {
        if (affectedEnemiesMap.isEmpty()) return;
        AtomicBoolean affected = new AtomicBoolean(false);
        affectedEnemiesMap.forEach((uuid, uuids) -> {
            if (uuids.contains(event.getMob().getUniqueId()))
                affected.set(true);
        });
        if (!affected.get()) return;
        double reduction = event.getAmount() * mobDamageReduction;
        event.setAmount((int) (event.getAmount() - reduction));
    }

    /**
     * Spawns a flower block with a task to remove it
     *
     * @param location to spawn the block
     */
    private void spawnJukebox(Player player, Location location) {
        Material oldMaterial = location.getBlock().getType();
        location.getBlock().setType(Material.JUKEBOX, false);
        Hologram hologram = HolographicDisplaysAPI.get(RunicCore.getInstance()).createHologram(location.getBlock().getLocation().add(0.5, 2.5, 0.5));
        hologram.getLines().appendText(ChatColor.WHITE + player.getName() + "'s " + ChatColor.GRAY + "Jukebox");
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            hologram.delete();
            location.getBlock().setType(oldMaterial);
        }, (int) duration * 20L);
    }

}

