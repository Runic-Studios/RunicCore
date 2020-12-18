package com.runicrealms.plugin.spellapi.skilltrees.gui;

import com.runicrealms.plugin.RunicCore;
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

import java.util.Arrays;

public class SpellEditorGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Player player;
    public static final int SPELL_ONE_INDEX = 10;
    public static final int SPELL_TWO_INDEX = 16;
    public static final int SPELL_THREE_INDEX = 46;
    public static final int SPELL_FOUR_INDEX = 52;

    public SpellEditorGUI(Player player) {
        this.inventory = Bukkit.createInventory(this, 54, ColorUtil.format("&d&lSpell Editor"));
        this.player = player;
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

    /**
     * Opens the inventory associated w/ this GUI, ordering perks
     */
    private void openMenu() {
        this.inventory.clear();
        this.inventory.setItem(0, GUIUtil.backButton());
        this.inventory.setItem(31, ancientRune());
        this.inventory.setItem(SPELL_ONE_INDEX, spellButton("Hotbar 1"));
        this.inventory.setItem(SPELL_TWO_INDEX, spellButton("Left-click"));
        this.inventory.setItem(SPELL_THREE_INDEX, spellButton("Right-click"));
        this.inventory.setItem(SPELL_FOUR_INDEX, spellButton("Swap-hands"));
    }

    private ItemStack ancientRune() {
        ItemStack skillTreeButton = new ItemStack(Material.POPPED_CHORUS_FRUIT);
        ItemMeta meta = skillTreeButton.getItemMeta();
        if (meta == null) return skillTreeButton;
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Your Spell Setup:");
        String spellOne = "&d[1] &7Spell Hotbar 1: &f" + RunicCore.getSkillTreeManager().getPlayerSpellWrapper(player).getSpellHotbarOne();
        String spellTwo = "&d[L] &7Spell Left-click: &f" + RunicCore.getSkillTreeManager().getPlayerSpellWrapper(player).getSpellLeftClick();
        String spellThree = "&d[R] &7Spell Right-click: &f" + RunicCore.getSkillTreeManager().getPlayerSpellWrapper(player).getSpellRightClick();
        String spellFour = "&d[F] &7Spell Swap-hands: &f" + RunicCore.getSkillTreeManager().getPlayerSpellWrapper(player).getSpellSwapHands();
        meta.setLore(Arrays.asList(ColorUtil.format(spellOne), ColorUtil.format(spellTwo),
                ColorUtil.format(spellThree), ColorUtil.format(spellFour)));
        skillTreeButton.setItemMeta(meta);
        return skillTreeButton;
    }

    private static ItemStack spellButton(String displayName) {
        ItemStack spellEditorButton = new ItemStack(Material.GREEN_WOOL);
        ItemMeta meta = spellEditorButton.getItemMeta();
        if (meta == null) return spellEditorButton;
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Spell " + displayName);
        meta.setLore(ChatUtils.formattedText(ChatColor.GRAY + "Configure your active spell for " +
                "slot: " + displayName));
        spellEditorButton.setItemMeta(meta);
        return spellEditorButton;
    }
}