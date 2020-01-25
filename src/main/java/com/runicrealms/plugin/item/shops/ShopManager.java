package com.runicrealms.plugin.item.shops;

import org.bukkit.entity.Player;

import java.util.*;

public class ShopManager {

    private HashMap<Player, Shop> shops;

    public ShopManager() {
        this.shops = new HashMap<>();
    }

    public Shop getPlayerShop(Player pl) {
        return this.shops.get(pl);
    }
    public void setPlayerShop(Player pl, Shop shop) {
        this.shops.put(pl, shop);
    }
}
