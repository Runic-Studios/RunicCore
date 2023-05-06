package com.runicrealms.plugin.item.shops;

import org.bukkit.entity.Player;

import java.util.function.Predicate;

@FunctionalInterface
public interface ShopCondition extends Predicate<Player> {
}
