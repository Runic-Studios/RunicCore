package com.runicrealms.plugin.dynamicitem;

import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import org.bukkit.ChatColor;

public class DynamicItemManager {

    public static final String X = ChatColor.RED + "✘";
    public static final String CHECKMARK = ChatColor.GREEN + "✔";

    public DynamicItemManager() {
        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemClassRequirementTextPlaceholder());
        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemLevelRequirementTextPlaceholder());
    }

}
