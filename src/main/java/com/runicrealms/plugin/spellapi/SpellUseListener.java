package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.WeaponType;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.listeners.DamageListener;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
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
import java.util.HashSet;
import java.util.UUID;

public class SpellUseListener implements Listener {

    private static final int SPELL_TIMEOUT = 5;
    private static final int GLOBAL_COOLDOWN_TICKS = 5; // 0.25s
    private static final String ACTIVATE_RIGHT = "R";
    private static final String ACTIVATE_LEFT = "L";
    private static final HashSet<UUID> CAST_MENU_CASTERS = new HashSet<>();
    private static final HashMap<UUID, BukkitTask> casters = new HashMap<>();

    enum ClickType {
        LEFT,
        RIGHT
    }

    @EventHandler
    public void onWeaponInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        WeaponType heldItemType = WeaponType.matchType(e.getPlayer().getInventory().getItemInMainHand());
        if (heldItemType == WeaponType.NONE) return;
        if (heldItemType == WeaponType.GATHERING_TOOL) return;
        if (!DamageListener.matchClass(e.getPlayer(), false)) return;
        if (CAST_MENU_CASTERS.contains(e.getPlayer().getUniqueId())) return;
        Player player = e.getPlayer();
        String className = RunicCoreAPI.getPlayerClass(player); // lowercase
        boolean isArcher = className.equalsIgnoreCase("archer");
        if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)
            activateSpellMode(player, ClickType.LEFT, 2, isArcher);
        else if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
            activateSpellMode(player, ClickType.RIGHT, 3, isArcher);
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
            player.sendTitle
                    (
                            "", ChatColor.LIGHT_PURPLE + prefix +
                                    " - " + ChatColor.DARK_GRAY + "[1] [L] [R] [F]", 0, SPELL_TIMEOUT * 20, 0
                    );
        } else {
            castSpell(player, whichSpellToCast, RunicCoreAPI.getPlayerClass(player).equalsIgnoreCase("archer"));
        }
        CAST_MENU_CASTERS.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> CAST_MENU_CASTERS.remove(player.getUniqueId()), GLOBAL_COOLDOWN_TICKS);
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

    @EventHandler
    public void onSpellCast(PlayerItemHeldEvent e) {
        if (!casters.containsKey(e.getPlayer().getUniqueId())) return;
        if (e.getNewSlot() != 0) return;
        e.setCancelled(true);
        castSpell(e.getPlayer(), 1, RunicCoreAPI.getPlayerClass(e.getPlayer()).equalsIgnoreCase("archer"));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpellCast(SpellCastEvent e) {
        if (!e.isCancelled() && e.willExecute())
            e.getSpellCasted().execute(e.getCaster(), SpellItemType.ARTIFACT);
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent e) {
        if (!casters.containsKey(e.getPlayer().getUniqueId())) return;
        castSpell(e.getPlayer(), 4, RunicCoreAPI.getPlayerClass(e.getPlayer()).equalsIgnoreCase("archer"));
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
        player.sendTitle
                (
                        "",
                        ChatColor.LIGHT_PURPLE + prefix + " - "
                                + determineSelectedSlot(number), 0, 15, 0
                );
        castSelectedSpell(player, number);
    }

    /**
     * Determines which UI button to lightup.
     *
     * @param number which of the four spells to execute
     * @return A String corresponding to the new UI to display
     */
    private String determineSelectedSlot(int number) {
        String selectedSpell = "";
        switch (number) {
            case 1:
                selectedSpell = ChatColor.LIGHT_PURPLE + "[1] " + ChatColor.DARK_GRAY + "[L] [R] [F]";
                break;
            case 2:
                selectedSpell = ChatColor.DARK_GRAY + "[1] " + ChatColor.LIGHT_PURPLE + "[L] " + ChatColor.DARK_GRAY + "[R] [F]";
                break;
            case 3:
                selectedSpell = ChatColor.DARK_GRAY + "[1] [L] " + ChatColor.LIGHT_PURPLE + "[R] " + ChatColor.DARK_GRAY + "[F]";
                break;
            case 4:
                selectedSpell = ChatColor.DARK_GRAY + "[1] [L] [R] " + ChatColor.LIGHT_PURPLE + "[F]";
                break;
        }
        return selectedSpell;
    }

    /**
     * Determines which spell to case based on the selected number.
     *
     * @param player caster of spell
     * @param number which spell number to cast (1, 2, 3, 4)
     */
    private void castSelectedSpell(Player player, int number) {
        Spell spellToCast = RunicCoreAPI.getPlayerSpell(player, number);
        if (spellToCast == null) return;
        if (RunicCore.getSpellManager().isOnCooldown(player, spellToCast.getName())) return;
        SpellCastEvent event = new SpellCastEvent(player, spellToCast);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static HashMap<UUID, BukkitTask> getCasters() {
        return casters;
    }
}

