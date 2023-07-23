package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.WeaponType;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.listeners.DamageListener;
import com.runicrealms.plugin.rdb.RunicDatabase;
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
    // private static final int GLOBAL_COOLDOWN_TICKS = 5; // 0.25s
    private static final String ACTIVATE_RIGHT = "R";
    private static final String ACTIVATE_LEFT = "L";
    // private static final HashSet<UUID> CAST_MENU_CASTERS = new HashSet<>();
    private static final HashMap<UUID, BukkitTask> casters = new HashMap<>();

    public static HashMap<UUID, BukkitTask> getCasters() {
        return casters;
    }

    /**
     * Handles spell logic for left and right-click spells, checking to 'flip' system for archer.
     *
     * @param player           player to cast
     * @param clickType        left or right
     * @param whichSpellToCast should be the spell '2' for left, '3' for right
     * @param isArcher         whether to flip UI for archer
     */
    private void activateSpellMode(Player player, ClickType clickType, int whichSpellToCast, boolean isArcher) {
        if (!casters.containsKey(player.getUniqueId())) {
            if (clickType != ClickType.LEFT && isArcher) return;
            if (clickType != ClickType.RIGHT && !isArcher) return;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.25f, 1.0f);
            casters.put(player.getUniqueId(), castTimeoutTask(player));
            String prefix = isArcher ? ACTIVATE_LEFT : ACTIVATE_RIGHT;
            boolean displayCastMenu = RunicCore.getSettingsManager().getSettingsData(player.getUniqueId()).isCastMenuEnabled();
            if (!displayCastMenu) return;
            // Add space to title to fix a 1.17/1.18 bug
            player.sendTitle
                    (
                            " ", ChatColor.LIGHT_PURPLE + prefix +
                                    " - " + ChatColor.DARK_GRAY + "[" + getActivationOne(player) + "] [L] [R] [" + getActivationFour(player) + "]", 0, SPELL_TIMEOUT * 20, 0
                    );
        } else {
            castSpell(player, whichSpellToCast, RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(player).equalsIgnoreCase("archer"));
        }
    }

    /**
     * Determines which spell to case based on the selected number.
     *
     * @param player caster of spell
     * @param number which spell number to cast (1, 2, 3, 4)
     */
    private void castSelectedSpell(Player player, int number) {
        Spell spellToCast = RunicCore.getSpellAPI().getPlayerSpell(player, number);
        if (spellToCast == null) return;
        if (RunicCore.getSpellAPI().isOnCooldown(player, spellToCast.getName())) return;
        SpellCastEvent event = new SpellCastEvent(player, spellToCast);
        Bukkit.getPluginManager().callEvent(event);
    }

    /**
     * Removes the player from casters set and executes spell logic
     *
     * @param player who cast the spell
     * @param number which spell to execute (1, 2, 3, 4)
     */
    private void castSpell(Player player, int number, boolean isArcher) {
        casters.get(player.getUniqueId()).cancel(); // cancel timeout task
        casters.remove(player.getUniqueId());
        String prefix = isArcher ? ACTIVATE_LEFT : ACTIVATE_RIGHT;
        boolean displayCastMenu = RunicCore.getSettingsManager().getSettingsData(player.getUniqueId()).isCastMenuEnabled();
        if (displayCastMenu) {
            player.sendTitle
                    (
                            " ",
                            ChatColor.LIGHT_PURPLE + prefix + " - "
                                    + determineSelectedSlot(player, number), 0, 15, 0
                    );
        }
        castSelectedSpell(player, number);
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
     * Determines which UI button to lightup.
     *
     * @param number which of the four spells to execute
     * @return A String corresponding to the new UI to display
     */
    private String determineSelectedSlot(Player player, int number) {
        return switch (number) {
            case 1 ->
                    ChatColor.LIGHT_PURPLE + "[" + getActivationOne(player) + "] " + ChatColor.DARK_GRAY + "[L] [R] [" + getActivationFour(player) + "]";
            case 2 ->
                    ChatColor.DARK_GRAY + "[" + getActivationOne(player) + "] " + ChatColor.LIGHT_PURPLE + "[L] " + ChatColor.DARK_GRAY + "[R] [" + getActivationFour(player) + "]";
            case 3 ->
                    ChatColor.DARK_GRAY + "[" + getActivationOne(player) + "] [L] " + ChatColor.LIGHT_PURPLE + "[R] " + ChatColor.DARK_GRAY + "[" + getActivationFour(player) + "]";
            case 4 ->
                    ChatColor.DARK_GRAY + "[" + getActivationOne(player) + "] [L] [R] " + ChatColor.LIGHT_PURPLE + "[" + getActivationFour(player) + "]";
            default -> "";
        };
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (event.willExecute()) {
            boolean willCast = event.getSpellCasted().execute(event.getCaster(), SpellItemType.ARTIFACT);
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
        castSpell(event.getPlayer(), 1, RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(event.getPlayer()).equalsIgnoreCase("archer"));
    }

    //if player swaps hotbar and is casting reset them
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
    public void onSwapHands(PlayerSwapHandItemsEvent e) {
        if (!casters.containsKey(e.getPlayer().getUniqueId())) return;
        castSpell(e.getPlayer(), 4, RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(e.getPlayer()).equalsIgnoreCase("archer"));
    }

    @EventHandler(priority = EventPriority.HIGH) //can't check if cancelled since right clicking air is cancelled
    public void onWeaponInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        WeaponType heldItemType = WeaponType.matchType(event.getPlayer().getInventory().getItemInMainHand());
        if (heldItemType == WeaponType.NONE) return;
        if (heldItemType == WeaponType.GATHERING_TOOL) return;
        if (!DamageListener.matchClass(event.getPlayer(), false)) return;
        Player player = event.getPlayer();
        String className = RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(player); // lowercase
        boolean isArcher = className.equalsIgnoreCase("archer");
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
            activateSpellMode(player, ClickType.LEFT, 2, isArcher);
        else if (event.useInteractedBlock() == Event.Result.DENY && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            //When right clicking the ground this event sometimes fires twice (useInteractedBlock returns ALLOW in one and DENY in the other), the hand is already accounted for at the top so I implemented a band-aid fix -BoBoBalloon
            activateSpellMode(player, ClickType.RIGHT, 3, isArcher);
        }
    }

    //trigger left click spell even if player hits mob
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!casters.containsKey(event.getPlayer().getUniqueId()) || event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }

        WeaponType heldItemType = WeaponType.matchType(event.getPlayer().getInventory().getItemInMainHand());

        if (heldItemType == WeaponType.NONE || heldItemType == WeaponType.GATHERING_TOOL || !DamageListener.matchClass(event.getPlayer(), false)) {
            return;
        }

        String className = RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(event.getPlayer()); // lowercase
        boolean isArcher = className.equalsIgnoreCase("archer");

        activateSpellMode(event.getPlayer(), ClickType.LEFT, 2, isArcher);
        event.setCancelled(true);
    }

    private String getActivationOne(Player player) {
        return RunicCore.getSettingsManager().getSettingsData(player.getUniqueId()).getSpellSlotOneDisplay();
    }

    private String getActivationFour(Player player) {
        return RunicCore.getSettingsManager().getSettingsData(player.getUniqueId()).getSpellSlotFourDisplay();
    }

    private enum ClickType {
        LEFT,
        RIGHT
    }
}

