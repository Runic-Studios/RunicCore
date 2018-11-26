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
import us.fortherealm.plugin.scoreboard.ScoreboardHandler;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.caster.itemstack.CasterItemStack;
import us.fortherealm.plugin.skills.skilltypes.archer.defensive.Parry;
import us.fortherealm.plugin.skills.skilltypes.archer.offensive.Barrage;
import us.fortherealm.plugin.skills.skilltypes.cleric.defensive.Rejuvenate;
import us.fortherealm.plugin.skills.skilltypes.cleric.defensive.Windstride;
import us.fortherealm.plugin.skills.skilltypes.mage.offensive.ArcaneSpike;
import us.fortherealm.plugin.skills.skilltypes.mage.offensive.Discharge;
import us.fortherealm.plugin.skills.skilltypes.rogue.offensive.Backstab;
import us.fortherealm.plugin.skills.skilltypes.runic.defensive.Blink;
import us.fortherealm.plugin.skills.skilltypes.runic.defensive.Heal;
import us.fortherealm.plugin.skills.skilltypes.runic.defensive.Speed;
import us.fortherealm.plugin.skills.skilltypes.runic.offensive.Fireball;
import us.fortherealm.plugin.skills.skilltypes.runic.offensive.Frostbolt;
import us.fortherealm.plugin.skills.skilltypes.warrior.defensive.Deliverance;
import us.fortherealm.plugin.skills.skilltypes.warrior.offensive.Enrage;

import java.util.ArrayList;
import java.util.Arrays;

import static org.bukkit.Color.*;

public class SelectClass implements InventoryProvider {

    public static final SmartInventory CLASS_SELECTION = SmartInventory.builder()
            .id("classSelection")
            .provider(new SelectClass())
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
                        setupRune(player, "Archer");
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
                            setupRune(player, "Cleric");
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
                            setupRune(player, "Mage");
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
                            setupRune(player, "Rogue");
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
                            setupRune(player, "Warrior");
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

        String itemName = "";
        Material material = Material.STICK;
        Skill primary = new Fireball();
        Skill secondary = new Fireball();

        switch (className) {
            case "Archer":
                itemName = "Stiff Oaken Shortbow";
                material = Material.BOW;
                primary = new Barrage();
                secondary = new Parry();
                launchFirework(player, LIME);
                player.sendTitle(
                        ChatColor.DARK_GREEN + "You selected",
                        ChatColor.GREEN + className + "!", 10, 40, 10);
                break;
            case "Cleric":
                itemName = "Initiate's Oaken Mace";
                material = Material.WOODEN_SHOVEL;
                primary = new Rejuvenate();
                secondary = new Windstride();
                launchFirework(player, AQUA);
                player.sendTitle(
                        ChatColor.DARK_AQUA + "You selected",
                        ChatColor.AQUA + className + "!", 10, 40, 10);
                break;
            case "Mage":
                itemName = "Sturdy Oaken Branch";
                material = Material.WOODEN_HOE;
                primary = new ArcaneSpike();
                secondary = new Discharge();
                launchFirework(player, FUCHSIA);
                player.sendTitle(
                        ChatColor.DARK_PURPLE + "You selected",
                        ChatColor.LIGHT_PURPLE + className + "!", 10, 40, 10);
                break;
            case "Rogue":
                itemName = "Oaken Sparring Sword";
                material = Material.WOODEN_SWORD;
                primary = new Backstab();
                launchFirework(player, YELLOW);
                player.sendTitle(
                        ChatColor.GOLD + "You selected",
                        ChatColor.YELLOW + className + "!", 10, 40, 10);
                break;
            case "Warrior":
                itemName = "Worn Oaken Battleaxe";
                material = Material.WOODEN_AXE;
                primary = new Deliverance();
                secondary = new Enrage();
                launchFirework(player, RED);
                player.sendTitle(
                        ChatColor.DARK_RED + "You selected",
                        ChatColor.RED + className + "!", 10, 40, 10);
                break;
        }

        CasterItemStack artifact = new CasterItemStack(
                new ItemStack(material),
                itemName, CasterItemStack.ItemType.ARTIFACT,
                Arrays.asList(primary), 1,
                Arrays.asList(secondary), 2);

        ItemMeta meta = artifact.getItemMeta();
        meta.setUnbreakable(true);
        artifact.setItemMeta(meta);
        player.getInventory().setItem(0, artifact);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
    }

    private void setupRune(Player player, String className) {

        Skill primary = new Fireball();
        Skill secondary = new Fireball();

        switch (className) {
            case "Archer":
                primary = new Frostbolt();
                secondary = new Speed();
                break;
            case "Cleric":
                primary = new Frostbolt();
                secondary = new Heal();
                break;
            case "Mage":
                primary = new Fireball();
                secondary = new Blink();
                break;
            case "Rogue":
                primary = new Frostbolt();
                secondary = new Speed();
                break;
            case "Warrior":
                primary = new Fireball();
                secondary = new Frostbolt();
                break;
        }

        // create the player's rune
        CasterItemStack rune = new CasterItemStack(
                new ItemStack(Material.POPPED_CHORUS_FRUIT),
                "Ancient Rune", CasterItemStack.ItemType.RUNE,
                Arrays.asList(primary), 1,
                Arrays.asList(secondary), 2);

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
    }

    private void launchFirework(Player p, Color color) {
        Firework firework = p.getWorld().spawn(p.getEyeLocation(), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.setPower(0);
        meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(color).build());
        firework.setFireworkMeta(meta);
    }
}
