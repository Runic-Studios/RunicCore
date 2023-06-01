package com.runicrealms.plugin.spellapi.skilltrees.gui;

import com.runicrealms.plugin.RunicCore;
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
    public static final int SPELL_THREE_INDEX = 46;
    public static final int SPELL_FOUR_INDEX = 52;
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

    private ItemStack ancientRunestone() {
        ItemStack skillTreeButton = new ItemStack(Material.POPPED_CHORUS_FRUIT);
        try {
            int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(player.getUniqueId());
            SpellData playerSpellData = RunicCore.getSkillTreeAPI().getPlayerSpellData(player.getUniqueId(), slot);
            ItemMeta meta = skillTreeButton.getItemMeta();
            if (meta == null) return skillTreeButton;
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Your Spell Setup:");
            String spellOne = "&d[1] &7Spell Hotbar 1: &f" + playerSpellData.getSpellHotbarOne();
            String spellTwo = "&d[L] &7Spell Left-click: &f" + playerSpellData.getSpellLeftClick();
            String spellThree = "&d[R] &7Spell Right-click: &f" + playerSpellData.getSpellRightClick();
            String spellFour = "&d[F] &7Spell Swap-hands: &f" + playerSpellData.getSpellSwapHands();
            List<String> lore = new ArrayList<String>() {
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
        this.inventory.setItem(0, GUIUtil.BACK_BUTTON);
        this.inventory.setItem(31, ancientRunestone());
        this.inventory.setItem(SPELL_ONE_INDEX, spellButton("Hotbar 1", 0));
        this.inventory.setItem(SPELL_TWO_INDEX, spellButton("Left-click", SLOT_REQ_2));
        this.inventory.setItem(SPELL_THREE_INDEX, spellButton("Right-click", SLOT_REQ_3));
        this.inventory.setItem(SPELL_FOUR_INDEX, spellButton("Swap-hands", SLOT_REQ_4));
    }

    private ItemStack spellButton(String displayName, int level) {
        ItemStack spellEditorButton = new ItemStack(Material.PAPER);
        boolean hasSlotUnlocked = hasSlotUnlocked(this.player, level);
        if (!hasSlotUnlocked)
            spellEditorButton = new ItemStack(Material.BARRIER);
        ItemMeta meta = spellEditorButton.getItemMeta();
        if (meta == null) return spellEditorButton;
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Spell " + displayName);
        meta.setLore(ChatUtils.formattedText(ChatColor.GRAY + "Configure your active spell for " +
                "slot: " + ChatColor.WHITE + displayName));
        if (!hasSlotUnlocked) {
            meta.setDisplayName(ChatColor.RED + "Spell Slot Locked");
            meta.setLore(ChatUtils.formattedText(ChatColor.GRAY + "Reach level [" + level + "] to unlock this slot!"));
        }
        spellEditorButton.setItemMeta(meta);
        return spellEditorButton;
    }
}