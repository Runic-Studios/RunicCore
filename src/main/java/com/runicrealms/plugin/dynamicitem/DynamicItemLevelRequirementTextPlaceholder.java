package com.runicrealms.plugin.dynamicitem;

import com.runicrealms.plugin.runicitems.dynamic.DynamicItemTextPlaceholder;
import com.runicrealms.plugin.runicitems.item.LevelRequirementHolder;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Replaces <level> on items with an X or a checkmark
 */
public class DynamicItemLevelRequirementTextPlaceholder extends DynamicItemTextPlaceholder {

    public DynamicItemLevelRequirementTextPlaceholder() {
        super("level");
    }

    @Nullable
    @Override
    public String generateReplacement(Player viewer, ItemStack item, NBTItem itemNBT, RunicItemTemplate template) {
        if (!(template instanceof LevelRequirementHolder levelRequirementHolder)) return null;

        if (viewer.getLevel() >= levelRequirementHolder.getLevel()) {
            return DynamicItemManager.CHECKMARK;
        } else {
            return DynamicItemManager.X;
        }
    }

}
