package com.runicrealms.plugin.spellapi.skilltrees.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.GUIUtil;
import com.runicrealms.plugin.model.SpellData;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SpellEditorGUI;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SpellGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class SpellGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        /*
        Preliminary checks
         */
        if (event.getClickedInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof SpellGUI)) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }
        SpellGUI spellGUI = (SpellGUI) event.getClickedInventory().getHolder();
        if (!event.getWhoClicked().equals(spellGUI.getPlayer())) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;
        if (spellGUI.getInventory().getItem(event.getRawSlot()) == null) return;

        ItemStack item = event.getCurrentItem();
        Material material = item.getType();

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        event.setCancelled(true);

        if (material == GUIUtil.BACK_BUTTON.getType())
            player.openInventory(new SpellEditorGUI(player).getInventory());
        else if (material == Material.NETHER_WART) {
            String spellName = spellGUI.getInventory().getItem(event.getRawSlot()).getItemMeta().getDisplayName();
            updateSpellInSlot(player.getUniqueId(), spellGUI, spellName);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "You've set the spell in this slot to " + spellName + ChatColor.LIGHT_PURPLE + "!");
            player.openInventory(new SpellEditorGUI(player).getInventory());
        }
    }

    /**
     * Sets the in-memory spell in the current GUI slot for given player.
     *
     * @param uuid      of player to set spell for
     * @param spellGUI  associated open GUI
     * @param spellName name of spell to set in slot
     */
    private void updateSpellInSlot(UUID uuid, SpellGUI spellGUI, String spellName) {
        String spell = ChatColor.stripColor(spellName);
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(uuid);
        SpellData playerSpellData = RunicCore.getSkillTreeAPI().getPlayerSpellData(uuid, slot);
        switch (spellGUI.getSpellField()) {
            case HOT_BAR_ONE -> playerSpellData.setSpellHotbarOne(spell);
            case LEFT_CLICK -> playerSpellData.setSpellLeftClick(spell);
            case RIGHT_CLICK -> playerSpellData.setSpellRightClick(spell);
            case SWAP_HANDS -> playerSpellData.setSpellSwapHands(spell);
            default -> throw new IllegalStateException("Unexpected value: " + spellGUI.getSpellField());
        }
        try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
            playerSpellData.writeToJedis(uuid, jedis, RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(uuid));
        }
    }
}
