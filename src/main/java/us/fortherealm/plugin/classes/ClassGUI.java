package us.fortherealm.plugin.classes;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.item.LoreGenerator;
import us.fortherealm.plugin.attributes.AttributeUtil;
import us.fortherealm.plugin.scoreboard.ScoreboardHandler;

import java.util.ArrayList;

import static org.bukkit.Color.*;

public class ClassGUI implements InventoryProvider {

    // globals
    public static final SmartInventory CLASS_SELECTION = SmartInventory.builder()
            .id("classSelection")
            .provider(new ClassGUI())
            .size(1, 9)
            .title(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Choose Your Class!")
            .build();

    private ScoreboardHandler sbh = Main.getScoreboardHandler();

    @Override
    public void init(Player player, InventoryContents contents) {

        // select archer
        contents.set(0, 0, ClickableItem.of
                (menuItem(Material.BOW,
                        ChatColor.GREEN,
                        "Archer",
                        "An agile, long-range artillary.",
                        "Barrage"),
                e -> {
                        setupArtifact(player, "Archer");
                        setupRune(player);
                        setConfig(player, "Archer");
                        player.closeInventory();
                }));

        // select cleric
        contents.set(0, 2, ClickableItem.of
                (menuItem(Material.WOODEN_SHOVEL,
                        ChatColor.AQUA,
                        "Cleric",
                        "A versatile support and healer.",
                        "Rejuvenate"),
                        e -> {
                            setupArtifact(player, "Cleric");
                            setupRune(player);
                            setConfig(player, "Cleric");
                            player.closeInventory();
                        }));

        // select mage
        contents.set(0, 4, ClickableItem.of
                (menuItem(Material.WOODEN_HOE,
                        ChatColor.LIGHT_PURPLE,
                        "Mage",
                        "A powerful, ranged nuker.",
                        "Arcane Spike"),
                        e -> {
                            setupArtifact(player, "Mage");
                            setupRune(player);
                            setConfig(player, "Mage");
                            player.closeInventory();
                        }));

        // select rogue
        contents.set(0, 6, ClickableItem.of
                (menuItem(Material.WOODEN_SWORD,
                        ChatColor.YELLOW,
                        "Rogue",
                        "A cunning, close-range duelist.",
                        "Backstab"),
                        e -> {
                            setupArtifact(player, "Rogue");
                            setupRune(player);
                            setConfig(player, "Rogue");
                            player.closeInventory();
                        }));

        // select warrior
        contents.set(0, 8, ClickableItem.of
                (menuItem(Material.WOODEN_AXE,
                        ChatColor.RED,
                        "Warrior",
                        "A durable, close-range fighter.",
                        "Deliverance"),
                        e -> {
                            setupArtifact(player, "Warrior");
                            setupRune(player);
                            setConfig(player, "Warrior");
                            player.closeInventory();
                        }));
    }

    // used for animated inventories
    @Override
    public void update(Player player, InventoryContents contents) {
    }

    // creates the visual menu
    private ItemStack menuItem(Material material, ChatColor color, String displayName, String description, String spell) {

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
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
    private void setupArtifact(Player player, String className) {

        // grab our variables
        String itemName = "";
        Material material = Material.STICK;
        String spell = "null";

        // build class-specific variables
        switch (className) {
            case "Archer":
                itemName = "Stiff Oaken Shortbow";
                material = Material.BOW;
                spell = "Barrage";
                launchFirework(player, LIME);
                player.sendTitle(
                        ChatColor.DARK_GREEN + "You selected",
                        ChatColor.GREEN + className + "!", 10, 40, 10);
                break;
            case "Cleric":
                itemName = "Initiate's Oaken Mace";
                material = Material.WOODEN_SHOVEL;
                spell = "Rejuvenate";
                launchFirework(player, AQUA);
                player.sendTitle(
                        ChatColor.DARK_AQUA + "You selected",
                        ChatColor.AQUA + className + "!", 10, 40, 10);
                break;
            case "Mage":
                itemName = "Sturdy Oaken Branch";
                material = Material.WOODEN_HOE;
                launchFirework(player, FUCHSIA);
                spell = "Blizzard";
                player.sendTitle(
                        ChatColor.DARK_PURPLE + "You selected",
                        ChatColor.LIGHT_PURPLE + className + "!", 10, 40, 10);
                break;
            case "Rogue":
                itemName = "Oaken Sparring Sword";
                material = Material.WOODEN_SWORD;
                spell = "Smoke Bomb";
                launchFirework(player, YELLOW);
                player.sendTitle(
                        ChatColor.GOLD + "You selected",
                        ChatColor.YELLOW + className + "!", 10, 40, 10);
                break;
            case "Warrior":
                itemName = "Worn Oaken Battleaxe";
                material = Material.WOODEN_AXE;
                spell = "Charge";
                launchFirework(player, RED);
                player.sendTitle(
                        ChatColor.DARK_RED + "You selected",
                        ChatColor.RED + className + "!", 10, 40, 10);
                break;
        }

        // create the artifact
        ItemStack artifact = new ItemStack(material);

        // add default spells
        artifact = AttributeUtil.addSpell(artifact, "primarySpell", spell);
        artifact = AttributeUtil.addSpell(artifact, "secondarySpell", ChatColor.RED + "LOCKED");

        // --------------------------------------------------------------------------------------------------------
        // add default damage, attack speed values
        // multiply by 2 for standard defences, subtract one because some weapons have base 1 damage.
        switch (className) {
            case "Archer":
                artifact = AttributeUtil.addCustomStat(artifact, "custom.bowSpeed", -23.25);
                artifact = AttributeUtil.addCustomStat(artifact, "custom.minDamage", 2);
                artifact = AttributeUtil.addCustomStat(artifact, "custom.maxDamage", 4);
                break;
            case "Cleric":
                artifact = AttributeUtil.addGenericStat(artifact, "generic.attackSpeed", -23.4, "mainhand");
                artifact = AttributeUtil.addCustomStat(artifact, "custom.minDamage", 3);
                artifact = AttributeUtil.addCustomStat(artifact, "custom.maxDamage", 8);
                break;
            case "Mage":
                artifact = AttributeUtil.addGenericStat(artifact, "generic.attackSpeed", -23.4, "mainhand");
                artifact = AttributeUtil.addCustomStat(artifact, "custom.minDamage", 3);
                artifact = AttributeUtil.addCustomStat(artifact, "custom.maxDamage", 5);
                break;
            case "Rogue":
                artifact = AttributeUtil.addGenericStat(artifact, "generic.attackSpeed", -23.1, "mainhand");
                artifact = AttributeUtil.addCustomStat(artifact, "custom.minDamage", 3);
                artifact = AttributeUtil.addCustomStat(artifact, "custom.maxDamage", 5);
                break;
            case "Warrior":
                artifact = AttributeUtil.addGenericStat(artifact, "generic.attackSpeed", -23.25, "mainhand");
                artifact = AttributeUtil.addCustomStat(artifact, "custom.minDamage", 4);
                artifact = AttributeUtil.addCustomStat(artifact, "custom.maxDamage", 6);
                break;
        }
        // --------------------------------------------------------------------------------------------------------

        // generate our lore
        LoreGenerator.generateArtifactLore(artifact, ChatColor.YELLOW, itemName, className, 0);

        // set the player's artifact
        player.getInventory().setItem(0, artifact);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
    }

    // creates the player's rune
    private void setupRune(Player player) {
        ItemStack rune = new ItemStack(Material.POPPED_CHORUS_FRUIT);
        rune = AttributeUtil.addSpell(rune, "primarySpell", ChatColor.RED + "LOCKED");
        rune = AttributeUtil.addSpell(rune, "secondarySpell", ChatColor.RED + "LOCKED");
        LoreGenerator.generateRuneLore(rune);
        player.getInventory().setItem(1, rune);
    }

    private void setConfig(Player player, String className) {
        Main.getInstance().getConfig().set(player.getUniqueId() + ".info.class.name", className);
        Main.getInstance().getConfig().set(player.getUniqueId() + ".info.class.level", 0);
        Main.getInstance().saveConfig();
        Main.getInstance().reloadConfig();
        sbh.updatePlayerInfo(player);
        sbh.updateSideInfo(player);
    }

    private void launchFirework(Player p, Color color) {
        Firework firework = p.getWorld().spawn(p.getEyeLocation(), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.setPower(0);
        meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(color).build());
        firework.setFireworkMeta(meta);
    }
}
