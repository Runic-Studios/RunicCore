package com.runicrealms.plugin.item.commands;

import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.command.supercommands.RunicGiveSC;
import com.runicrealms.plugin.enums.ArmorSlotEnum;
import com.runicrealms.plugin.item.ItemNameGenerator;
import com.runicrealms.plugin.item.LoreGenerator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ItemCMD implements SubCommand {

    private RunicGiveSC giveItemSC;

    public ItemCMD(RunicGiveSC giveItemSC) {
        this.giveItemSC = giveItemSC;
    }

    @Override
    public void onConsoleCommand(CommandSender sender, String[] args)  {

        // runicgive item [player] [itemType] [tier] ([x] [y] [z])
        ItemNameGenerator nameGen = new ItemNameGenerator();

        String name = nameGen.generateName(ItemNameGenerator.NameTier.valueOf(args[3].toUpperCase()));
        if (name == null) return;

        Player pl = Bukkit.getPlayer(args[1]);
        if (pl == null) return;
        String itemType = args[2];

        ChatColor color = ChatColor.GRAY;
        switch (args[3].toLowerCase()) {
            case "uncommon":
                color = ChatColor.GREEN;
                break;
            case "rare":
                color = ChatColor.AQUA;
                break;
            case "epic":
                color = ChatColor.LIGHT_PURPLE;
                break;
            case "legendary":
                color = ChatColor.GOLD;
                break;
        }
        String itemTypeName = "";
        Material material = Material.STICK;
        int durability = 0;
        String itemSlot = "";
        Random random = new Random();
        int randomNum = random.nextInt(5) + 1;
        if (itemType.toLowerCase().equals("armor")) {
            int type = random.nextInt(4) + 1;
            switch (type) {
                case 1:
                    itemType = "helmet";
                    break;
                case 2:
                    itemType = "chestplate";
                    break;
                case 3:
                    itemType = "leggings";
                    break;
                case 4:
                    itemType = "boots";
                    break;
            }
        }
        switch (itemType.toLowerCase()) {
            case "helmet":
                material = Material.SHEARS;
                itemSlot = "head";
                switch (randomNum) {
                    case 1:
                        durability = 5;
                        itemTypeName = "Hood";
                    break;
                    case 2:
                        durability = 10;
                        itemTypeName = "Cowl";
                        break;
                    case 3:
                        durability = 15;
                        itemTypeName = "Coif";
                        break;
                    case 4:
                        durability = 20;
                        itemTypeName = "Crown";
                        break;
                    case 5:
                        durability = 25;
                        itemTypeName = "Helm";
                        break;
                }
                break;
            case "chestplate":
                itemSlot = "chest";
                switch (randomNum) {
                    case 1:
                        material = Material.DIAMOND_CHESTPLATE;
                        itemTypeName = "Robe";
                        break;
                    case 2:
                        material = Material.LEATHER_CHESTPLATE;
                        itemTypeName = "Tunic";
                        break;
                    case 3:
                        material = Material.CHAINMAIL_CHESTPLATE;
                        itemTypeName = "Chest";
                        break;
                    case 4:
                        material = Material.GOLDEN_CHESTPLATE;
                        itemTypeName = "Chestplate";
                        break;
                    case 5:
                        material = Material.IRON_CHESTPLATE;
                        itemTypeName = "Chestplate";
                        break;
                }
                break;
            case "leggings":
                itemSlot = "legs";
                switch (randomNum) {
                    case 1:
                        material = Material.DIAMOND_LEGGINGS;
                        itemTypeName = "Legs";
                        break;
                    case 2:
                        material = Material.LEATHER_LEGGINGS;
                        itemTypeName = "Chaps";
                        break;
                    case 3:
                        material = Material.CHAINMAIL_LEGGINGS;
                        itemTypeName = "Tassets";
                        break;
                    case 4:
                        material = Material.GOLDEN_LEGGINGS;
                        itemTypeName = "Platelegs";
                        break;
                    case 5:
                        material = Material.IRON_LEGGINGS;
                        itemTypeName = "Platelegs";
                        break;
                }
                break;
            case "boots":
                itemSlot = "feet";
                switch (randomNum) {
                    case 1:
                        material = Material.DIAMOND_BOOTS;
                        itemTypeName = "Boots";
                        break;
                    case 2:
                        material = Material.LEATHER_BOOTS;
                        itemTypeName = "Boots";
                        break;
                    case 3:
                        material = Material.CHAINMAIL_BOOTS;
                        itemTypeName = "Greaves";
                        break;
                    case 4:
                        material = Material.GOLDEN_BOOTS;
                        itemTypeName = "Boots";
                        break;
                    case 5:
                        material = Material.IRON_BOOTS;
                        itemTypeName = "Boots";
                        break;
                }
                break;
            case "gemstone":
                switch (randomNum) {
                    case 1:
                        material = Material.REDSTONE;
                        break;
                    case 2:
                        material = Material.LAPIS_LAZULI;
                        break;
                    case 3:
                        material = Material.EMERALD;
                        break;
                    case 4:
                        material = Material.QUARTZ;
                        break;
                    case 5:
                        material = Material.DIAMOND;
                        break;
                }
                break;
            default:
                pl.sendMessage(ChatColor.DARK_RED + "Please specify correct input: helmet, chestplate, leggings, boots, or gemstone");
                break;
        }

        ItemStack craftedItem = new ItemStack(material);
        ItemMeta meta = craftedItem.getItemMeta();
        ((Damageable) Objects.requireNonNull(meta)).setDamage(durability);
        craftedItem.setItemMeta(meta);

        switch (args[3].toLowerCase()) {
            case "common":
                craftedItem = generateCommonStats(craftedItem, itemSlot, material);
                break;
            case "uncommon":
                craftedItem = generateUncommonStats(craftedItem, itemSlot, material);
                break;
            case "rare":
                craftedItem = generateRareStats(craftedItem, itemSlot, material);
                break;
            case "epic":
                craftedItem = generateEpicStats(craftedItem, itemSlot, material);
                break;
        }

        LoreGenerator.generateItemLore(craftedItem, color, name + " " + itemTypeName, "");

        // check that the player has an open inventory space
        // this method prevents items from stacking if the player crafts 5

        // quests, or directly in inventory
        if (args.length == 4) {
            if (pl.getInventory().firstEmpty() != -1) {
                int firstEmpty = pl.getInventory().firstEmpty();
                pl.getInventory().setItem(firstEmpty, craftedItem);
            } else {
                pl.getWorld().dropItem(pl.getLocation(), craftedItem);
            }

        // mob drops
        } else if (args.length == 7) {
            Location loc = new Location(pl.getWorld(), Double.parseDouble(args[4]), Double.parseDouble(args[5]), Double.parseDouble(args[6]));
            pl.getWorld().dropItem(loc, craftedItem);
        }
    }

    private ItemStack generateCommonStats(ItemStack item, String itemSlot, Material material) {

        Random rand = new Random();
        int numOfStats = rand.nextInt(2) + 1;

        int maxHealth = 0;
        int maxMana = 0;
        String className = determineClass(item, material);
        switch (className.toLowerCase()) {
            case "mage":
                maxHealth = 4;
                maxMana = 10;
                break;
            case "rogue":
                maxHealth = 5;
                maxMana = 7;
                break;
            case "archer":
                maxHealth = 5;
                maxMana = 7;
                break;
            case "cleric":
                maxHealth = 7;
                maxMana = 7;
                break;
            case "warrior":
                maxHealth = 10;
                maxMana = 7;
                break;
        }

        int health = rand.nextInt(maxHealth) + 1;
        int mana = rand.nextInt(maxMana) + 1;

        //List<Integer> stats = determineWhichStats(numOfStats, 2); // 1 or 2

        item = AttributeUtil.addGenericStat(item, "generic.maxHealth", health, itemSlot);

        if (numOfStats == 1) {
            item = AttributeUtil.addCustomStat(item, "custom.manaBoost", mana);
        }

        return item;
    }

    private ItemStack generateUncommonStats(ItemStack item, String itemSlot, Material material) {

        item = AttributeUtil.addCustomStat(item, "required.level", 10);

        Random rand = new Random();
        int numOfStats = rand.nextInt(2) + 1;

        int maxHealth = 0;
        int maxMana = 0;
        int weapDamage = 2;
        String className = determineClass(item, material);
        switch (className.toLowerCase()) {
            case "mage":
                maxHealth = 6;
                maxMana = 15;
                break;
            case "rogue":
                maxHealth = 7;
                maxMana = 12;
                break;
            case "archer":
                maxHealth = 7;
                maxMana = 12;
                break;
            case "cleric":
                maxHealth = 10;
                maxMana = 12;
                break;
            case "warrior":
                maxHealth = 15;
                maxMana = 12;
                break;
        }

        int health = rand.nextInt(maxHealth) + 4;
        int mana = rand.nextInt(maxMana) + 7;
        int meleeDamage = rand.nextInt(weapDamage) + 1;

        List<Integer> stats = determineWhichStats(numOfStats, 3); // 1 or 2 stats in addition to health


        item = AttributeUtil.addGenericStat(item, "generic.maxHealth", health, itemSlot);

        if (stats.contains(2)) {
            item = AttributeUtil.addCustomStat(item, "custom.manaBoost", mana);
        }
        if (stats.contains(3)) {
                item = AttributeUtil.addCustomStat(item, "custom.attackDamage", meleeDamage);
        }

        return item;
    }

    private ItemStack generateRareStats(ItemStack item, String itemSlot, Material material) {

        item = AttributeUtil.addCustomStat(item, "required.level", 20);

        // item can be socketed once
        item = AttributeUtil.addCustomStat(item, "custom.socketCount", 1);

        Random rand = new Random();
        //int numOfStats = rand.nextInt(2) + 1;

        int maxHealth = 0;
        int maxMana = 0;
        int weapDamage = 2;
        int maxHealing = 8;
        int maxSpellDamage = 4;
        String className = determineClass(item, material);
        switch (className.toLowerCase()) {
            case "mage":
                maxHealth = 10;
                maxMana = 30;
                maxSpellDamage = 6;
                break;
            case "rogue":
                maxHealth = 12;
                maxMana = 20;
                break;
            case "archer":
                maxHealth = 12;
                maxMana = 20;
                break;
            case "cleric":
                maxHealth = 16;
                maxMana = 20;
                break;
            case "warrior":
                maxHealth = 25;
                maxMana = 20;
                break;
        }

        int health = rand.nextInt(maxHealth) + 6;
        int mana = rand.nextInt(maxMana) + 12;
        int healing = rand.nextInt(maxHealing) + 1;
        int spellDamage = rand.nextInt(maxSpellDamage) + 1;

        List<Integer> stats = determineWhichStats(2, 5);

      //  if (stats.contains(1)) {
            item = AttributeUtil.addGenericStat(item, "generic.maxHealth", health, itemSlot);
       // }
        if (stats.contains(2)) {
            item = AttributeUtil.addCustomStat(item, "custom.manaBoost", mana);
        }
        if (stats.contains(3)) {
            item = AttributeUtil.addCustomStat(item, "custom.attackDamage", weapDamage);
        }
        if (stats.contains(4)) {
            item = AttributeUtil.addCustomStat(item, "custom.healingBoost", healing);
        }
        if (stats.contains(5)) {
            item = AttributeUtil.addCustomStat(item, "custom.magicDamage", spellDamage);
        }


        return item;
    }

    private ItemStack generateEpicStats(ItemStack item, String itemSlot, Material material) {

        item = AttributeUtil.addCustomStat(item, "required.level", 30);

        // item can be socketed once
        item = AttributeUtil.addCustomStat(item, "custom.socketCount", 1);

        Random rand = new Random();
        //int numOfStats = rand.nextInt(2) + 1;

        int maxHealth = 0;
        int maxMana = 0;
        int weapDamage = 3;
        int maxHealing = 12;
        int maxSpellDamage = 8;
        String className = determineClass(item, material);
        switch (className.toLowerCase()) {
            case "mage":
                maxHealth = 12;
                maxMana = 35;
                maxSpellDamage = 10;
                break;
            case "rogue":
                maxHealth = 15;
                maxMana = 25;
                break;
            case "archer":
                maxHealth = 15;
                maxMana = 25;
                break;
            case "cleric":
                maxHealth = 35;
                maxMana = 25;
                break;
            case "warrior":
                maxHealth = 40;
                maxMana = 25;
                break;
        }

        int health = rand.nextInt(maxHealth) + 10;
        int mana = rand.nextInt(maxMana) + 20;
        int healing = rand.nextInt(maxHealing) + 8;
        int spellDamage = rand.nextInt(maxSpellDamage) + 1;

        List<Integer> stats = determineWhichStats(2, 5);

       // if (stats.contains(1)) {
            item = AttributeUtil.addGenericStat(item, "generic.maxHealth", health, itemSlot);
      //  }
        if (stats.contains(2)) {
            item = AttributeUtil.addCustomStat(item, "custom.manaBoost", mana);
        }
        if (stats.contains(3)) {
            item = AttributeUtil.addCustomStat(item, "custom.attackDamage", weapDamage);
        }
        if (stats.contains(4)) {
            item = AttributeUtil.addCustomStat(item, "custom.healingBoost", healing);
        }
        if (stats.contains(5)) {
            item = AttributeUtil.addCustomStat(item, "custom.magicDamage", spellDamage);
        }


        return item;
    }

    private String determineClass(ItemStack item, Material material) {

        String className = "";
        if (material == Material.SHEARS) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                int durability = ((Damageable) meta).getDamage();

                switch (durability) {
                    case 5:
                        className = "mage";
                        break;
                    case 10:
                        className = "rogue";
                        break;
                    case 15:
                        className = "archer";
                        break;
                    case 20:
                        className = "cleric";
                        break;
                    case 25:
                        className = "warrior";
                        break;
                }
            }
        } else if (material == Material.DIAMOND_CHESTPLATE
                || material == Material.DIAMOND_LEGGINGS
                || material == Material.DIAMOND_BOOTS) {
            className = "mage";
        } else if (material == Material.LEATHER_CHESTPLATE
                || material == Material.LEATHER_LEGGINGS
                || material == Material.LEATHER_BOOTS) {
            className = "rogue";
        } else if (material == Material.CHAINMAIL_CHESTPLATE
                || material == Material.CHAINMAIL_LEGGINGS
                || material == Material.CHAINMAIL_BOOTS) {
            className = "archer";
        } else if (material == Material.GOLDEN_CHESTPLATE
                || material == Material.GOLDEN_LEGGINGS
                || material == Material.GOLDEN_BOOTS) {
            className = "cleric";
        } else if (material == Material.IRON_CHESTPLATE
                || material == Material.IRON_LEGGINGS
                || material == Material.IRON_BOOTS) {
            className = "warrior";
        }


        return className;
    }

    private List<Integer> determineWhichStats(int totalNumOfStats, int statUpTo) {

        // ex: if statUpTo is 3, it will add 1, 2, and 3, corresponding to +health, +mana, and +healing (order of gemstones)
        List<Integer> howManyDiffStatsDoWeHave = new ArrayList<>();
        for (int i = 1; i <= statUpTo; i++) {
            howManyDiffStatsDoWeHave.add(i);
        }

        Random rand = new Random();

        // create a temporary list for storing
        // selected element
        List<Integer> newList = new ArrayList<>();
        for (int i = 0; i < totalNumOfStats; i++) {

            // take a raundom index between 0 to size
            // of given List
            int bound = howManyDiffStatsDoWeHave.size() - 1;
            if (bound <= 0) {
                bound = 1;
            }
            int randomIndex = rand.nextInt(bound) + 1;

            // add element in temporary list
            newList.add(howManyDiffStatsDoWeHave.get(randomIndex));

            // Remove selected element from orginal list
            howManyDiffStatsDoWeHave.remove(randomIndex);
        }
        return newList;
    }

    @Override
    public void onOPCommand(Player sender, String[] args) {

        if(args.length == 4) {
            this.onConsoleCommand(sender, args);
        } else if (args.length == 7) {
            this.onConsoleCommand(sender, args);
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Command usage: /giveitem generator [itemType] [tier] ([x] [y] [z])");
        }
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {
    }

    @Override
    public String permissionLabel() {
        return "runic.generateitem";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
        //return TabCompleteUtil.getPlayers(commandSender, strings, RunicCore.getInstance());
    }
}
