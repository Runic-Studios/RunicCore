package com.runicrealms.plugin.loot;

public class CustomTimedLoot extends TimedLoot { // Timed loot chests that are only spawn-able through commands

    private final String identifier;

    public CustomTimedLoot(TimedLootChest lootChest, String identifier) {
        super(lootChest);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return this.identifier;
    }

}
