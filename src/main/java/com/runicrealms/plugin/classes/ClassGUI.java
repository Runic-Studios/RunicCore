package com.runicrealms.plugin.classes;

import com.runicrealms.plugin.classes.utilities.ClassUtil;
import com.runicrealms.plugin.item.LoreGenerator;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.scoreboard.ScoreboardHandler;

import java.util.ArrayList;

import static org.bukkit.Color.*;

public class ClassGUI implements InventoryProvider {

    // base attack speed, damage values for each class' artifact
    // max attack speed is 24.0, so 24+(-23.0) = 1.0 attack speed
    // bows use a different NBT tag.
    private static final double archerBaseBowSpeed = -23.25;
    private static final double clericBaseSpeed = -23.4;
    private static final double mageBaseSpeed = -23.4;
    private static final double rogueBaseSpeed = -22.8;
    private static final double warriorBaseSpeed = -23.25;

    // this inventory cannot be closed!
    public static final SmartInventory CLASS_SELECTION = SmartInventory.builder()
            .id("classSelection")
            .provider(new ClassGUI())
            .size(4, 9)
            .title(ChatColor.GREEN + "" + ChatColor.BOLD + "Choose your class!")
            .closeable(false)
            .build();

    private ScoreboardHandler sbh = RunicCore.getScoreboardHandler();

    @Override
    public void init(Player player, InventoryContents contents) {

        // select archer
        contents.set(1, 2, ClickableItem.of
                (menuItem(Material.BOW,
                        ChatColor.GREEN,
                        "Archer",
                        "An agile, long-range artillary.",
                        "Barrage"),
                        e -> {
                            setupPlayer(player, "Archer", contents);
                }));

        // select cleric
        contents.set(1, 4, ClickableItem.of
                (menuItem(Material.IRON_SHOVEL,
                        ChatColor.AQUA,
                        "Cleric",
                        "A versatile support and healer.",
                        "Rejuvenate"),
                        e -> {
                            setupPlayer(player, "Cleric", contents);
                        }));

        // select mage
        contents.set(1, 6, ClickableItem.of
                (menuItem(Material.IRON_HOE,
                        ChatColor.LIGHT_PURPLE,
                        "Mage",
                        "A powerful, ranged nuker.",
                        "Blizzard"),
                        e -> {
                            setupPlayer(player, "Mage", contents);
                        }));

        // select rogue
        contents.set(2, 3, ClickableItem.of
                (menuItem(Material.IRON_SWORD,
                        ChatColor.YELLOW,
                        "Rogue",
                        "A cunning, close-range duelist.",
                        "Smoke Bomb"),
                        e -> {
                            setupPlayer(player, "Rogue", contents);
                        }));

        // select warrior
        contents.set(2, 5, ClickableItem.of
                (menuItem(Material.IRON_AXE,
                        ChatColor.RED,
                        "Warrior",
                        "A durable, close-range fighter.",
                        "Charge"),
                        e -> {
                            setupPlayer(player, "Warrior", contents);
                        }));
    }

    private void setupPlayer(Player pl, String className, InventoryContents contents) {
        setupArtifact(pl, className, false);
        setupRune(pl);
        setupHearthstone(pl);
        setConfig(pl, className);
        contents.inventory().close(pl);
        sbh.updatePlayerInfo(pl);
        sbh.updateSideInfo(pl);
    }

    // used for animated inventories
    @Override
    public void update(Player player, InventoryContents contents) {
    }

    // creates the visual menu
    private ItemStack menuItem(Material material, ChatColor color, String displayName, String description, String spell) {

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (material == Material.BOW) {
            ((Damageable) meta).setDamage(10);
        }
        meta.setDisplayName(color + displayName);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "-> Select this class");
        lore.add(ChatColor.GRAY + ""); // blank line
        lore.add(ChatColor.GRAY + "Info:");
        lore.add(ChatColor.GOLD + description);
        lore.add(ChatColor.GRAY + ""); // blank line
        lore.add(ChatColor.GRAY + "Starter Spell:");
        lore.add(ChatColor.GRAY + " - " + color + spell);
        meta.setLore(lore);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    // sets the player artifact
    public static void setupArtifact(Player player, String className, boolean isTutorial) {

        // grab our variables
        String itemName = "";
        Material material = Material.STICK;
        String spell = "null";

        // build class-specific variables
        Color color = WHITE;
        switch (className.toLowerCase()) {
            case "archer":
                itemName = "Stiff Oaken Shortbow";
                material = Material.BOW;
                spell = "Barrage";
                color = LIME;
                if (isTutorial) {
                    player.sendTitle(
                            ChatColor.GOLD + "Try",
                            ChatColor.YELLOW + "Left-Click!", 10, 100, 10);
                    player.sendMessage(ChatColor.GOLD + "Try " + ChatColor.YELLOW + "Left-Click " + ChatColor.GRAY + "to cast barrage!");
                } else {
                    player.sendTitle(
                            ChatColor.DARK_GREEN + "You selected",
                            ChatColor.GREEN + "Archer!", 10, 40, 10);
                }
                break;
            case "cleric":
                itemName = "Initiate's Oaken Mace";
                material = Material.WOODEN_SHOVEL;
                spell = "Rejuvenate";
                color = AQUA;
                if (isTutorial) {
                    player.sendTitle(
                            ChatColor.GOLD + "Try",
                            ChatColor.YELLOW + "Sneak + Left-Click!", 10, 100, 10);
                    player.sendMessage(ChatColor.GOLD + "Try " + ChatColor.YELLOW + "Sneak + Left-Click " + ChatColor.GRAY + "to cast rejuvenate!");
                } else {
                    player.sendTitle(
                            ChatColor.DARK_AQUA + "You selected",
                            ChatColor.AQUA + "Cleric!", 10, 40, 10);
                }
                break;
            case "mage":
                itemName = "Sturdy Oaken Branch";
                material = Material.WOODEN_HOE;
                color = FUCHSIA;
                spell = "Blizzard";
                if (isTutorial) {
                    player.sendTitle(
                            ChatColor.GOLD + "Try",
                            ChatColor.YELLOW + "Sneak + Left-Click!", 10, 100, 10);
                    player.sendMessage(ChatColor.GOLD + "Try " + ChatColor.YELLOW + "Sneak + Left-Click " + ChatColor.GRAY + "to cast blizzard!");
                } else {
                    player.sendTitle(
                            ChatColor.DARK_PURPLE + "You selected",
                            ChatColor.LIGHT_PURPLE + "Mage!", 10, 40, 10);
                }
                break;
            case "rogue":
                itemName = "Oaken Sparring Sword";
                material = Material.WOODEN_SWORD;
                spell = "Smoke Bomb";
                color = YELLOW;
                if (isTutorial) {
                    player.sendTitle(
                            ChatColor.GOLD + "Try",
                            ChatColor.YELLOW + "Sneak + Left-Click!", 10, 100, 10);
                    player.sendMessage(ChatColor.GOLD + "Try " + ChatColor.YELLOW + "Sneak + Left-Click " + ChatColor.GRAY + "to cast smoke bomb!");
                } else {
                    player.sendTitle(
                            ChatColor.GOLD + "You selected",
                            ChatColor.YELLOW + "Rogue!", 10, 40, 10);
                }
                break;
            case "warrior":
                itemName = "Worn Oaken Battleaxe";
                material = Material.WOODEN_AXE;
                spell = "Charge";
                color = RED;
                if (isTutorial) {
                    player.sendTitle(
                            ChatColor.GOLD + "Try",
                            ChatColor.YELLOW + "Sneak + Left-Click!", 10, 100, 10);
                    player.sendMessage(ChatColor.GOLD + "Try " + ChatColor.YELLOW + "Sneak + Left-Click " + ChatColor.GRAY + "to cast charge!");
                } else {
                    player.sendTitle(
                            ChatColor.DARK_RED + "You selected",
                            ChatColor.RED + "Warrior!", 10, 40, 10);
                }
                break;
        }
        if (!isTutorial) ClassUtil.launchFirework(player, color);

        // create the artifact
        ItemStack artifact = new ItemStack(material);

        // add default spells, souldbound
        artifact = AttributeUtil.addSpell(artifact, "primarySpell", spell);
        artifact = AttributeUtil.addSpell(artifact, "secondarySpell", ChatColor.RED + "LOCKED");
        artifact = AttributeUtil.addCustomStat(artifact, "soulbound", "true");

        // --------------------------------------------------------------------------------------------------------
        // add default damage, attack speed values
        // multiply by 2 for standard defences, subtract one because some weapons have base 1 damage.
        switch (className) {
            case "Archer":
                artifact = AttributeUtil.addCustomStat(artifact, "custom.bowSpeed", archerBaseBowSpeed);
                artifact = AttributeUtil.addCustomStat(artifact, "custom.minDamage", 3);
                artifact = AttributeUtil.addCustomStat(artifact, "custom.maxDamage", 5);
                break;
            case "Cleric":
                artifact = AttributeUtil.addGenericStat(artifact, "generic.attackSpeed", clericBaseSpeed, "mainhand");
                artifact = AttributeUtil.addCustomStat(artifact, "custom.minDamage", 3);
                artifact = AttributeUtil.addCustomStat(artifact, "custom.maxDamage", 8);
                break;
            case "Mage":
                artifact = AttributeUtil.addGenericStat(artifact, "generic.attackSpeed", mageBaseSpeed, "mainhand");
                artifact = AttributeUtil.addCustomStat(artifact, "custom.minDamage", 3);
                artifact = AttributeUtil.addCustomStat(artifact, "custom.maxDamage", 5);
                break;
            case "Rogue":
                artifact = AttributeUtil.addGenericStat(artifact, "generic.attackSpeed", rogueBaseSpeed, "mainhand");
                artifact = AttributeUtil.addCustomStat(artifact, "custom.minDamage", 4);
                artifact = AttributeUtil.addCustomStat(artifact, "custom.maxDamage", 6);
                break;
            case "Warrior":
                artifact = AttributeUtil.addGenericStat(artifact, "generic.attackSpeed", warriorBaseSpeed, "mainhand");
                artifact = AttributeUtil.addCustomStat(artifact, "custom.minDamage", 4);
                artifact = AttributeUtil.addCustomStat(artifact, "custom.maxDamage", 6);
                break;
        }
        // --------------------------------------------------------------------------------------------------------

        // generate our lore
        LoreGenerator.generateArtifactLore(artifact, itemName, className, 0);

        // set the player's artifact
        player.getInventory().setItem(0, artifact);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
    }

    // creates the player's rune
    public static void setupRune(Player pl) {
        ItemStack rune = new ItemStack(Material.POPPED_CHORUS_FRUIT);
        rune = AttributeUtil.addSpell(rune, "primarySpell", ChatColor.RED + "LOCKED");
        rune = AttributeUtil.addSpell(rune, "secondarySpell", ChatColor.RED + "LOCKED");
        rune = AttributeUtil.addCustomStat(rune, "soulbound", "true");
        LoreGenerator.generateRuneLore(rune);
        pl.getInventory().setItem(1, rune);
    }

    public static void setupHearthstone(Player pl) {
        ItemStack hearthstone = new ItemStack(Material.CLAY_BALL);
        hearthstone = AttributeUtil.addCustomStat(hearthstone, "location", "Tutorial Island");
        hearthstone = AttributeUtil.addCustomStat(hearthstone, "soulbound", "true");
        LoreGenerator.generateHearthstoneLore(hearthstone);
        pl.getInventory().setItem(2, hearthstone);
    }

    public static void setConfig(Player player, String className) {
        HealthUtils.setBaseHealth(player);
        HealthUtils.setHeartDisplay(player);
        player.setLevel(0);
        player.setExp(0);
        RunicCore.getInstance().getConfig().set(player.getUniqueId() + ".info.class.name", className);
        RunicCore.getInstance().getConfig().set(player.getUniqueId() + ".info.class.level", 0);
        RunicCore.getInstance().saveConfig();
        RunicCore.getInstance().reloadConfig();
    }

    public static double getArcherBaseBowSpeed() {
        return archerBaseBowSpeed;
    }

    public static double getClericBaseSpeed() {
        return clericBaseSpeed;
    }

    public static double getMageBaseSpeed() {
        return mageBaseSpeed;
    }

    public static double getRogueBaseSpeed() {
        return rogueBaseSpeed;
    }

    public static double getWarriorBaseSpeed() {
        return warriorBaseSpeed;
    }
}
