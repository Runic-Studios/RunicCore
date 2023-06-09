package com.runicrealms.plugin.donor.ui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.DonorRank;
import com.runicrealms.plugin.common.util.ChatUtils;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.GUIUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DonorPerksUI implements InventoryHolder {

    private final Player player;
    private final Inventory inventory;

    public DonorPerksUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, ColorUtil.format("&cAdditional Donor Perks"));
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

        DonorRank rank = DonorRank.getDonorRank(player);
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            ItemStack topElement = new ItemStack(Material.EMERALD);
            ItemMeta meta = topElement.getItemMeta();
            meta.setDisplayName(ColorUtil.format("&cAdditional Donor Perks"));
            List<String> lore = new ArrayList<>();
            lore.addAll(ChatUtils.formattedText("&7View additional bonuses offered by your current donor rank of: " + rank.getChatColor() + ChatColor.BOLD + rank.getName().toUpperCase() + "&r&7."));
            lore.addAll(ChatUtils.formattedText("&7Visit &estore.runicrealms.com &7to purchase a rank and view what they have to offer."));
            meta.setLore(lore);
            topElement.setItemMeta(meta);
            this.inventory.setItem(4, topElement);

            ItemStack nameIcon = new ItemStack(Material.NAME_TAG);
            meta = nameIcon.getItemMeta();
            meta.setDisplayName(ColorUtil.format("&c&lName Color and Title"));
            meta.setLore(List.of(
                    ColorUtil.format("&7Customize your appearance in chat"),
                    "",
                    ColorUtil.format("&7Chat Name Color: " + rank.getChatColor() + "&l" + rank.getChatColor().name().replaceAll("_", " ").toUpperCase()),
                    ColorUtil.format("&7Title: " + rank.getChatColor() + "&l" + (rank.getTitle().isEmpty() ? "NONE" : rank.getTitle()))
            ));
            nameIcon.setItemMeta(meta);
            this.inventory.setItem(20, nameIcon);

            ItemStack priorityQueueIcon = new ItemStack(Material.RABBIT_FOOT);
            meta = priorityQueueIcon.getItemMeta();
            meta.setDisplayName(ColorUtil.format("&c&lPriority Queue"));
            meta.setLore(List.of(
                    ColorUtil.format("&7Cut the line when the server is full"),
                    "",
                    ColorUtil.format(rank.hasPriorityQueue() ? "&7Access: &a&lYES" : "&7Access: &4&lNO")
            ));
            priorityQueueIcon.setItemMeta(meta);
            this.inventory.setItem(22, priorityQueueIcon);

            ItemStack characterSlotsIcon = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) characterSlotsIcon.getItemMeta();
            skullMeta.setOwningPlayer(player);
            skullMeta.setDisplayName(ColorUtil.format("&c&lCharacter Slots"));
            skullMeta.setLore(List.of(
                    ColorUtil.format("&7Create more playable characters"),
                    "",
                    ColorUtil.format("&7Number of slots: &e&l" + rank.getClassSlots() + "&r&7/10")
            ));
            characterSlotsIcon.setItemMeta(skullMeta);
            this.inventory.setItem(24, characterSlotsIcon);

            ItemStack commandsIcon = new ItemStack(Material.PAPER);
            meta = commandsIcon.getItemMeta();
            meta.setDisplayName(ColorUtil.format("&c&lCommands"));
            meta.setLore(List.of(
                    ColorUtil.format("&7QoL Commands Exclusive to Donors"),
                    "",
                    ColorUtil.format(rank.hasPartySummon() ? "&7/&fparty summon&7: &a&lYES" : "&7/&fparty summon&7: &4&lNO")
            ));
            commandsIcon.setItemMeta(meta);
            this.inventory.setItem(30, commandsIcon);

            ItemStack gravestoneIcon = new ItemStack(Material.LIGHT_GRAY_SHULKER_BOX);
            meta = gravestoneIcon.getItemMeta();
            meta.setDisplayName(ColorUtil.format("&c&lGravestone Duration"));
            meta.setLore(List.of(
                    ColorUtil.format("&7Extra time to retrieve your items"),
                    "",
                    ColorUtil.format("&7Gravestone Total Duration: &e&l" + rank.getGravestoneDuration() + " minutes"),
                    ColorUtil.format("&7Gravestone Priority Duration: &e&l" + rank.getGravestonePriorityDuration() + " minutes")
            ));
            gravestoneIcon.setItemMeta(meta);
            this.inventory.setItem(32, gravestoneIcon);
        });
    }

}
