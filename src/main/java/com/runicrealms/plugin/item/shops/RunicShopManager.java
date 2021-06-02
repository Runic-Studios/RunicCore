package com.runicrealms.plugin.item.shops;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class RunicShopManager {

    private final HashMap<Player, RunicShop> shops;

    public RunicShopManager() {
        this.shops = new HashMap<>();
    }

    public RunicShop getPlayerShop(Player pl) {
        return this.shops.get(pl);
    }
    public void setPlayerShop(Player pl, RunicShop shop) {
        this.shops.put(pl, shop);
    }
}
