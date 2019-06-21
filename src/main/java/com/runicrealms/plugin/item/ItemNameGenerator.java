package com.runicrealms.plugin.item;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Random;

public class ItemNameGenerator {

    private Random random;
    private YamlConfiguration yaml;

    public ItemNameGenerator () {
        File itemPrefixes = new File(Bukkit.getServer().getPluginManager().getPlugin("RunicCore").getDataFolder(), "item_prefixes.yml");
        yaml = YamlConfiguration.loadConfiguration(itemPrefixes);
        random = new Random();
    }

    public enum NameTier {

        COMMON("Common"),
        UNCOMMON("Uncommon"),
        RARE("Rare"),
        EPIC("Epic"),
        LEGENDARY("Legendary");

        String value;
        NameTier (String value) {
            this.value = value;
        }

        public String toValue() {
            return value.charAt(0) + name().substring(1).toLowerCase();
        }
    }

    private List<String> getNames(NameTier tier) {
        return yaml.getStringList("Prefixes." + tier.toValue());
    }

    /**
     * This method returns a random prefix from the item_prefixes.yml file, just enter a tier!
     */
    public String generateName(NameTier tier) {
        return getNames(tier).get(random.nextInt(getNames(tier).size()));
    }
}
