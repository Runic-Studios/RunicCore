package com.runicrealms.plugin.spellapi.skilltrees.listener;

import com.runicrealms.plugin.model.SpellField;
import com.runicrealms.plugin.spellapi.skilltrees.gui.RuneGUI;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SpellEditorGUI;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SpellGUI;
import com.runicrealms.plugin.utilities.GUIUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class SpellEditorGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) { 

        /*
        Preliminary checks
         */
        if (event.getClickedInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof SpellEditorGUI)) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }
        SpellEditorGUI spellEditorGUI = (SpellEditorGUI) event.getClickedInventory().getHolder();
        if (!event.getWhoClicked().equals(spellEditorGUI.getPlayer())) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;
        if (spellEditorGUI.getInventory().getItem(event.getRawSlot()) == null) return;

        ItemStack item = event.getCurrentItem();
        Material material = item.getType();

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        event.setCancelled(true);

        if (material == GUIUtil.BACK_BUTTON.getType())
            player.openInventory(new RuneGUI(player).getInventory());
        else if (event.getRawSlot() == SpellEditorGUI.SPELL_ONE_INDEX)
            player.openInventory(new SpellGUI(player, SpellField.HOT_BAR_ONE).getInventory());
        else if (event.getRawSlot() == SpellEditorGUI.SPELL_TWO_INDEX)
            if (SpellEditorGUI.hasSlotUnlocked(player, SpellEditorGUI.getSlotReq2()))
                player.openInventory(new SpellGUI(player, SpellField.LEFT_CLICK).getInventory());
            else
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
        else if (event.getRawSlot() == SpellEditorGUI.SPELL_THREE_INDEX)
            if (SpellEditorGUI.hasSlotUnlocked(player, SpellEditorGUI.getSlotReq3()))
                player.openInventory(new SpellGUI(player, SpellField.RIGHT_CLICK).getInventory());
            else
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
        else if (event.getRawSlot() == SpellEditorGUI.SPELL_FOUR_INDEX)
            if (SpellEditorGUI.hasSlotUnlocked(player, SpellEditorGUI.getSlotReq4()))
                player.openInventory(new SpellGUI(player, SpellField.SWAP_HANDS).getInventory());
            else
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
    }
}
