package com.runicrealms.plugin.spellapi.skilltrees.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.spellapi.PlayerSpellWrapper;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SpellEditorGUI;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SpellGUI;
import com.runicrealms.plugin.utilities.GUIUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SpellGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        /*
        Preliminary checks
         */
        if (e.getClickedInventory() == null) return;
        if (!(e.getView().getTopInventory().getHolder() instanceof SpellGUI)) return;
        if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
            e.setCancelled(true);
            return;
        }
        SpellGUI spellGUI = (SpellGUI) e.getClickedInventory().getHolder();
        if (!e.getWhoClicked().equals(spellGUI.getPlayer())) {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            return;
        }
        Player player = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        if (spellGUI.getInventory().getItem(e.getRawSlot()) == null) return;

        ItemStack item = e.getCurrentItem();
        Material material = item.getType();

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        e.setCancelled(true);

        if (material == GUIUtil.backButton().getType())
            player.openInventory(new SpellEditorGUI(player).getInventory());
        else if (material == Material.PAPER) {
            String spellName = spellGUI.getInventory().getItem(e.getRawSlot()).getItemMeta().getDisplayName();
            updateSpellInSlot(player.getUniqueId(), spellGUI, spellName);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "You've set the spell in this slot to " + spellName + ChatColor.LIGHT_PURPLE + "!");
            player.openInventory(new SpellEditorGUI(player).getInventory());
        }
    }

    /**
     * Sets the in-memory spell in the current GUI slot for given player.
     *
     * @param uuid      of the player to set spell for
     * @param spellGUI  associated open GUI
     * @param spellName name of spell to set in slot
     */
    private void updateSpellInSlot(UUID uuid, SpellGUI spellGUI, String spellName) {
        switch (spellGUI.getSpellSlot()) {
            case PlayerSpellWrapper.PATH_1:
                RunicCore.getSkillTreeManager().getPlayerSpellWrapper(uuid).setSpellHotbarOne(ChatColor.stripColor(spellName));
                break;
            case PlayerSpellWrapper.PATH_2:
                RunicCore.getSkillTreeManager().getPlayerSpellWrapper(uuid).setSpellLeftClick(ChatColor.stripColor(spellName));
                break;
            case PlayerSpellWrapper.PATH_3:
                RunicCore.getSkillTreeManager().getPlayerSpellWrapper(uuid).setSpellRightClick(ChatColor.stripColor(spellName));
                break;
            case PlayerSpellWrapper.PATH_4:
                RunicCore.getSkillTreeManager().getPlayerSpellWrapper(uuid).setSpellSwapHands(ChatColor.stripColor(spellName));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + spellGUI.getSpellSlot());
        }
    }
}
