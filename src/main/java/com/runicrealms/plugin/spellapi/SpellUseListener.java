package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.WeaponType;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.listeners.DamageListener;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.spellapi.event.SpellCastEvent;
import com.runicrealms.plugin.spellapi.event.SpellTriggerEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

public class SpellUseListener implements Listener {
    private static final int SPELL_TIMEOUT = 5;
    private static final String ACTIVATE_RIGHT = "R";
    private static final String ACTIVATE_LEFT = "L";
    private static final HashMap<UUID, BukkitTask> casters = new HashMap<>();

    public static HashMap<UUID, BukkitTask> getCasters() {
        return casters;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSpellTrigger(SpellTriggerEvent event) {
        Player player = event.getPlayer();
        SpellSlot spellSlot = event.getSpellslot();
        SpellTriggerType triggerType = event.getSpellTriggerType();
        // Bring up the UI
        if (!casters.containsKey(player.getUniqueId())) {
            if (spellSlot != SpellSlot.LEFT_CLICK && triggerType == SpellTriggerType.ARCHER) return;
            if (spellSlot != SpellSlot.RIGHT_CLICK && triggerType == SpellTriggerType.DEFAULT) return;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.25f, 1.0f);
            casters.put(player.getUniqueId(), castTimeoutTask(player));
            String prefix = triggerType == SpellTriggerType.ARCHER ? ACTIVATE_LEFT : ACTIVATE_RIGHT;
            boolean displayCastMenu = RunicCore.getSettingsManager().getSettingsData(player.getUniqueId()).isCastMenuEnabled();
            if (!displayCastMenu) return;
            // Add space to title to fix a 1.17/1.18 bug
            player.sendTitle
                    (
                            " ", ChatColor.LIGHT_PURPLE + prefix +
                                    " - " + ChatColor.DARK_GRAY + "[" + getActivationOne(player) + "] [L] [R] [" + getActivationFour(player) + "]", 0, SPELL_TIMEOUT * 20, 0
                    );
            // Cast the spell
        } else {
            castSpell(player, spellSlot, RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(player).equalsIgnoreCase("archer"));
        }
    }

    /**
     * Determines which spell to case based on the selected number.
     *
     * @param player    caster of spell
     * @param spellSlot which spell number to cast (1, 2, 3, 4)
     */
    private void castSelectedSpell(Player player, SpellSlot spellSlot) {
        Spell spellToCast = RunicCore.getSpellAPI().getPlayerSpell(player, spellSlot);
        if (spellToCast == null) return;
        if (RunicCore.getSpellAPI().isOnCooldown(player, spellToCast.getName())) return;
        SpellCastEvent event = new SpellCastEvent(player, spellToCast);
        Bukkit.getPluginManager().callEvent(event);
    }

    /**
     * Removes the player from casters set and executes spell logic
     *
     * @param player    who cast the spell
     * @param spellSlot which spell to execute
     */
    private void castSpell(Player player, SpellSlot spellSlot, boolean isArcher) {
        casters.get(player.getUniqueId()).cancel(); // cancel timeout task
        casters.remove(player.getUniqueId());
        String prefix = isArcher ? ACTIVATE_LEFT : ACTIVATE_RIGHT;
        boolean displayCastMenu = RunicCore.getSettingsManager().getSettingsData(player.getUniqueId()).isCastMenuEnabled();
        if (displayCastMenu) {
            player.sendTitle
                    (
                            " ",
                            ChatColor.LIGHT_PURPLE + prefix + " - "
                                    + determineSelectedSlot(player, spellSlot), 0, 15, 0
                    );
        }
        castSelectedSpell(player, spellSlot);
    }

    /**
     * Fixes a bug where timeout task wouldn't cancel on spell cast
     *
     * @param player player to begin timeout task for
     * @return a task to be cancelled if they cast
     */
    private BukkitTask castTimeoutTask(Player player) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                casters.remove(player.getUniqueId());
            }
        }.runTaskLater(RunicCore.getInstance(), SPELL_TIMEOUT * 20L);
    }

    /**
     * Determines which UI button to light up.
     *
     * @param spellSlot which of the four spells to execute
     * @return A String corresponding to the new UI to display
     */
    private String determineSelectedSlot(Player player, SpellSlot spellSlot) {
        return switch (spellSlot) {
            case HOT_BAR_ONE ->
                    ChatColor.LIGHT_PURPLE + "[" + getActivationOne(player) + "] " + ChatColor.DARK_GRAY + "[L] [R] [" + getActivationFour(player) + "]";
            case LEFT_CLICK ->
                    ChatColor.DARK_GRAY + "[" + getActivationOne(player) + "] " + ChatColor.LIGHT_PURPLE + "[L] " + ChatColor.DARK_GRAY + "[R] [" + getActivationFour(player) + "]";
            case RIGHT_CLICK ->
                    ChatColor.DARK_GRAY + "[" + getActivationOne(player) + "] [L] " + ChatColor.LIGHT_PURPLE + "[R] " + ChatColor.DARK_GRAY + "[" + getActivationFour(player) + "]";
            case SWAP_HANDS ->
                    ChatColor.DARK_GRAY + "[" + getActivationOne(player) + "] [L] [R] " + ChatColor.LIGHT_PURPLE + "[" + getActivationFour(player) + "]";
        };
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (event.willExecute()) {
            boolean willCast = event.getSpell().execute(event.getCaster(), SpellItemType.ARTIFACT);
            if (!willCast)
                event.setCancelled(true);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpellCast(PlayerItemHeldEvent event) {
        if (!casters.containsKey(event.getPlayer().getUniqueId())) return;
        if (event.getNewSlot() != 0) return;
        event.setCancelled(true);
        castSpell(
                event.getPlayer(),
                SpellSlot.HOT_BAR_ONE,
                RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(event.getPlayer()).equalsIgnoreCase("archer")
        );
    }

    // If player swaps hotbar and is casting, reset them
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        if (event.getNewSlot() == event.getPreviousSlot()) {
            return;
        }

        BukkitTask task = casters.remove(event.getPlayer().getUniqueId());

        if (task == null) {
            return;
        }

        task.cancel();
        event.getPlayer().resetTitle();
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        if (!casters.containsKey(event.getPlayer().getUniqueId())) return;
        castSpell(
                event.getPlayer(),
                SpellSlot.SWAP_HANDS,
                RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(event.getPlayer()).equalsIgnoreCase("archer")
        );
    }

    @EventHandler(priority = EventPriority.HIGH) // Can't check if cancelled since right-clicking air is cancelled
    public void onWeaponInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        WeaponType heldItemType = WeaponType.matchType(event.getPlayer().getInventory().getItemInMainHand());
        if (heldItemType == WeaponType.NONE) return;
        if (heldItemType == WeaponType.GATHERING_TOOL) return;
        if (!DamageListener.matchClass(event.getPlayer(), false)) return;

        /*
        When right-clicking the ground, this event sometimes fires twice
        (useInteractedBlock returns ALLOW in one and DENY in the other),
        the hand is already accounted for at the top,
         so I implemented a band-aid fix

         -BoBoBalloon
         */
        if (event.useInteractedBlock() != Event.Result.DENY) return;

        Player player = event.getPlayer();
        // Call SpellTriggerEvent
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            SpellTriggerEvent spellTriggerEvent = new SpellTriggerEvent(player, SpellSlot.LEFT_CLICK);
            Bukkit.getPluginManager().callEvent(spellTriggerEvent);
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            SpellTriggerEvent spellTriggerEvent = new SpellTriggerEvent(player, SpellSlot.RIGHT_CLICK);
            Bukkit.getPluginManager().callEvent(spellTriggerEvent);
        }
    }

    // Trigger left click spell even if player hits mob
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!casters.containsKey(event.getPlayer().getUniqueId()) || event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }

        WeaponType heldItemType = WeaponType.matchType(event.getPlayer().getInventory().getItemInMainHand());
        if (heldItemType == WeaponType.NONE || heldItemType == WeaponType.GATHERING_TOOL || !DamageListener.matchClass(event.getPlayer(), false)) {
            return;
        }

        event.setCancelled(true);
        Bukkit.getPluginManager().callEvent(new SpellTriggerEvent(event.getPlayer(), SpellSlot.LEFT_CLICK));
    }

    private String getActivationOne(Player player) {
        return RunicCore.getSettingsManager().getSettingsData(player.getUniqueId()).getSpellSlotOneDisplay();
    }

    private String getActivationFour(Player player) {
        return RunicCore.getSettingsManager().getSettingsData(player.getUniqueId()).getSpellSlotFourDisplay();
    }
}

