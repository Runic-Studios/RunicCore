package com.runicrealms.plugin.donator;

import com.runicrealms.plugin.RunicArtifacts;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.item.LoreGenerator;
import com.runicrealms.plugin.item.util.ItemRemover;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Objects;

public class ThreeDManager implements Listener {

    public ThreeDManager() {
        RunicArtifacts.getInstance().getServer().getPluginManager().registerEvents(this, RunicArtifacts.getInstance());
    }

    /*
     * When a player picks up a skinned item, it will revert to 2D if the player doesn't have the perms
     */
    @EventHandler
    public void onItemPickup(PlayerAttemptPickupItemEvent e) {
        if (e.getItem().getItemStack().getItemMeta() == null)
            return;
        if (!ThreeDSkin.findArtifactMaterial(e.getItem().getItemStack()))
            return;
        if (ThreeDSkin.getSkinFromName(e.getItem().getItemStack().getItemMeta().getDisplayName()) == null)
            return;
        ItemMeta meta = e.getItem().getItemStack().getItemMeta();
        if (e.getPlayer().hasPermission(ThreeDSkin.getSkinFromName(e.getItem().getItemStack().getItemMeta().getDisplayName()).getPermission()))
            return;
        ((Damageable) meta).setDamage(ThreeDSkin.getSkinFromName(e.getItem().getItemStack().getItemMeta().getDisplayName()).getTwoDDurability());
        e.getItem().getItemStack().setItemMeta(meta);
    }

    public boolean disguiseArtifact(Player donor, ItemStack toDisguise) {
        return updateArtifactSkin(donor, toDisguise);
    }

    /**
     * Update the player's artifacts from 2D --> 3D
     * @param donor with disguised artifact
     * @param item the item to disguise
     * @return true if the artifact had a skin and was updated
     */
    public boolean updateArtifactSkin(Player donor, ItemStack item) { // todo: just add untradeable
        if (item.getItemMeta() == null)
            return false;
        Damageable meta = (Damageable) item.getItemMeta();
        ThreeDSkin skin = ThreeDSkin.getSkinFromName(item.getItemMeta().getDisplayName());
        if (skin == null)
            return false;
        if (skin.getThreeDDurability() == ((Damageable) item.getItemMeta()).getDamage())
            return false;
        if (!skin.hasPermission(donor)) {
            donor.sendMessage(ChatColor.RED + "You have not unlocked this skin! You can buy skins in our store: runicrealms.buycraft.net");
            return false;
        }
        meta.setDamage(skin.getThreeDDurability());
        item.setItemMeta((ItemMeta) meta);

        ItemRemover.takeItem(donor, item, 1);

        if (!AttributeUtil.getCustomString(item, "soulbound").equalsIgnoreCase("true"))
            item = AttributeUtil.addCustomStat(item, "untradeable", "true");

        // regenerate item lore, give item
        LoreGenerator.generateItemLore(item, ChatColor.YELLOW,
                Objects.requireNonNull(item.getItemMeta()).getDisplayName(), "", true, ""); // todo: get this tag from attributes
        HashMap<Integer, ItemStack> leftOver = donor.getInventory().addItem(item);
        for (ItemStack is : leftOver.values()) {
            donor.getWorld().dropItem(donor.getLocation(), is);
        }

        return true;
    }

}
