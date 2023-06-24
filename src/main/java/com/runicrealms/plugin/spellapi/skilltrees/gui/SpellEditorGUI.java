package com.runicrealms.plugin.spellapi.skilltrees.gui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.commands.admin.ResetTreeCMD;
import com.runicrealms.plugin.common.util.ChatUtils;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.GUIUtil;
import com.runicrealms.plugin.model.SpellData;
import com.runicrealms.plugin.rdb.RunicDatabase;
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
import java.util.Set;
import java.util.UUID;

public class SpellEditorGUI implements InventoryHolder {

    public static final int SPELL_ONE_INDEX = 10;
    public static final int SPELL_TWO_INDEX = 16;
    public static final int SPELL_THREE_INDEX = 37;
    public static final int SPELL_FOUR_INDEX = 43;
    private static final int SLOT_REQ_2 = 10;
    private static final int SLOT_REQ_3 = 15;
    private static final int SLOT_REQ_4 = 20;
    private final Inventory inventory;
    private final Player player;

    public SpellEditorGUI(Player player) {
        this.inventory = Bukkit.createInventory(this, 54, ColorUtil.format("&d&lSpell Editor"));
        this.player = player;
        openMenu();
    }

    /**
     * Nicely formats the player's list of passives
     *
     * @param uuid of the player
     * @return a string list of their passive spells
     */
    private static List<String> passiveList(UUID uuid) {
        List<String> passiveStringList = new ArrayList<>();
        passiveStringList.add(ChatColor.LIGHT_PURPLE + "Your Passives:");
        Set<String> passives = RunicCore.getSkillTreeAPI().getPassives(uuid);
        for (String passive : passives) {
            passiveStringList.add(ChatColor.WHITE + "- " + passive);
        }
        return passiveStringList;
    }

    public static boolean hasSlotUnlocked(Player player, int level) {
        return player.getLevel() >= level;
    }

    public static int getSlotReq2() {
        return SLOT_REQ_2;
    }

    public static int getSlotReq3() {
        return SLOT_REQ_3;
    }

    public static int getSlotReq4() {
        return SLOT_REQ_4;
    }

    private ItemStack resetButton() {
        ItemStack infoItem = new ItemStack(Material.MILK_BUCKET);
        ItemMeta meta = infoItem.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Reset Skill Trees");
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.addAll(ChatUtils.formattedText("&6&lCLICK &7to reset and refund your skill points! Current cost: " +
                ResetTreeCMD.getCostStringFromLevel(ResetTreeCMD.getCostFromLevel(player))));
        meta.setLore(lore);
        infoItem.setItemMeta(meta);
        return infoItem;
    }

    private ItemStack ancientRunestone() {
        ItemStack skillTreeButton = new ItemStack(Material.POPPED_CHORUS_FRUIT);
        try {
            int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(player.getUniqueId());
            SpellData playerSpellData = RunicCore.getSkillTreeAPI().getPlayerSpellData(player.getUniqueId(), slot);
            ItemMeta meta = skillTreeButton.getItemMeta();
            if (meta == null) return skillTreeButton;
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Your Spell Setup:");
            String slotOne = RunicCore.getSettingsManager().getSettingsData(player.getUniqueId()).getSpellSlotOneDisplay();
            String slotFour = RunicCore.getSettingsManager().getSettingsData(player.getUniqueId()).getSpellSlotFourDisplay();
            String spellOne = "&d[" + slotOne + "] &7Spell Slot One: &f" + playerSpellData.getSpellHotbarOne();
            String spellTwo = "&d[L] &7Spell Left-click: &f" + playerSpellData.getSpellLeftClick();
            String spellThree = "&d[R] &7Spell Right-click: &f" + playerSpellData.getSpellRightClick();
            String spellFour = "&d[" + slotFour + "] &7Spell Slot Four: &f" + playerSpellData.getSpellSwapHands();
            List<String> lore = new ArrayList<>() {
                {
                    add(ColorUtil.format(spellOne));
                    add(ColorUtil.format(spellTwo));
                    add(ColorUtil.format(spellThree));
                    add(ColorUtil.format(spellFour));
                }
            };
            lore.add("");
            List<String> passives = passiveList(player.getUniqueId());
            lore.addAll(passives);
            meta.setLore(lore);
            skillTreeButton.setItemMeta(meta);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return skillTreeButton;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public Player getPlayer() {
        return this.player;
    }

    /**
     * Opens the inventory associated w/ this GUI, ordering perks
     */
    private void openMenu() {
        this.inventory.clear();
        GUIUtil.fillInventoryBorders(this.inventory);
        this.inventory.setItem(0, GUIUtil.BACK_BUTTON);
        this.inventory.setItem(4, ancientRunestone());
        this.inventory.setItem(5, resetButton());
        this.inventory.setItem(SPELL_ONE_INDEX, spellButton(1, 0));
        this.inventory.setItem(SPELL_TWO_INDEX, spellButton(2, SLOT_REQ_2));
        this.inventory.setItem(SPELL_THREE_INDEX, spellButton(3, SLOT_REQ_3));
        this.inventory.setItem(SPELL_FOUR_INDEX, spellButton(4, SLOT_REQ_4));
    }

    private ItemStack spellButton(int slot, int level) {
        String letter = switch (slot) {
            case 1 -> RunicCore.getSettingsManager().getSettingsData(player.getUniqueId()).getSpellSlotOneDisplay();
            case 2 -> "L";
            case 3 -> "R";
            case 4 -> RunicCore.getSettingsManager().getSettingsData(player.getUniqueId()).getSpellSlotFourDisplay();
            default -> "";
        };
        String name = switch (slot) {
            case 1 -> "Slot One (Hotbar 1)";
            case 2 -> "Left-click";
            case 3 -> "Right-click";
            case 4 -> "Slot Four (Swap-hands)";
            default -> "";
        };
        String nameShort = switch (slot) {
            case 1 -> "Slot One";
            case 2 -> "Left-click";
            case 3 -> "Right-click";
            case 4 -> "Slot Four";
            default -> "";
        };

        ItemStack spellEditorButton = new ItemStack(Material.PAPER);
        boolean hasSlotUnlocked = hasSlotUnlocked(this.player, level);
        if (!hasSlotUnlocked)
            spellEditorButton = new ItemStack(Material.BARRIER);
        ItemMeta meta = spellEditorButton.getItemMeta();
        if (meta == null) return spellEditorButton;
        meta.setDisplayName(ColorUtil.format("&d[" + letter + "] Spell " + name));
        meta.setLore(ChatUtils.formattedText(ChatColor.GRAY + "Configure your active spell for " + ChatColor.WHITE + nameShort));
        if (!hasSlotUnlocked) {
            meta.setDisplayName(ChatColor.RED + "Spell Slot Locked");
            meta.setLore(ChatUtils.formattedText(ChatColor.GRAY + "Reach level [" + level + "] to unlock this slot!"));
        }
        spellEditorButton.setItemMeta(meta);
        return spellEditorButton;
    }
}