package com.runicrealms.plugin.spellapi.skilltrees.gui;

import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;
import com.runicrealms.plugin.spellapi.skilltrees.SkillTree;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class SkillTreeGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Player player;
    private final SkillTree skillTree;

    public SkillTreeGUI(Player player, SkillTree skillTree) {
        this.inventory = Bukkit.createInventory(this, 54, ColorUtil.format("&a&lSkill Tree"));
        this.player = player;
        this.skillTree = skillTree;
        openMenu();
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public Player getPlayer() {
        return this.player;
    }

    public SkillTree getSkillTree() {
        return this.skillTree;
    }

    private void openMenu() {

        this.inventory.clear();
        this.inventory.setItem(0, backButton());
        int i = 0;
        int[] perkSlots = new int[]{10, 28, 46, 48, 30, 12, 14, 32, 50, 52, 34, 16};
        for (Perk perk : skillTree.getPerks()) {
            ItemStack test = new ItemStack(Material.PAPER);
            ItemMeta meta = test.getItemMeta();
            if (meta == null) continue;
            if (perk instanceof PerkBaseStat)
                meta.setDisplayName(((PerkBaseStat) perk).getBaseStatEnum().getName());
            else
                meta.setDisplayName(((PerkSpell) perk).getSpellName());
            test.setItemMeta(meta);
            this.inventory.setItem(perkSlots[i++], test);
        }

        int[] downArrowSlots = new int[]{19, 23, 37, 41};
        int[] upArrowSlots = new int[]{21, 25, 39, 43};
        int[] rightArrowSlots = new int[]{13, 47, 51};

        for (int downArrowSlot : downArrowSlots) {
            this.inventory.setItem(downArrowSlot, arrow(Material.RED_STAINED_GLASS_PANE));
        }
        for (int upArrowSlot : upArrowSlots) {
            this.inventory.setItem(upArrowSlot, arrow(Material.GREEN_STAINED_GLASS_PANE));
        }
        for (int rightArrowSlot : rightArrowSlots) {
            this.inventory.setItem(rightArrowSlot, arrow(Material.BROWN_STAINED_GLASS_PANE));
        }
    }

    private static ItemStack backButton() {
        ItemStack backButton = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = backButton.getItemMeta();
        if (meta == null) return backButton;
        meta.setDisplayName(ChatColor.RED + "Return");
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "Return to the class selection screen"));
        backButton.setItemMeta(meta);
        return backButton;
    }

    private static ItemStack arrow(Material material) {
        ItemStack arrow = new ItemStack(material);
        ItemMeta meta = arrow.getItemMeta();
        if (meta == null) return arrow;
        meta.setDisplayName(ChatColor.GRAY + "");
        arrow.setItemMeta(meta);
        return arrow;
    }
}