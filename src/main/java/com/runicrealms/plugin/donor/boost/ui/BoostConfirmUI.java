package com.runicrealms.plugin.donor.boost.ui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.GUIUtil;
import com.runicrealms.plugin.donor.boost.api.StoreBoost;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BoostConfirmUI implements InventoryHolder {

    private final Player player;
    private final Inventory inventory;
    private final StoreBoost boost;

    public BoostConfirmUI(Player player, StoreBoost boost) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 27, ColorUtil.format("&cCONFIRM: Activate &4" + boost.getName() + " &cBoost"));
        this.boost = boost;
        generateMenu();
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public Player getPlayer() {
        return this.player;
    }

    public StoreBoost getBoost() {
        return this.boost;
    }

    private void generateMenu() {
        this.inventory.clear();
        for (int i = 0; i < 9; i++) if (i != 4) this.inventory.setItem(i, GUIUtil.BORDER_ITEM);
        for (int i = 18; i < 27; i++) this.inventory.setItem(i, GUIUtil.BORDER_ITEM);
        this.inventory.setItem(9, GUIUtil.BORDER_ITEM);
        this.inventory.setItem(17, GUIUtil.BORDER_ITEM);

        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            ItemStack topElement = new ItemStack(Material.WRITABLE_BOOK);
            ItemMeta meta = topElement.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.setDisplayName(ColorUtil.format("&cCONFIRM: Activate &4" + boost.getName() + " &cExperience Boost"));
            topElement.setItemMeta(meta);
            this.inventory.setItem(4, topElement);

            ItemStack confirm = new ItemStack(Material.EXPERIENCE_BOTTLE);
            meta = confirm.getItemMeta();
            meta.setDisplayName(ColorUtil.format("&cCONFIRM: Activate &4" + boost.getName() + " &cExperience Boost"));
            meta.setLore(List.of(
                    ColorUtil.format("&7Doing so will immediately apply this " + boost.getName().toLowerCase() + " experience"),
                    ColorUtil.format("&7boost to all players on this server shard."),
                    ColorUtil.format("&4This boost will be removed from your boost inventory."),
                    "",
                    ColorUtil.format("&4&lThis action is irreversible.")
            ));
            confirm.setItemMeta(meta);
            this.inventory.setItem(11, confirm);

            ItemStack deny = new ItemStack(Material.BARRIER);
            meta = deny.getItemMeta();
            meta.setDisplayName(ColorUtil.format("&cDENY: Activate &4" + boost.getName() + " &cExperience Boost"));
            meta.setLore(List.of(ColorUtil.format("&7Doing so will not impact your boost inventory.")));
            deny.setItemMeta(meta);
            this.inventory.setItem(15, deny);
        });
    }
}
