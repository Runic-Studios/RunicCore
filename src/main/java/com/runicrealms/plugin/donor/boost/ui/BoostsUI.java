package com.runicrealms.plugin.donor.boost.ui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ChatUtils;
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
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BoostsUI implements InventoryHolder {

    private static final ItemStack topElement;

    static {
        topElement = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = topElement.getItemMeta();
        meta.setDisplayName(ColorUtil.format("&2Experience Boosts"));
        meta.setLore(ChatUtils.formattedText("&7View your combat, crafting, and gathering boosts. Purchase more at &estore.runicrealms.com&7. All online players are boosted for a limited duration."));
        topElement.setItemMeta(meta);
    }

    private final Player player;
    private final Inventory inventory;

    public BoostsUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, ColorUtil.format("&2Activate EXP Boosts"));
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
        GUIUtil.fillInventoryBorders(this.inventory);
        this.inventory.setItem(0, GUIUtil.BACK_BUTTON);
        this.inventory.setItem(4, topElement);
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            boolean hasCombatBoost = RunicCore.getBoostAPI().hasStoreBoost(player.getUniqueId(), StoreBoost.COMBAT);
            ItemStack combatIcon = new ItemStack(
                    RunicCore.getBoostAPI().hasStoreBoost(player.getUniqueId(), StoreBoost.COMBAT) ? Material.ZOMBIE_HEAD : Material.BARRIER,
                    hasCombatBoost ? RunicCore.getBoostAPI().getStoreBoostCount(player.getUniqueId(), StoreBoost.COMBAT) : 1);
            ItemMeta meta = combatIcon.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            meta.setUnbreakable(true);
            meta.setDisplayName(ColorUtil.format("&2Activate &lCombat &r&2Experience Boost"));
            List<String> lore = new ArrayList<>();
            if (!hasCombatBoost) {
                lore.add(ColorUtil.format("&4&lYou do not have any combat boosts."));
                lore.add(ColorUtil.format("&cVisit store.runicrealms.com to purchase more."));
                lore.add("");
            }
            lore.add(ColorUtil.format("&7The boost will apply to &feveryone online"));
            lore.add(ColorUtil.format("&7Duration: &f" + StoreBoost.COMBAT.getDuration() + "&7 minutes"));
            lore.add(ColorUtil.format("&7Experience Multiplier: &f" + (1 + StoreBoost.COMBAT.getAdditionalMultiplier()) + "x"));
            meta.setLore(lore);
            combatIcon.setItemMeta(meta);
            this.inventory.setItem(20, combatIcon);

            boolean hasCraftingBoost = RunicCore.getBoostAPI().hasStoreBoost(player.getUniqueId(), StoreBoost.CRAFTING);
            ItemStack craftingIcon = new ItemStack(
                    RunicCore.getBoostAPI().hasStoreBoost(player.getUniqueId(), StoreBoost.CRAFTING) ? Material.ANVIL : Material.BARRIER,
                    hasCraftingBoost ? RunicCore.getBoostAPI().getStoreBoostCount(player.getUniqueId(), StoreBoost.CRAFTING) : 1);
            meta = craftingIcon.getItemMeta();
            meta.setDisplayName(ColorUtil.format("&2Activate &lCrafting &r&2Experience Boost"));
            lore = new ArrayList<>();
            if (!hasCraftingBoost) {
                lore.add(ColorUtil.format("&4&lYou do not have any crafting boosts."));
                lore.add(ColorUtil.format("&cVisit store.runicrealms.com to purchase more."));
                lore.add("");
            }
            lore.add(ColorUtil.format("&7The boost will apply to &feveryone online"));
            lore.add(ColorUtil.format("&7Duration: &f" + StoreBoost.CRAFTING.getDuration() + "&7 minutes"));
            lore.add(ColorUtil.format("&7Experience Multiplier: &f" + (1 + StoreBoost.CRAFTING.getAdditionalMultiplier()) + "x"));
            meta.setLore(lore);
            craftingIcon.setItemMeta(meta);
            this.inventory.setItem(22, craftingIcon);

            boolean hasGatheringBoost = RunicCore.getBoostAPI().hasStoreBoost(player.getUniqueId(), StoreBoost.GATHERING);
            ItemStack gatheringIcon = new ItemStack(
                    RunicCore.getBoostAPI().hasStoreBoost(player.getUniqueId(), StoreBoost.GATHERING) ? Material.IRON_PICKAXE : Material.BARRIER,
                    hasGatheringBoost ? RunicCore.getBoostAPI().getStoreBoostCount(player.getUniqueId(), StoreBoost.GATHERING) : 1);
            Damageable damageable = (Damageable) gatheringIcon.getItemMeta();
            damageable.setDamage(5);
            gatheringIcon.setItemMeta((ItemMeta) damageable);
            meta = gatheringIcon.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            meta.setUnbreakable(true);
            meta.setDisplayName(ColorUtil.format("&2Activate &lGathering &r&2Experience Boost"));
            lore = new ArrayList<>();
            if (!hasGatheringBoost) {
                lore.add(ColorUtil.format("&4&lYou do not have any gathering boosts."));
                lore.add(ColorUtil.format("&cVisit store.runicrealms.com to purchase more."));
                lore.add("");
            }
            lore.add(ColorUtil.format("&7The boost will apply to &feveryone online"));
            lore.add(ColorUtil.format("&7Duration: &f" + StoreBoost.GATHERING.getDuration() + "&7 minutes"));
            lore.add(ColorUtil.format("&7Experience Multiplier: &f" + (1 + StoreBoost.GATHERING.getAdditionalMultiplier()) + "x"));
            meta.setLore(lore);
            gatheringIcon.setItemMeta(meta);
            this.inventory.setItem(24, gatheringIcon);
        });
    }
}
