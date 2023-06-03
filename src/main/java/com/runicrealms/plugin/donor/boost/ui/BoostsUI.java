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

import java.util.ArrayList;
import java.util.List;

public class BoostsUI implements InventoryHolder {

    private static final ItemStack topElement;

    static {
        topElement = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = topElement.getItemMeta();
        meta.setDisplayName(ColorUtil.format("&cExperience Boosts"));
        meta.setLore(List.of(
                ColorUtil.format("&7All online players are boosted for a limited duration"),
                ColorUtil.format("&7View combat, crafting, and gathering boosts"),
                ColorUtil.format("&7Purchase more boosts at &estore.runicrealms.com")));
        topElement.setItemMeta(meta);
    }

    private final Player player;
    private final Inventory inventory;

    public BoostsUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 45, ColorUtil.format("&cActivate EXP Boosts"));
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

    private void generateMenu() {
        this.inventory.clear();
        for (int i = 0; i < 9; i++) {
            if (i != 4) this.inventory.setItem(i, GUIUtil.BORDER_ITEM);
        }
        this.inventory.setItem(4, topElement);
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            boolean hasCraftingBoost = RunicCore.getBoostAPI().hasStoreBoost(player.getUniqueId(), StoreBoost.CRAFTING);
            ItemStack craftingIcon = new ItemStack(
                    RunicCore.getBoostAPI().hasStoreBoost(player.getUniqueId(), StoreBoost.CRAFTING) ? Material.ANVIL : Material.BARRIER,
                    hasCraftingBoost ? RunicCore.getBoostAPI().getStoreBoostCount(player.getUniqueId(), StoreBoost.CRAFTING) : 1);
            ItemMeta meta = craftingIcon.getItemMeta();
            meta.setDisplayName(ColorUtil.format("&cActivate &lCrafting &r&cExperience Boost"));
            List<String> lore = new ArrayList<>();
            if (!hasCraftingBoost) {
                lore.add(ColorUtil.format("&c&lYou do not have any crafting boosts."));
                lore.add(ColorUtil.format("&cVisit store.runicrealms.com to purchase more."));
                lore.add("");
            }
            lore.add(ColorUtil.format("&7The boost will apply to &feveryone online"));
            lore.add(ColorUtil.format("&7Duration: &f" + StoreBoost.CRAFTING.getDuration() + "&7 minutes"));
            lore.add(ColorUtil.format("&7Additional Experience multiplier: &f" + ((int) (StoreBoost.CRAFTING.getAdditionalMultiplier() * 100)) + "%"));
            meta.setLore(lore);
            craftingIcon.setItemMeta(meta);
            this.inventory.setItem(19, craftingIcon);

            boolean hasCombatBoost = RunicCore.getBoostAPI().hasStoreBoost(player.getUniqueId(), StoreBoost.COMBAT);
            ItemStack combatIcon = new ItemStack(
                    RunicCore.getBoostAPI().hasStoreBoost(player.getUniqueId(), StoreBoost.COMBAT) ? Material.WOODEN_SWORD : Material.BARRIER,
                    hasCombatBoost ? RunicCore.getBoostAPI().getStoreBoostCount(player.getUniqueId(), StoreBoost.COMBAT) : 1);
            meta = combatIcon.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            meta.setUnbreakable(true);
            meta.setDisplayName(ColorUtil.format("&cActivate &lCombat &r&cExperience Boost"));
            lore = new ArrayList<>();
            if (!hasCombatBoost) {
                lore.add(ColorUtil.format("&c&lYou do not have any combat boosts."));
                lore.add(ColorUtil.format("&cVisit store.runicrealms.com to purchase more."));
                lore.add("");
            }
            lore.add(ColorUtil.format("&7The boost will apply to &feveryone online"));
            lore.add(ColorUtil.format("&7Duration: &f" + StoreBoost.COMBAT.getDuration() + "&7 minutes"));
            lore.add(ColorUtil.format("&7Additional Experience multiplier: &f" + ((int) (StoreBoost.COMBAT.getAdditionalMultiplier() * 100)) + "%"));
            meta.setLore(lore);
            combatIcon.setItemMeta(meta);
            this.inventory.setItem(22, combatIcon);

            boolean hasGatheringBoost = RunicCore.getBoostAPI().hasStoreBoost(player.getUniqueId(), StoreBoost.GATHERING);
            ItemStack gatheringIcon = new ItemStack(
                    RunicCore.getBoostAPI().hasStoreBoost(player.getUniqueId(), StoreBoost.GATHERING) ? Material.WOODEN_PICKAXE : Material.BARRIER,
                    hasGatheringBoost ? RunicCore.getBoostAPI().getStoreBoostCount(player.getUniqueId(), StoreBoost.GATHERING) : 1);
            meta = gatheringIcon.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            meta.setUnbreakable(true);
            meta.setDisplayName(ColorUtil.format("&cActivate &lGathering &r&cExperience Boost"));
            lore = new ArrayList<>();
            if (!hasGatheringBoost) {
                lore.add(ColorUtil.format("&c&lYou do not have any gathering boosts."));
                lore.add(ColorUtil.format("&cVisit store.runicrealms.com to purchase more."));
                lore.add("");
            }
            lore.add(ColorUtil.format("&7The boost will apply to &feveryone online"));
            lore.add(ColorUtil.format("&7Duration: &f" + StoreBoost.GATHERING.getDuration() + "&7 minutes"));
            lore.add(ColorUtil.format("&7Additional Experience multiplier: &f" + ((int) (StoreBoost.GATHERING.getAdditionalMultiplier() * 100)) + "%"));
            meta.setLore(lore);
            gatheringIcon.setItemMeta(meta);
            this.inventory.setItem(25, gatheringIcon);
        });
    }
}
