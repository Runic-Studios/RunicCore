package com.runicrealms.plugin.spellapi.skilltrees.gui;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;
import com.runicrealms.plugin.spellapi.skilltrees.SkillTree;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.ChatPaginator;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
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

    @NotNull
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

    /**
     * Opens the inventory associated w/ this GUI, ordering perks
     */
    private void openMenu() {

        this.inventory.clear();
        this.inventory.setItem(0, backButton());
        int i = 0;
        int[] perkSlots = new int[]{10, 28, 46, 48, 30, 12, 14, 32, 50, 52, 34, 16};
        for (Perk perk : skillTree.getPerks()) {
            ItemStack item = buildPerkItem(perk);
            this.inventory.setItem(perkSlots[i++], item);
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

    /**
     * Creates the meta and lore for a perk GUI icon.
     * @param perk Perk to create lore for (can be base stat or spell)
     * @return ItemStack for use in inventory
     */
    private ItemStack buildPerkItem(Perk perk) {
        ItemStack perkItem = new ItemStack(Material.PAPER);
        ItemMeta meta = perkItem.getItemMeta();
        assert meta != null;
        if (perk instanceof PerkBaseStat) {
            meta.setDisplayName
                    (
                        ChatColor.GREEN + ((PerkBaseStat) perk).getBaseStatEnum().getName() +
                        ChatColor.WHITE + " [" +
                        ChatColor.GREEN + perk.getCurrentlyAllocatedPoints() +
                        ChatColor.WHITE + "/" +
                        ChatColor.GREEN + + perk.getMaxAllocatedPoints() +
                        ChatColor.WHITE + "]"
                    );
            meta.setLore(Arrays.asList(ChatPaginator.wordWrap
                    (ChatColor.YELLOW + "Character Stat\n" + ChatColor.GRAY + ((PerkBaseStat) perk).getBaseStatEnum().getDescription(), 25)));
        } else {
            Spell spell = RunicCoreAPI.getSpell(((PerkSpell) perk).getSpellName());
            String spellType = spell.getIsPassive() ? "PASSIVE SPELL " : "ACTIVE SPELL";
            meta.setDisplayName
                    (
                        ChatColor.GREEN + spell.getName() +
                        ChatColor.WHITE + " [" +
                        ChatColor.GREEN + perk.getCurrentlyAllocatedPoints() +
                        ChatColor.WHITE + "/" +
                        ChatColor.GREEN + + perk.getMaxAllocatedPoints() +
                        ChatColor.WHITE + "]"
                    );
            meta.setLore(Arrays.asList(ChatPaginator.wordWrap(ChatColor.GOLD + ""
                    + ChatColor.BOLD + spellType + ChatColor.GRAY + spell.getDescription(), 25)));
        }
        perkItem.setItemMeta(meta);
        return perkItem;
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