package com.runicrealms.plugin.loot;

import io.lumine.mythic.bukkit.MythicBukkit;

public class BossTimedLoot extends TimedLoot {

    private final String mmBossID;
    private final double lootDamageThreshold; // percentage, 0 means don't track damage

    public BossTimedLoot(TimedLootChest lootChest, String mmBossID, double lootDamageThreshold) {
        super(lootChest);
        this.mmBossID = mmBossID;
        try (MythicBukkit mythicBukkit = MythicBukkit.inst()) {
            if (mythicBukkit.getAPIHelper().getMythicMob(mmBossID) == null)
                throw new IllegalArgumentException("Boss timed loot has invalid MM ID: " + mmBossID);
        }
        this.lootDamageThreshold = lootDamageThreshold;
    }

    public String getMmBossID() {
        return this.mmBossID;
    }

    public double getLootDamageThreshold() {
        return this.lootDamageThreshold;
    }

}
