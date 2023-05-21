package com.runicrealms.plugin.spellapi.skilltrees.gui;

import com.runicrealms.plugin.common.util.ChatUtils;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
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

import java.util.LinkedHashSet;
import java.util.Set;

public class RuneGUI implements InventoryHolder {

    public static final ItemStack SPELL_EDITOR_BUTTON;
    public static final ItemStack STATUS_EFFECT_TOOLTIP;

    static {
        SPELL_EDITOR_BUTTON = new ItemStack(Material.NETHER_WART);
        ItemMeta meta = SPELL_EDITOR_BUTTON.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Open Spell Editor");
        meta.setLore(ChatUtils.formattedText(ChatColor.GRAY + "Configure your active spells! " +
                "Set spells to be executed by different key combos!"));
        SPELL_EDITOR_BUTTON.setItemMeta(meta);

        STATUS_EFFECT_TOOLTIP = new ItemStack(Material.PRISMARINE_CRYSTALS);
        ItemMeta meta2 = SPELL_EDITOR_BUTTON.getItemMeta();
        assert meta2 != null;
        meta2.setDisplayName(ChatColor.RED + "[?] " + ChatColor.YELLOW + "Status Effect Key");
        Set<String> lore = new LinkedHashSet<>(); // Set ensures no duplicates for values like "slowness"
        lore.add(" ");
        for (RunicStatusEffect statusEffect : RunicStatusEffect.values()) {
            lore.add(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + statusEffect.getName() + " -");
            lore.addAll(ChatUtils.formattedText("&7" + statusEffect.getDescription()));
        }
        meta2.setLore(lore.stream().toList());
        STATUS_EFFECT_TOOLTIP.setItemMeta(meta2);
    }

    private final Inventory inventory;
    private final Player player;

    public RuneGUI(Player player) {
        this.inventory = Bukkit.createInventory(this, 45, ColorUtil.format("&dAncient Runestone"));
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
        this.inventory.setItem(11, skillTreeButton());
        this.inventory.setItem(13, SPELL_EDITOR_BUTTON);
        this.inventory.setItem(15, GUIUtil.CLOSE_BUTTON);
        this.inventory.setItem(31, STATUS_EFFECT_TOOLTIP);
    }

    public ItemStack skillTreeButton() {
        ItemStack skillTreeButton = new ItemStack(Material.PAPER);
        ItemMeta meta = skillTreeButton.getItemMeta();
        if (meta == null) return skillTreeButton;
        meta.setDisplayName(ChatColor.GREEN + "Open Skill Trees");
        String lore = ChatColor.GRAY + "Open the skill trees for the " +
                ChatColor.GREEN + RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(player) +
                ChatColor.GRAY + " class! Earn skill points by leveling-up " +
                "and spend them on unique and powerful perks!";
        meta.setLore(ChatUtils.formattedText(lore));
        skillTreeButton.setItemMeta(meta);
        return skillTreeButton;
    }
}