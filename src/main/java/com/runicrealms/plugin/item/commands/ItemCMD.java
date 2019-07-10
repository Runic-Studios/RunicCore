package com.runicrealms.plugin.item.commands;

import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.command.supercommands.RunicGiveSC;
import com.runicrealms.plugin.item.ItemNameGenerator;
import com.runicrealms.plugin.item.LoreGenerator;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@SuppressWarnings("FieldCanBeLocal")
public class ItemCMD implements SubCommand {

    private RunicGiveSC giveItemSC;

    public ItemCMD(RunicGiveSC giveItemSC) {
        this.giveItemSC = giveItemSC;
    }

    @Override
    public void onConsoleCommand(CommandSender sender, String[] args)  {

        // runicgive item [player] [itemType] [tier] ([x] [y] [z])
        // runicgive item [player] [potion] [type] [someVar]
        ItemNameGenerator nameGen = new ItemNameGenerator();

        String name = "";
        if (!args[2].equals("gemstone") && !args[2].equals("potion")) {
            name = nameGen.generateName(ItemNameGenerator.NameTier.valueOf(args[3].toUpperCase()));
            if (name == null) return;
        }

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
            case "potion":
                ItemStack potion = generatePotion(args[3], Integer.parseInt(args[4]));
                // check that the player has an open inventory space
                // this method prevents items from stacking if the player crafts 5
                if (pl.getInventory().firstEmpty() != -1) {
                    int firstEmpty = pl.getInventory().firstEmpty();
                    pl.getInventory().setItem(firstEmpty, potion);
                } else {
                    pl.getWorld().dropItem(pl.getLocation(), potion);
                }
                return;
            default:
                pl.sendMessage(ChatColor.DARK_RED + "Please specify correct input: helmet, chestplate, leggings, boots, gemstone, or potion");
                break;
        }

        ItemStack craftedItem = new ItemStack(material);
        ItemMeta meta = craftedItem.getItemMeta();
        ((Damageable) Objects.requireNonNull(meta)).setDamage(durability);
        craftedItem.setItemMeta(meta);

        switch (args[3].toLowerCase()) {
            case "common":
                craftedItem = generateCommonItem();
                break;
            case "uncommon":
                craftedItem = generateUncommonItem(craftedItem, itemSlot, material);
                break;
            case "rare":
                craftedItem = generateRareItem(craftedItem, itemSlot, material);
                break;
            case "epic":
                craftedItem = generateEpicItem(craftedItem, itemSlot, material);
                break;
        }

        //LoreGenerator.generateItemLore(craftedItem, color, name + " " + itemTypeName, "");

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

    public static ItemStack generateCommonItem() {

        Random rand = new Random();

        String itemType = "";
        int type = rand.nextInt(4) + 1;
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

        // setup info for item
        String itemTypeName = "";
        Material material = Material.STICK;
        int durability = 0;
        String itemSlot = "";

        int randomNum = rand.nextInt(5) + 1;
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
        }

        ItemStack commonItem = new ItemStack(material);
        ItemMeta meta = commonItem.getItemMeta();
        ((Damageable) Objects.requireNonNull(meta)).setDamage(durability);
        commonItem.setItemMeta(meta);

        int numOfStats = rand.nextInt(2) + 1;

        int maxHealth = 0;
        int maxMana = 0;
        String className = determineClass(commonItem, material);
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

        commonItem = AttributeUtil.addGenericStat(commonItem, "generic.maxHealth", health, itemSlot);

        if (numOfStats == 1) {
            commonItem = AttributeUtil.addCustomStat(commonItem, "custom.manaBoost", mana);
        }

        ItemNameGenerator nameGen = new ItemNameGenerator();
        String name = nameGen.generateName(ItemNameGenerator.NameTier.valueOf("COMMON"));
        LoreGenerator.generateItemLore(commonItem, ChatColor.GRAY, name + " " + itemTypeName, "");

        return commonItem;
    }

    private ItemStack generateUncommonItem(ItemStack item, String itemSlot, Material material) {

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

        int health = rand.nextInt(maxHealth-4) + 4;
        int mana = rand.nextInt(maxMana-7) + 7;
        int meleeDamage = rand.nextInt(weapDamage-1) + 1;

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

    private ItemStack generateRareItem(ItemStack item, String itemSlot, Material material) {

        item = AttributeUtil.addCustomStat(item, "required.level", 20);

        // item can be socketed once
        item = AttributeUtil.addCustomStat(item, "custom.socketCount", 1);

        Random rand = new Random();
        //int numOfStats = rand.nextInt(2) + 1;

        int maxHealth = 0;
        int maxMana = 0;
        int weapDamage = 2;
        int maxHealing = 3;
        int maxSpellDamage = 3;
        String className = determineClass(item, material);
        switch (className.toLowerCase()) {
            case "mage":
                maxHealth = 10;
                maxMana = 30;
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

        int health = rand.nextInt(maxHealth-6) + 6;
        int mana = rand.nextInt(maxMana-12) + 12;
        int healing = rand.nextInt(maxHealing-1) + 1;
        int spellDamage = rand.nextInt(maxSpellDamage-1) + 1;

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

    private ItemStack generateEpicItem(ItemStack item, String itemSlot, Material material) {

        item = AttributeUtil.addCustomStat(item, "required.level", 30);

        // item can be socketed once
        item = AttributeUtil.addCustomStat(item, "custom.socketCount", 1);

        Random rand = new Random();
        //int numOfStats = rand.nextInt(2) + 1;

        int maxHealth = 0;
        int maxMana = 0;
        int weapDamage = 3;
        int maxHealing = 6;
        int maxSpellDamage = 6;
        String className = determineClass(item, material);
        switch (className.toLowerCase()) {
            case "mage":
                maxHealth = 12;
                maxMana = 35;
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

        int health = rand.nextInt(maxHealth-10) + 10;
        int mana = rand.nextInt(maxMana-20) + 20;
        int healing = rand.nextInt(maxHealing-3) + 3;
        int spellDamage = rand.nextInt(maxSpellDamage-3) + 3;

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

    private static String determineClass(ItemStack item, Material material) {

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

        if(args.length == 4 || args.length == 5 || args.length == 7) {
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

    /**
     * Taken from Alchemy profession
     * @param type display name of the item, accepts color codes
     * @param someVar whatever variable the potion takes goes here (health, mana, duration. the system knows)
     */
    public static ItemStack generatePotion(String type, int someVar) {

        String dispName = "";
        switch (type) {
            case "healing":
                dispName = "&cPotion of Healing";
                break;
            case "mana":
                dispName = "&3Potion of Mana";
                break;
            case "slaying":
                dispName = "&b&oPotion of Slaying";
                break;
            case "looting":
                dispName = "&6Potion of Looting";
                break;
        }


        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta pMeta = (PotionMeta) potion.getItemMeta();
        Color color;
        String desc;
        if (dispName.toLowerCase().contains("healing")) {
            color = Color.RED;
            desc = "\n&eRestores &c" + someVar + "❤ &eon use";
        } else if (dispName.toLowerCase().contains("mana")) {
            color = Color.AQUA;
            desc = "\n&eRestores &3" + someVar + "✸ &eon use";
        } else if (dispName.toLowerCase().contains("slaying")) {
            color = Color.BLACK;
            desc = "\n&eIncreases spellʔ and weapon⚔ damage" +
                    "\n&evs. monsters by &f20% &efor &f" + someVar + " &eminutes";
        } else {
            color = Color.ORANGE;
            desc = "\n&eIncreases looting chance by &f20%" +
                    "\n&efor &f" + someVar + " &eminutes";
        }
        Objects.requireNonNull(pMeta).setColor(color);

        pMeta.setDisplayName(ColorUtil.format(dispName));
        ArrayList<String> lore = new ArrayList<>();
        for (String s : desc.split("\n")) {
            lore.add(ColorUtil.format(s));
        }
        lore.add("");
        lore.add(ColorUtil.format("&7Consumable"));
        pMeta.setLore(lore);

        pMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        pMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        pMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        pMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        potion.setItemMeta(pMeta);

        // ----------------------------------------------
        // must be set AFTER meta is set
        if (dispName.toLowerCase().contains("healing")) {
            potion = AttributeUtil.addCustomStat(potion, "potion.healing", someVar);
        } else if (dispName.toLowerCase().contains("mana")) {
            potion = AttributeUtil.addCustomStat(potion, "potion.mana", someVar);
        } else if (dispName.toLowerCase().contains("slaying")) {
            potion = AttributeUtil.addCustomStat(potion, "potion.slaying", someVar);
        } else {
            potion = AttributeUtil.addCustomStat(potion, "potion.looting", someVar);
        }
        // ----------------------------------------------


//        // check that the player has an open inventory space
//        // this method prevents items from stacking if the player crafts 5
//        if (pl.getInventory().firstEmpty() != -1) {
//            int firstEmpty = pl.getInventory().firstEmpty();
//            pl.getInventory().setItem(firstEmpty, potion);
//        } else {
//            pl.getWorld().dropItem(pl.getLocation(), potion);
//        }
        return potion;
    }
}
