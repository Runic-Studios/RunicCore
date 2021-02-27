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
import org.bukkit.inventory.ItemStack;

public class SpellGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        /*
        Preliminary checks
         */
        if (e.getClickedInventory() == null) return;
        if (!(e.getClickedInventory().getHolder() instanceof SpellGUI)) return;
        SpellGUI spellGUI = (SpellGUI) e.getClickedInventory().getHolder();
        if (!e.getWhoClicked().equals(spellGUI.getPlayer())) {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            return;
        }
        Player pl = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        if (spellGUI.getInventory().getItem(e.getRawSlot()) == null) return;

        ItemStack item = e.getCurrentItem();
        Material material = item.getType();

        pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        e.setCancelled(true);

        if (material == GUIUtil.backButton().getType())
            pl.openInventory(new SpellEditorGUI(pl).getInventory());
        else if (material == Material.PAPER) {
            String spellName = spellGUI.getInventory().getItem(e.getRawSlot()).getItemMeta().getDisplayName();
            updateSpellInSlot(pl, spellGUI, spellName);
            pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
            pl.sendMessage(ChatColor.LIGHT_PURPLE + "You've set the spell in this slot to " + spellName + ChatColor.LIGHT_PURPLE + "!");
            pl.openInventory(new SpellEditorGUI(pl).getInventory());
        }
    }

    /**
     * Sets the in-memory spell in the current GUI slot for given player.
     * @param pl player to set spell for
     * @param spellGUI associated open GUI
     * @param spellName name of spell to set in slot
     */
    private void updateSpellInSlot(Player pl, SpellGUI spellGUI, String spellName) {
        switch (spellGUI.getSpellSlot()) {
            case PlayerSpellWrapper.PATH_1:
                RunicCore.getSkillTreeManager().getPlayerSpellWrapper(pl).setSpellHotbarOne(ChatColor.stripColor(spellName));
                break;
            case PlayerSpellWrapper.PATH_2:
                RunicCore.getSkillTreeManager().getPlayerSpellWrapper(pl).setSpellLeftClick(ChatColor.stripColor(spellName));
                break;
            case PlayerSpellWrapper.PATH_3:
                RunicCore.getSkillTreeManager().getPlayerSpellWrapper(pl).setSpellRightClick(ChatColor.stripColor(spellName));
                break;
            case PlayerSpellWrapper.PATH_4:
                RunicCore.getSkillTreeManager().getPlayerSpellWrapper(pl).setSpellSwapHands(ChatColor.stripColor(spellName));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + spellGUI.getSpellSlot());
        }
    }
}
