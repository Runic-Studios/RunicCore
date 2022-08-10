package com.runicrealms.plugin.spellapi.skilltrees.listener;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.model.SpellField;
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
            updateSpellInSlot(player, spellGUI, spellName);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "You've set the spell in this slot to " + spellName + ChatColor.LIGHT_PURPLE + "!");
            player.openInventory(new SpellEditorGUI(player).getInventory());
        }
    }

    /**
     * Sets the in-memory spell in the current GUI slot for given player.
     *
     * @param player    to set spell for
     * @param spellGUI  associated open GUI
     * @param spellName name of spell to set in slot
     */
    private void updateSpellInSlot(Player player, SpellGUI spellGUI, String spellName) {
        String spell = ChatColor.stripColor(spellName);
        switch (spellGUI.getSpellField()) {
            case HOT_BAR_ONE:
                RunicCoreAPI.setRedisValue(player, SpellField.HOT_BAR_ONE.getField(), spell);
                break;
            case LEFT_CLICK:
                RunicCoreAPI.setRedisValue(player, SpellField.LEFT_CLICK.getField(), spell);
                break;
            case RIGHT_CLICK:
                RunicCoreAPI.setRedisValue(player, SpellField.RIGHT_CLICK.getField(), spell);
                break;
            case SWAP_HANDS:
                RunicCoreAPI.setRedisValue(player, SpellField.SWAP_HANDS.getField(), spell);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + spellGUI.getSpellField());
        }
    }
}
