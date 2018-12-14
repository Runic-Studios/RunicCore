package us.fortherealm.plugin.classes;

import de.tr7zw.itemnbtapi.NBTItem;
import de.tr7zw.itemnbtapi.NBTList;
import de.tr7zw.itemnbtapi.NBTListCompound;
import de.tr7zw.itemnbtapi.NBTType;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.artifact.LoreGenerator;
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

    private ScoreboardHandler sbh = new ScoreboardHandler();

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
        double attSpeed = -23.0;
        double damage = 5.0;
        double bowSpeed = 1.0;

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
                attSpeed = 0.0;
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
                spell = "Arcane Spike";
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

        // --------------------------------------------------------------------------------------------------------

        // create the artifact
        ItemStack artifact = new ItemStack(material);

        // add default spells
        artifact = AttributeUtil.addSpell(artifact, "primarySpell", spell);
        artifact = AttributeUtil.addSpell(artifact, "secondarySpell", ChatColor.RED + "LOCKED");

        // add default damage, attack speed values
        // multiply by 2 for standard defences, subtract one because all weapons have base 1 damage.
        switch (className) {
            case "Archer":
                // store custom bow attributes, set melee damage to 0
                artifact = AttributeUtil.addStat(artifact, "custom.bowSpeed", bowSpeed);
                artifact = AttributeUtil.addStat(artifact, "custom.bowDamage", (damage * 2) - 1);
                artifact = AttributeUtil.addStat(artifact, "generic.attackDamage", 0);
                break;
            case "Cleric":
                artifact = AttributeUtil.addStat(artifact, "generic.attackSpeed", attSpeed);
                artifact = AttributeUtil.addStat(artifact, "generic.attackDamage", (damage * 2) - 1);
                break;
            case "Mage":
                // store custom staff attributes, set melee damage to 0
                artifact = AttributeUtil.addStat(artifact, "generic.attackSpeed", attSpeed);
                artifact = AttributeUtil.addStat(artifact, "custom.staffDamage", (damage * 2) - 1);
                artifact = AttributeUtil.addStat(artifact, "generic.attackDamage", 0);
                break;
            case "Rogue":
                artifact = AttributeUtil.addStat(artifact, "generic.attackSpeed", attSpeed);
                artifact = AttributeUtil.addStat(artifact, "generic.attackDamage", (damage * 2) - 1);
                break;
            case "Warrior":
                artifact = AttributeUtil.addStat(artifact, "generic.attackSpeed", attSpeed);
                artifact = AttributeUtil.addStat(artifact, "generic.attackDamage", (damage * 2) - 1);
                break;
        }
        // --------------------------------------------------------------------------------------------------------

        // save our attributes, since we'll have to re-apply them after we update the meta
        NBTItem nbtiOld = new NBTItem(artifact);
        NBTList oldAttributes = nbtiOld.getList("AttributeModifiers", NBTType.NBTTagCompound);

        // generate our lore (which destroys our custom attributes, oh goodie -__-)
        LoreGenerator.generateArtifactLore(artifact, ChatColor.YELLOW, itemName, className, 0);

        // create a new list of attributes
        NBTItem nbtiNew = new NBTItem(artifact);
        NBTList newAttributes = nbtiNew.getList("AttributeModifiers", NBTType.NBTTagCompound);

        // add the spells back
        nbtiNew.setString("primarySpell", spell);
        nbtiNew.setString("secondarySpell", ChatColor.RED + "LOCKED");


        // add all the attributes back, and since generic attributes are automatically saved, we add a duplicate check
        for (int i = 0; i < oldAttributes.size(); i++) {
            NBTListCompound oldStat = oldAttributes.getCompound(i);
            if (!(oldStat.getString("Name").startsWith("generic"))) {
                NBTListCompound newStat = newAttributes.addCompound();
                newStat.setDouble("Amount", oldStat.getDouble("Amount"));
                newStat.setString("AttributeName", oldStat.getString("AttributeName"));
                newStat.setString("Name", oldStat.getString("Name"));
                newStat.setInteger("Operation", 0);
                newStat.setInteger("UUIDLeast", 59764);
                newStat.setInteger("UUIDMost", 31483);
                artifact = nbtiNew.getItem();
            }
        }

        // set the player's artifact. we're done.
        player.getInventory().setItem(0, artifact);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
    }

    // creates the player's rune
    private void setupRune(Player player) {

        String primary = ChatColor.WHITE + "Left click§7: " + ChatColor.RED + "SLOT LOCKED";
        String secondary = ChatColor.WHITE + "Right click§7: " + ChatColor.RED + "SLOT LOCKED";

        // grab our variables
        ItemStack rune = new ItemStack(Material.POPPED_CHORUS_FRUIT);
        ItemMeta meta = rune.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Ancient Rune");
        lore.add("");
        lore.add(ChatColor.GREEN + "Spells:");
        lore.add(primary);
        lore.add(secondary);
        lore.add("");
        lore.add(ChatColor.WHITE + "Click §7this item to open the editor.");
        lore.add("");
        lore.add(ChatColor.LIGHT_PURPLE + "Rune");

        // set the player's rune
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.setLore(lore);
        rune.setItemMeta(meta);
        player.getInventory().setItem(1, rune);
    }

    private void setConfig(Player player, String className) {
        // todo: save player exp on logout
        player.setLevel(1);
        Main.getInstance().getConfig().set(player.getUniqueId() + ".info.class", className);
        Main.getInstance().saveConfig();
        Main.getInstance().reloadConfig();
        sbh.updatePlayerInfo(player);
        sbh.updateSideInfo(player);
        sbh.updateHealthbar(player);
    }

    private void launchFirework(Player p, Color color) {
        Firework firework = p.getWorld().spawn(p.getEyeLocation(), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.setPower(0);
        meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(color).build());
        firework.setFireworkMeta(meta);
    }
}
