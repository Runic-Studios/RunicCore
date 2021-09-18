package com.runicrealms.plugin.item.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemUtils {

    /**
     * Returns a skinned head
     *
     * @param value from minecraft-heads.com
     * @return head item stack for use in menus
     */
    public static ItemStack getHead(String value) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        UUID hashAsId = new UUID(value.hashCode(), value.hashCode());
        return Bukkit.getUnsafe().modifyItemStack(skull,
                "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + value + "\"}]}}}"
        );
    }
}
