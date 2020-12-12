package com.runicrealms.plugin.spellapi.skilltrees.gui;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;
import com.runicrealms.plugin.spellapi.skilltrees.SkillTree;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.ChatUtils;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.GUIUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SkillTreeGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Player player;
    private final SkillTree skillTree;
    private static final int[] PERK_SLOTS = new int[]{10, 28, 46, 48, 30, 12, 14, 32, 50, 52, 34, 16};

    public SkillTreeGUI(Player player, SkillTree skillTree) {
        this.inventory = Bukkit.createInventory(this, 54,
                ColorUtil.format("&a&l" + skillTree.getSubClassEnum().getName() + " Skill Tree"));
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
        this.inventory.setItem(0, GUIUtil.backButton());
        this.inventory.setItem(4, infoItem());
        int i = 0;

        for (Perk perk : skillTree.getPerks()) {
            ItemStack item = buildPerkItem(perk);
            this.inventory.setItem(PERK_SLOTS[i++], item);
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

    private ItemStack infoItem() {
        ItemStack infoItem = new ItemStack(skillTree.getSubClassEnum().getItemStack());
        ItemMeta meta = infoItem.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GREEN + skillTree.getSubClassEnum().getName() + " Tree Info");
        String lore = "&7Skill Points: 0";
        meta.setLore(ChatUtils.formattedText(lore));
        infoItem.setItemMeta(meta);
        return infoItem;
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
            meta.setLore(ChatUtils.formattedText
                    (("\n&eCharacter Stat &7" + ((PerkBaseStat) perk).getBaseStatEnum().getDescription())));
        } else {
            Spell spell = RunicCoreAPI.getSpell(((PerkSpell) perk).getSpellName());
            String spellType = spell.isPassive() ? "PASSIVE SPELL " : "ACTIVE SPELL ";
            meta.setDisplayName
                    (
                        ChatColor.GREEN + spell.getName() +
                        ChatColor.WHITE + " [" +
                        ChatColor.GREEN + perk.getCurrentlyAllocatedPoints() +
                        ChatColor.WHITE + "/" +
                        ChatColor.GREEN + + perk.getMaxAllocatedPoints() +
                        ChatColor.WHITE + "]"
                    );
            List<String> lore = new ArrayList<>
                    (ChatUtils.formattedText("\n" + ChatColor.GOLD + "" + ChatColor.BOLD +
                            spellType + ChatColor.GRAY + spell.getDescription()));
            if (!spell.isPassive())
                lore.add(ChatColor.DARK_AQUA + "Costs " + spell.getManaCost() + "✸");
            lore.add("");
            lore.add(ChatColor.AQUA + "» Click to purchase");
            meta.setLore(lore);
        }
        perkItem.setItemMeta(meta);
        return perkItem;
    }

    private static ItemStack arrow(Material material) {
        ItemStack arrow = new ItemStack(material);
        ItemMeta meta = arrow.getItemMeta();
        if (meta == null) return arrow;
        meta.setDisplayName(ChatColor.GRAY + "");
        arrow.setItemMeta(meta);
        return arrow;
    }

    public static int[] getPerkSlots() {
        return PERK_SLOTS;
    }
}