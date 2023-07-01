package com.runicrealms.plugin.loot;

public abstract class TimedLoot {

    private TimedLootChest lootChest;

    public TimedLoot(TimedLootChest lootChest) {
        this.lootChest = lootChest;
    }

    public TimedLootChest getLootChest() {
        return this.lootChest;
    }

}
