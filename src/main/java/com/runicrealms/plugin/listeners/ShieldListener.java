package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.ShieldBreakEvent;
import com.runicrealms.plugin.api.event.SpellShieldEvent;
import com.runicrealms.plugin.events.EnvironmentDamageEvent;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.rdb.event.CharacterSelectEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Shield;
import com.runicrealms.plugin.spellapi.spelltypes.ShieldPayload;
import com.runicrealms.plugin.utilities.HologramUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class ShieldListener implements Listener {
    private static final int HALF_HEART_AMOUNT = 50; // how much health is 1/2 heart?
    private static final int SHIELD_EXPIRE_TIME = 5; // Seconds
    private static final double CAP_MULTIPLIER = 0.33;

    /**
     * Running async task to expire shields after expire time
     */
    public ShieldListener() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {
            for (UUID uuid : RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters()) {
                if (!RunicCore.getSpellAPI().isShielded(uuid)) continue;
                ShieldPayload shieldPayload = RunicCore.getSpellAPI().getShieldedPlayers().get(uuid);
                if (shieldPayload == null) continue;
                long lastShieldTime = shieldPayload.shield().getStartTime();
                if (System.currentTimeMillis() - lastShieldTime > SHIELD_EXPIRE_TIME * 1000) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) continue;
                    Bukkit.getScheduler().runTask(RunicCore.getInstance(),
                            () -> Bukkit.getPluginManager().callEvent(new ShieldBreakEvent(shieldPayload, ShieldBreakEvent.BreakReason.FALLOFF)));
                }
            }
        }, 0, 5L);
    }

    /**
     * Damages a player's shield. If there's still shield left, updates the displayed yellow hearts.
     * Otherwise, returns the damage the player should take
     *
     * @param player who was damaged
     * @param damage the player received
     * @return the damage left over after the shield
     */
    private double damageShield(Player player, double damage) {
        Map<UUID, ShieldPayload> shieldedPlayers = RunicCore.getSpellAPI().getShieldedPlayers();
        double shield = shieldedPlayers.get(player.getUniqueId()).shield().getAmount();
        double shieldLeftOver = shield - damage;
        if (shieldLeftOver > 0) {
            player.setAbsorptionAmount(shieldLeftOver / HALF_HEART_AMOUNT);
            shieldedPlayers.get(player.getUniqueId()).shield().setAmount(shieldLeftOver);
            shieldedPlayers.get(player.getUniqueId()).shield().setStartTime(System.currentTimeMillis());
        } else if (shieldLeftOver <= 0) {
            // Shield was broken and there's leftover damage
            Bukkit.getScheduler().runTask(RunicCore.getInstance(),
                    () -> Bukkit.getPluginManager().callEvent(new ShieldBreakEvent(shieldedPlayers.get(player.getUniqueId()), ShieldBreakEvent.BreakReason.DAMAGE)));
        }
        return shieldLeftOver;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onGenericDamage(EnvironmentDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getVictim() instanceof Player victim)) return;
        if (!RunicCore.getSpellAPI().isShielded(victim.getUniqueId())) return;
        int shieldLeftOver = (int) damageShield(victim, event.getAmount());
        event.setAmount(Math.min(shieldLeftOver, 0)); // Deal leftover damage if shield is negative
    }

    @EventHandler(priority = EventPriority.HIGHEST) // runs after stat calculations
    public void onMagicDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getVictim() instanceof Player victim)) return;
        if (!RunicCore.getSpellAPI().isShielded(victim.getUniqueId())) return;
        int shieldLeftOver = (int) damageShield(victim, event.getAmount());
        event.setAmount(Math.min(shieldLeftOver, 0)); // Deal leftover damage if shield is negative
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMobDamage(MobDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getVictim() instanceof Player victim)) return;
        if (!RunicCore.getSpellAPI().isShielded(victim.getUniqueId())) return;
        int shieldLeftOver = (int) damageShield(victim, event.getAmount());
        event.setAmount(Math.min(shieldLeftOver, 0)); // Deal leftover damage if shield is negative
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getVictim() instanceof Player victim)) return;
        if (!RunicCore.getSpellAPI().isShielded(victim.getUniqueId())) return;
        int shieldLeftOver = (int) damageShield(victim, event.getAmount());
        event.setAmount(Math.min(shieldLeftOver, 0)); // Deal leftover damage if shield is negative
    }

    /**
     * Remove shield on select
     */
    @EventHandler
    public void onSelect(CharacterSelectEvent event) {
        event.getPlayer().setAbsorptionAmount(0);
    }

    /**
     * Remove shield on character quit async
     */
    @EventHandler
    public void onQuit(CharacterQuitEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(RunicCore.getInstance(),
                () -> Bukkit.getPluginManager().callEvent(new ShieldBreakEvent(RunicCore.getSpellAPI().getShieldedPlayers().get(player.getUniqueId()), ShieldBreakEvent.BreakReason.FALLOFF)));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onShield(SpellShieldEvent event) {
        if (event.isCancelled()) return;
        Player caster = event.getPlayer();
        Player recipient = event.getRecipient();
        int amount = event.getAmount();
        Map<UUID, ShieldPayload> shieldedPlayers = RunicCore.getSpellAPI().getShieldedPlayers();
        // Increment shield (no farther than cap)
        if (shieldedPlayers.containsKey(recipient.getUniqueId())) {
            Shield oldShield = shieldedPlayers.get(recipient.getUniqueId()).shield();
            double newAmount = amount + oldShield.getAmount();
            if (newAmount > recipient.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * CAP_MULTIPLIER)
                newAmount = recipient.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * CAP_MULTIPLIER;
            shieldedPlayers.get(recipient.getUniqueId()).shield().setAmount(newAmount);
            shieldedPlayers.get(recipient.getUniqueId()).shield().setStartTime(System.currentTimeMillis());
            shieldedPlayers.get(recipient.getUniqueId()).shield().addSource(caster.getUniqueId());
            // Create shield
        } else {
            shieldedPlayers.put(recipient.getUniqueId(), new ShieldPayload(recipient, event.getPlayer(), new Shield(amount, System.currentTimeMillis(), caster.getUniqueId())));
        }
        double currentAmount = shieldedPlayers.get(recipient.getUniqueId()).shield().getAmount();
        recipient.setAbsorptionAmount(currentAmount / HALF_HEART_AMOUNT);
        HologramUtil.createCombatHologram(Arrays.asList(caster, recipient), recipient.getEyeLocation(), ChatColor.YELLOW + "+" + amount + " ❤✦");
        recipient.playSound(recipient.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.25f, 0.5f);
        recipient.getWorld().spawnParticle(Particle.HEART, recipient.getEyeLocation(), 3, 0.35F, 0.35F, 0.35F, 0);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onShieldBreak(ShieldBreakEvent event) {
        if (event.isCancelled()) return;
        if (event.getShieldPayload() == null) return;
        Player player = event.getShieldPayload().player();
        player.setAbsorptionAmount(0);
        RunicCore.getSpellAPI().getShieldedPlayers().remove(player.getUniqueId());
        player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 0.5f);
    }

}
