package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.Pair;
import com.runicrealms.plugin.api.StatusEffectAPI;
import com.runicrealms.plugin.api.event.StatusEffectEvent;
import com.runicrealms.plugin.events.*;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A manager and a container for the custom runic status effects impacting a given player
 */
public class StatusEffectManager implements Listener, StatusEffectAPI {

    private final ConcurrentHashMap<UUID, ConcurrentHashMap<RunicStatusEffect, Pair<Long, Double>>> statusEffectMap;

    public StatusEffectManager() {
        this.statusEffectMap = new ConcurrentHashMap<>();
        startRemovalTask();
    }

    @Override
    public void addStatusEffect(LivingEntity livingEntity, RunicStatusEffect runicStatusEffect, double durationInSecs, boolean displayMessage) {
        Bukkit.getPluginManager().callEvent(new StatusEffectEvent(livingEntity, runicStatusEffect, durationInSecs, displayMessage));
    }

    @Override
    public boolean hasStatusEffect(UUID player, RunicStatusEffect runicStatusEffect) {
        return false;
    }

    @Override
    public boolean removeStatusEffect(UUID uuid, RunicStatusEffect statusEffect) {
        if (!statusEffectMap.containsKey(uuid)) return false;
        if (statusEffectMap.get(uuid).containsKey(statusEffect)) {
            statusEffectMap.get(uuid).remove(statusEffect);
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMobDamage(MobDamageEvent event) {
        UUID mobUuid = event.getDamager().getUniqueId();
        if (hasStatusEffect(mobUuid, RunicStatusEffect.SILENCE) ||
                hasStatusEffect(mobUuid, RunicStatusEffect.STUN) ||
                hasStatusEffect(mobUuid, RunicStatusEffect.DISARM) ||
                hasStatusEffect(event.getVictim().getUniqueId(), RunicStatusEffect.INVULNERABILITY)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!(hasStatusEffect(event.getPlayer().getUniqueId(), RunicStatusEffect.ROOT)
                || hasStatusEffect(event.getPlayer().getUniqueId(), RunicStatusEffect.STUN))) return;
        if (event.getTo() == null) return;
        Location to = event.getFrom();
        to.setPitch(event.getTo().getPitch());
        to.setYaw(event.getTo().getYaw());
        event.setTo(to);
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isBasicAttack() && hasStatusEffect(event.getPlayer().getUniqueId(), RunicStatusEffect.DISARM)) {
            event.setCancelled(true);
            return;
        }
        if (hasStatusEffect(event.getPlayer().getUniqueId(), RunicStatusEffect.SILENCE)
                || hasStatusEffect(event.getPlayer().getUniqueId(), RunicStatusEffect.STUN)
                || hasStatusEffect(event.getVictim().getUniqueId(), RunicStatusEffect.INVULNERABILITY)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent event) {
        if (hasStatusEffect(event.getCaster().getUniqueId(), RunicStatusEffect.SILENCE)
                || hasStatusEffect(event.getCaster().getUniqueId(), RunicStatusEffect.STUN)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpellDamage(MagicDamageEvent event) {
        if (hasStatusEffect(event.getPlayer().getUniqueId(), RunicStatusEffect.SILENCE)
                || hasStatusEffect(event.getPlayer().getUniqueId(), RunicStatusEffect.STUN)
                || hasStatusEffect(event.getVictim().getUniqueId(), RunicStatusEffect.INVULNERABILITY)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpellHeal(SpellHealEvent event) {
        if (hasStatusEffect(event.getPlayer().getUniqueId(), RunicStatusEffect.SILENCE)
                || hasStatusEffect(event.getPlayer().getUniqueId(), RunicStatusEffect.STUN)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onStatusEffect(StatusEffectEvent event) {
        if (event.isCancelled()) return;
        UUID uuid = event.getLivingEntity().getUniqueId();
        LivingEntity livingEntity = event.getLivingEntity();
        RunicStatusEffect runicStatusEffect = event.getRunicStatusEffect();
        double durationInSecs = event.getDurationInSeconds();

        // Prevent lower-level speeds from overriding
        if (runicStatusEffect == RunicStatusEffect.SPEED_I) {
            if (!(hasStatusEffect(uuid, RunicStatusEffect.SPEED_II) || hasStatusEffect(uuid, RunicStatusEffect.SPEED_III))) {
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (durationInSecs * 20), 0));
            }
            return;
        } else if (runicStatusEffect == RunicStatusEffect.SPEED_II) {
            if (!hasStatusEffect(uuid, RunicStatusEffect.SPEED_III)) {
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (durationInSecs * 20), 1));
            }
            return;
        } else if (runicStatusEffect == RunicStatusEffect.SPEED_III) {
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (durationInSecs * 20), 2));
            return;
        }

        // Since there's no entity move event, we handle root & stun the old-fashioned way for mobs
        if (!(livingEntity instanceof Player)
                && (runicStatusEffect == RunicStatusEffect.ROOT || runicStatusEffect == RunicStatusEffect.STUN)) {
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (durationInSecs * 20), 3));
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (int) (durationInSecs * 20), 127));
            return;
        }

        if (!statusEffectMap.containsKey(uuid)) {
            statusEffectMap.put(uuid, new ConcurrentHashMap<>());
        }
        statusEffectMap.get(uuid).put(runicStatusEffect, Pair.pair(System.currentTimeMillis(), durationInSecs));
        livingEntity.getWorld().playSound(livingEntity.getLocation(), runicStatusEffect.getSound(), 0.25f, 1.0f);
        if (livingEntity instanceof Player && event.willDisplayMessage()) {
            livingEntity.sendMessage(runicStatusEffect.getMessage());
        }
    }

    private void startRemovalTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {
            if (statusEffectMap.isEmpty()) return;
            for (UUID uuid : statusEffectMap.keySet()) {
                for (RunicStatusEffect runicStatusEffect : statusEffectMap.get(uuid).keySet()) {
                    long startTime = statusEffectMap.get(uuid).get(runicStatusEffect).first;
                    double duration = statusEffectMap.get(uuid).get(runicStatusEffect).second;
                    if (System.currentTimeMillis() - startTime > (duration * 1000)) {
                        statusEffectMap.get(uuid).remove(runicStatusEffect);
                        if (statusEffectMap.get(uuid).isEmpty()) {
                            statusEffectMap.remove(uuid);
                        }
                    }
                }
            }
        }, 0, 5L);
    }
}
