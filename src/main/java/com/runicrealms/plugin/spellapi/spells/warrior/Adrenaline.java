package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import com.runicrealms.plugin.spellapi.spellutil.particles.HelixParticleFrame;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Adrenaline extends Spell implements PhysicalDamageSpell {
    private static final int DURATION = 5;
    private static final int MAX_STACKS = 10;
    private static final double DAMAGE_PER_LEVEL = 0.25;
    private static final double PERCENT = 0.10;
    private final Map<UUID, AdrenalineTracker> adrenalineMap = new ConcurrentHashMap<>();

    public Adrenaline() {
        super("Adrenaline",
                "For the next " + DURATION + "s, each time you deal damage, " +
                        "gain a stack of Adrenaline, granting additional physical damage equal to" +
                        " " +
                        (PERCENT * 100) + "% of your &eStrength&7. Each hit refreshes the " +
                        "duration " +
                        "of your stacks, stacking up to " + MAX_STACKS + " times. " +
                        "At max stacks, extend the duration of this effect by 5s and gain Speed " +
                        "II " +
                        "for the remainder of the effect!",
                ChatColor.WHITE, CharacterClass.WARRIOR, 20, 30);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 1.0f);
        new HelixParticleFrame(0.5F, 3, 2.5F).playParticle(player, Particle.REDSTONE, player.getLocation(), Color.RED);
        adrenalineMap.put(player.getUniqueId(), new AdrenalineTracker(player.getUniqueId()));
    }

    private int foo(Player player) {
        // If the player is at max stacks, give them the max stacks damage
        if (adrenalineMap.get(player.getUniqueId()).getStacks() >= MAX_STACKS) {
//            Bukkit.broadcastMessage("max stacks damage");
            double damageBonus = PERCENT * RunicCore.getStatAPI().getPlayerStrength(player.getUniqueId());
            damageBonus *= MAX_STACKS;
            return (int) damageBonus;
        }
        // Otherwise increment stacks
        adrenalineMap.get(player.getUniqueId()).increment();
//        Bukkit.broadcastMessage(adrenalineMap.get(player.getUniqueId()).getStacks() + " is stacks");
        double damageBonus = PERCENT * RunicCore.getStatAPI().getPlayerStrength(player.getUniqueId());
        damageBonus *= adrenalineMap.get(player.getUniqueId()).getStacks();
//        Bukkit.broadcastMessage("damage bonus is " + damageBonus + " percent");
        // If this current increment brought us to threshold, refresh duration
        if (adrenalineMap.get(player.getUniqueId()).getStacks() >= MAX_STACKS) {
            refresh(player);
        }

        return (int) damageBonus;
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMagicDamage(MagicDamageEvent event) {
        if (!adrenalineMap.containsKey(event.getPlayer().getUniqueId())) return;
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        event.setAmount(event.getAmount() + foo(player));
    }

    /**
     * Activate on-hit effects
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onSuccessfulHit(PhysicalDamageEvent event) {
        if (!event.isBasicAttack()) return;
        if (!adrenalineMap.containsKey(event.getPlayer().getUniqueId())) return;
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        event.setAmount(event.getAmount() + foo(player));
    }

    private void refresh(Player player) {
        adrenalineMap.get(player.getUniqueId()).refreshTask();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
        Cone.coneEffect(player, Particle.REDSTONE, DURATION, 0, 20, Color.RED);
        addStatusEffect(player, RunicStatusEffect.SPEED_II, DURATION, false);
    }

    class AdrenalineTracker {
        private final UUID uuid;
        private final AtomicInteger stacks;
        private BukkitTask bukkitTask;

        public AdrenalineTracker(UUID uuid) {
            this.uuid = uuid;
            this.stacks = new AtomicInteger(0);
            this.bukkitTask =
                    Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                            () -> {
                                adrenalineMap.remove(uuid);
//                                Bukkit.broadcastMessage("effect timeout");
                            }, DURATION * 20L);
        }

        public int getStacks() {
            return stacks.get();
        }

        public UUID getUuid() {
            return uuid;
        }

        private void increment() {
            this.stacks.getAndIncrement();
        }

        /**
         * Refreshes the task that expires the buff
         */
        public void refreshTask() {
            this.bukkitTask.cancel();
            this.bukkitTask =
                    Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                            () -> {
                                adrenalineMap.remove(uuid);
//                                Bukkit.broadcastMessage("effect timeout");
                            }, DURATION * 20L);
        }
    }
}

