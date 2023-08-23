package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.StatusEffectAPI;
import com.runicrealms.plugin.api.event.StatusEffectEvent;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.statuseffects.EntityBleedEvent;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A manager and a container for the custom runic status effects impacting a given player
 */
public class StatusEffectManager implements Listener, StatusEffectAPI {

    private final ConcurrentHashMap<UUID, ConcurrentHashMap<RunicStatusEffect, Pair<Long, Double>>> statusEffectMap;
    private final Map<UUID, Long> lastbledMap;

    public StatusEffectManager() {
        this.statusEffectMap = new ConcurrentHashMap<>();
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        startRemovalTask();

        this.lastbledMap = new ConcurrentHashMap<>();
        //task chain does not directly support repeating tasks, I could do it using taskchain but even this ugly block is cleaner and easier -BoBo
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {
            long now = System.currentTimeMillis();
            Set<UUID> bleeding = new HashSet<>();
            for (Map.Entry<UUID, Long> pair : this.lastbledMap.entrySet()) {
                if (pair.getValue() + 2000 > now) { //2 second delay in milliseconds
                    continue;
                }

                pair.setValue(now);
                bleeding.add(pair.getKey());
            }

            if (bleeding.isEmpty()) {
                return;
            }

            Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
                for (UUID uuid : bleeding) {
                    LivingEntity recipient = (LivingEntity) Bukkit.getEntity(uuid); //safe to cast since only living entities can have bleeding applied in the first place

                    if (recipient == null || recipient.isDead()) {
                        this.removeStatusEffect(uuid, RunicStatusEffect.BLEED);
                        continue;
                    }

                    EntityBleedEvent event = new EntityBleedEvent(recipient);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return;
                    }

                    recipient.getWorld().playSound(recipient.getLocation(), Sound.ENTITY_COD_HURT, 0.5f, 1.0f);
                    recipient.getWorld().spawnParticle(Particle.BLOCK_CRACK, recipient.getLocation(), 10, Math.random() * 1.5, Math.random() / 2, Math.random() * 1.5, Material.REDSTONE_BLOCK.createBlockData());

                    DamageUtil.damageEntityGeneric(event.getAmount(), recipient, false);
                }
            });
        }, 0, 1);
    }

    @Override
    public void addStatusEffect(@NotNull LivingEntity livingEntity, @NotNull RunicStatusEffect runicStatusEffect, double durationInSecs, boolean displayMessage) {
        Bukkit.getPluginManager().callEvent(new StatusEffectEvent(livingEntity, runicStatusEffect, durationInSecs, displayMessage));
    }

    @Override
    public void cleanse(@NotNull UUID uuid) {
        if (!statusEffectMap.containsKey(uuid)) return;
        ConcurrentHashMap<RunicStatusEffect, Pair<Long, Double>> statusEffects = statusEffectMap.get(uuid);
        statusEffects.forEach((runicStatusEffect, longDoublePair) -> {
            if (!runicStatusEffect.isBuff()) {
                RunicCore.getStatusEffectAPI().removeStatusEffect(uuid, runicStatusEffect);
            }
        });
    }

    @Override
    public void purge(@NotNull UUID uuid) {
        if (!statusEffectMap.containsKey(uuid)) return;
        ConcurrentHashMap<RunicStatusEffect, Pair<Long, Double>> statusEffects = statusEffectMap.get(uuid);
        statusEffects.forEach((runicStatusEffect, longDoublePair) -> {
            if (runicStatusEffect.isBuff()) {
                RunicCore.getStatusEffectAPI().removeStatusEffect(uuid, runicStatusEffect);
            }
        });
    }

    @Override
    public boolean hasStatusEffect(@NotNull UUID uuid, @NotNull RunicStatusEffect statusEffect) {
        if (!statusEffectMap.containsKey(uuid)) return false;
        return statusEffectMap.get(uuid).containsKey(statusEffect);
    }

    @Override
    public boolean removeStatusEffect(@NotNull UUID uuid, @NotNull RunicStatusEffect statusEffect) {
        if (!statusEffectMap.containsKey(uuid)) return false;
        if (statusEffectMap.get(uuid).containsKey(statusEffect)) {
            // Remove vanilla potion slowness
            if (statusEffect == RunicStatusEffect.SLOW_I
                    || statusEffect == RunicStatusEffect.SLOW_II
                    || statusEffect == RunicStatusEffect.SLOW_III) {
                Entity entity = Bukkit.getEntity(uuid);
                if (entity == null) return false;
                if (!(entity instanceof LivingEntity)) return false;
                removePotionEffect((LivingEntity) entity, PotionEffectType.SLOW);
            }
            // Remove vanilla potion speed
            if (statusEffect == RunicStatusEffect.SPEED_I
                    || statusEffect == RunicStatusEffect.SPEED_II
                    || statusEffect == RunicStatusEffect.SPEED_III) {
                Entity entity = Bukkit.getEntity(uuid);
                if (entity == null) return false;
                if (!(entity instanceof LivingEntity)) return false;
                removePotionEffect((LivingEntity) entity, PotionEffectType.SPEED);
            }

            if (statusEffect == RunicStatusEffect.BLEED) {
                this.lastbledMap.remove(uuid); //if stop bleeding remove from the bled tick map
            }

            statusEffectMap.get(uuid).remove(statusEffect);
            return true;
        }
        return false;
    }

    @Override
    public double getStatusEffectDuration(@NotNull UUID uuid, @NotNull RunicStatusEffect effect) {
        if (!this.statusEffectMap.containsKey(uuid) || !this.statusEffectMap.get(uuid).containsKey(effect)) {
            return 0;
        }

        long startTime = statusEffectMap.get(uuid).get(effect).first;
        double duration = statusEffectMap.get(uuid).get(effect).second;
        long now = System.currentTimeMillis();

        if (now - startTime > (duration * 1000)) {
            return 0;
        }

        return duration - ((double) (now - startTime) / 1000);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMobDamage(MobDamageEvent event) {
        // Roots break on damage
        if (hasStatusEffect(event.getVictim().getUniqueId(), RunicStatusEffect.ROOT)) {
            removeStatusEffect(event.getVictim().getUniqueId(), RunicStatusEffect.ROOT);
        }
        UUID mobUuid = event.getEntity().getUniqueId();
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
                || hasStatusEffect(event.getPlayer().getUniqueId(), RunicStatusEffect.STUN)))
            return;
        if (event.getTo() == null) return;
        Location to = event.getFrom();
        to.setPitch(event.getTo().getPitch());
        to.setYaw(event.getTo().getYaw());
        event.setTo(to);
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        // Disarms ONLY stop basic attacks
        if (event.isBasicAttack() && hasStatusEffect(event.getPlayer().getUniqueId(), RunicStatusEffect.DISARM)) {
            event.setCancelled(true);
            return;
        }
        // Roots break on damage
        if (hasStatusEffect(event.getVictim().getUniqueId(), RunicStatusEffect.ROOT)) {
            removeStatusEffect(event.getVictim().getUniqueId(), RunicStatusEffect.ROOT);
        }
        // Silences stop everything except basic attacks
        if (!event.isBasicAttack() && hasStatusEffect(event.getPlayer().getUniqueId(), RunicStatusEffect.SILENCE)) {
            event.setCancelled(true);
            return;
        }
        // Stuns and invulns stop this event entirely
        if (hasStatusEffect(event.getPlayer().getUniqueId(), RunicStatusEffect.STUN)
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
        // Roots break on damage
        if (hasStatusEffect(event.getVictim().getUniqueId(), RunicStatusEffect.ROOT)) {
            removeStatusEffect(event.getVictim().getUniqueId(), RunicStatusEffect.ROOT);
        }
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

        if (hasStatusEffect(event.getPlayer().getUniqueId(), RunicStatusEffect.BLEED)) {
            event.setAmount((int) (event.getAmount() * .8)); //receive 20% less healing when bleeding
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onStatusEffect(StatusEffectEvent event) {
        if (event.isCancelled()) return;
        // Can't apply negative effects to invuln players
        if (hasStatusEffect(event.getLivingEntity().getUniqueId(), RunicStatusEffect.INVULNERABILITY) && !event.getRunicStatusEffect().isBuff()) {
            event.setCancelled(true);
            return;
        }
        UUID uuid = event.getLivingEntity().getUniqueId();
        LivingEntity livingEntity = event.getLivingEntity();
        RunicStatusEffect runicStatusEffect = event.getRunicStatusEffect();
        double durationInSecs = event.getDurationInSeconds();

        // Prevent lower-level slowness from overriding
        if (runicStatusEffect == RunicStatusEffect.SLOW_I) {
            if (!(hasStatusEffect(uuid, RunicStatusEffect.SLOW_II) || hasStatusEffect(uuid,
                    RunicStatusEffect.SLOW_III))) {
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,
                        (int) (durationInSecs * 20), 0));
            }
        } else if (runicStatusEffect == RunicStatusEffect.SLOW_II) {
            if (!hasStatusEffect(uuid, RunicStatusEffect.SLOW_III)) {
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (durationInSecs * 20), 1));
            }
        } else if (runicStatusEffect == RunicStatusEffect.SLOW_III) {
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (durationInSecs * 20), 2));
        }

        // Prevent lower-level speeds from overriding
        if (runicStatusEffect == RunicStatusEffect.SPEED_I) {
            if (!(hasStatusEffect(uuid, RunicStatusEffect.SPEED_II) || hasStatusEffect(uuid, RunicStatusEffect.SPEED_III))) {
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (durationInSecs * 20), 0));
            }
//            return;
        } else if (runicStatusEffect == RunicStatusEffect.SPEED_II) {
            if (!hasStatusEffect(uuid, RunicStatusEffect.SPEED_III)) {
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (durationInSecs * 20), 1));
            }
//            return;
        } else if (runicStatusEffect == RunicStatusEffect.SPEED_III) {
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (durationInSecs * 20), 2));
//            return;
        }

        if (runicStatusEffect == RunicStatusEffect.ROOT || runicStatusEffect == RunicStatusEffect.STUN) {
            livingEntity.getWorld().playSound(livingEntity.getLocation(),
                    Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.75f, 1.0f);
            // Since there's no entity move event, we handle root & stun the old-fashioned way for mobs
            if (!(livingEntity instanceof Player)) {
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (durationInSecs * 20), 3));
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (int) (durationInSecs * 20), 127));
                return;
            }
        }

        if (runicStatusEffect == RunicStatusEffect.BLEED && !this.lastbledMap.containsKey(uuid)) {
            this.lastbledMap.put(uuid, 0L);
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

    private void removePotionEffect(LivingEntity livingEntity, PotionEffectType potionEffectType) {
        for (PotionEffect effect : livingEntity.getActivePotionEffects()) {
            if (effect.getType() == potionEffectType)
                livingEntity.removePotionEffect(effect.getType());
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

                        if (runicStatusEffect == RunicStatusEffect.BLEED) {
                            this.lastbledMap.remove(uuid);
                        }
                    }
                }
            }
        }, 0, 5L);
    }
}
