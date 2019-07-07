package com.runicrealms.plugin.item.artifact;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import com.runicrealms.plugin.attributes.AttributeUtil;

public class ArtifactListener implements Listener {

    // opens the artifact editor
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        Player pl = (Player) e.getWhoClicked();
        int itemslot = e.getSlot();

        // only listen for the artifact slot
        if (itemslot != 0) return;

        // don't trigger if there's no item in the slot to avoid null issues
        if (pl.getInventory().getItem(0) == null) return;
        ItemStack artifact = pl.getInventory().getItem(0);
        if (artifact == null) return;
        ItemMeta meta = artifact.getItemMeta();
        if (meta == null) return;
        int durability = ((Damageable) meta).getDamage();

        // only activate in survival mode to save builders the headache
        if (pl.getGameMode() != GameMode.SURVIVAL) return;

        // only listen for a player inventory
        if (e.getClickedInventory() == null) return;
        if (!(e.getClickedInventory().getType().equals(InventoryType.PLAYER))) return;

        e.setCancelled(true);

        // cancel editor in combat
        if (RunicCore.getCombatManager().getPlayersInCombat().containsKey(pl.getUniqueId())) {
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.RED + "You can't open that in combat!");
            return;
        }

        // open the artifact editor
        pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
        ItemGUI menu = ArtifactGUI.artifactEditor(pl, artifact, durability);
        menu.open(pl);
    }

    // cancel artifact swapping
    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent e) {

        Player pl = e.getPlayer();
        int slot = pl.getInventory().getHeldItemSlot();

        if (slot == 0) {
            e.setCancelled(true);
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.GRAY + "You cannot perform this action in this slot.");
        }
    }

    // cancel artifact dropping, rune dropping, hearthstone dropping
    @EventHandler
    public void onSoulboundItemDrop(PlayerDropItemEvent e) {

        Player pl = e.getPlayer();
        boolean isSoulbound = false;
        String souldbound = AttributeUtil.getCustomString(e.getItemDrop().getItemStack(), "soulbound");
        if (souldbound.equals("true")) {
            isSoulbound = true;
        }

        if (isSoulbound && pl.getGameMode() == GameMode.SURVIVAL) {
            e.setCancelled(true);
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.GRAY + "This item is soulbound.");
        }
    }
}
