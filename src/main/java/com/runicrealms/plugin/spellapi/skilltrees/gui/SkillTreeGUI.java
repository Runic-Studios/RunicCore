package com.runicrealms.plugin.spellapi.skilltrees.gui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.model.SkillTreeData;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;
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

    private static final int INFO_ITEM_POSITION = 4;
    private static final int[] PERK_SLOTS = new int[]{10, 28, 46, 48, 30, 12, 14, 32, 50, 52, 34, 16};
    private final Inventory inventory;
    private final Player player;
    private final SkillTreeData skillTreeData;

    public SkillTreeGUI(Player player, SkillTreeData skillTreeData) {
        this.inventory = Bukkit.createInventory(this, 54,
                ColorUtil.format("&a&l" + skillTreeData.getSubclass().getName() + " Skill Tree"));
        this.player = player;
        this.skillTreeData = skillTreeData;
        openMenu();
    }

    /**
     * Creates the meta and lore for a perk GUI icon.
     *
     * @param perk Perk to create lore for (can be base stat or spell)
     * @return ItemStack for use in inventory
     */
    public static ItemStack buildPerkItem(Perk perk, boolean displayPoints, String description) {
        ItemStack perkItem = new ItemStack(Material.PAPER);
        ItemMeta meta = perkItem.getItemMeta();
        assert meta != null;
        if (perk instanceof PerkBaseStat) {
            if (displayPoints) {
                meta.setDisplayName
                        (
                                ChatColor.GREEN + ((PerkBaseStat) perk).getStat().getName() +
                                        ((PerkBaseStat) perk).getStat().getIcon() +
                                        ChatColor.WHITE + " [" +
                                        ChatColor.GREEN + perk.getCurrentlyAllocatedPoints() +
                                        ChatColor.WHITE + "/" +
                                        ChatColor.GREEN + +perk.getMaxAllocatedPoints() +
                                        ChatColor.WHITE + "]"
                        );
            } else {
                meta.setDisplayName(ChatColor.GREEN + ((PerkBaseStat) perk).getStat().getName());
            }
            meta.setLore(
                    ChatUtils.formattedText
                            (("\n&7Bonus per point: &a+" + ((PerkBaseStat) perk).getBonusAmount() +
                                    "\n\n&eCharacter Stat &7" + ((PerkBaseStat) perk).getStat().getDescription()))
            );
        } else {
            Spell spell = RunicCore.getSpellAPI().getSpell(((PerkSpell) perk).getSpellName());
            if (spell == null) {
                meta.setDisplayName(ChatColor.RED + "Error");
                perkItem.setItemMeta(meta);
                Bukkit.getServer().getLogger().info(ChatColor.RED + "Error, perk spell not found.");
                return perkItem;
            }
            String spellType = spell.isPassive() ? "PASSIVE SPELL " : "ACTIVE SPELL ";
            if (displayPoints)
                meta.setDisplayName
                        (
                                ChatColor.GREEN + spell.getName() +
                                        ChatColor.WHITE + " [" +
                                        ChatColor.GREEN + perk.getCurrentlyAllocatedPoints() +
                                        ChatColor.WHITE + "/" +
                                        ChatColor.GREEN + +perk.getMaxAllocatedPoints() +
                                        ChatColor.WHITE + "]"
                        );
            else
                meta.setDisplayName(ChatColor.GREEN + spell.getName());
            List<String> lore = new ArrayList<>
                    (ChatUtils.formattedText("\n" + ChatColor.GOLD + "" + ChatColor.BOLD +
                            spellType + ChatColor.GRAY + spell.getDescription()));
            if (!spell.isPassive()) {
                lore.add(ChatColor.DARK_AQUA + "Costs " + spell.getManaCost() + "✸");
                lore.add(ChatColor.RED + "Cooldown " + ChatColor.YELLOW + (int) spell.getCooldown() + "s");
            }
            lore.add("");
            lore.add(description);
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

    public static int getInfoItemPosition() {
        return INFO_ITEM_POSITION;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public Player getPlayer() {
        return this.player;
    }

    public SkillTreeData getSkillTree() {
        return this.skillTreeData;
    }

    public ItemStack infoItem() {
        ItemStack infoItem = new ItemStack(skillTreeData.getSubclass().getItemStack());
        ItemMeta meta = infoItem.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GREEN + skillTreeData.getSubclass().getName() + " Tree Info");
        String lore = "&7Remaining Skill Points: &a" + SkillTreeData.getAvailablePoints(player.getUniqueId(), RunicCore.getCharacterAPI().getCharacterSlot(player.getUniqueId()));
        meta.setLore(ChatUtils.formattedText(lore));
        infoItem.setItemMeta(meta);
        return infoItem;
    }

    /**
     * Opens the inventory associated w/ this GUI, ordering perks
     */
    private void openMenu() {

        this.inventory.clear();
        this.inventory.setItem(0, GUIUtil.BACK_BUTTON);
        this.inventory.setItem(INFO_ITEM_POSITION, infoItem());
        int i = 0;

        for (Perk perk : skillTreeData.getPerks()) {
            ItemStack item = buildPerkItem(perk, true, ChatColor.AQUA + "» Click to purchase");
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
}