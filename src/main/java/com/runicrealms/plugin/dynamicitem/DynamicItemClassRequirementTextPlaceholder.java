package com.runicrealms.plugin.dynamicitem;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.model.CorePlayerData;
import com.runicrealms.plugin.rdb.event.CharacterSelectEvent;
import com.runicrealms.plugin.runicitems.dynamic.DynamicItemTextPlaceholder;
import com.runicrealms.plugin.runicitems.item.ClassRequirementHolder;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.plugin.runicitems.item.util.RunicItemClass;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Replaces <class> on items with an X or a check mark
 */
public class DynamicItemClassRequirementTextPlaceholder extends DynamicItemTextPlaceholder implements Listener {

    /*
    We use our own cached classes here instead of RunicDatabase.getAPI().getCharacterAPI().getCharacterClass for two reasons:
    1) That method makes calls to a concurrent hash map which we would end up locking out hundreds of times per tick for no reason
    2) To prevent conversions between CharacterClass and RunicItemClass every replacement generation
     */
    private final Map<UUID, RunicItemClass> cachedClasses = new HashMap<>();

    public DynamicItemClassRequirementTextPlaceholder() {
        super("class");
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    @Override
    public @Nullable String generateReplacement(Player viewer, ItemStack item, NBTItem itemNBT, RunicItemTemplate template) {
        RunicItemClass viewerClass = cachedClasses.get(viewer.getUniqueId());
        if (viewerClass == null) return null;

        if (!(template instanceof ClassRequirementHolder classRequirementHolder)) return null;

        RunicItemClass runicItemClass = classRequirementHolder.getRunicClass();
        if (runicItemClass != viewerClass) {
            return DynamicItemManager.X;
        } else {
            return DynamicItemManager.CHECKMARK;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST) // Make sure our session data mongo is good
    public void onCharacterSelect(CharacterSelectEvent event) { // Run on character select so that we are prepared to modify items on character load
        // We need to directly access coreplayerdata instead of using RDB methods because those are only initialized on character load
        CorePlayerData corePlayerData = ((CorePlayerData) event.getSessionDataMongo());
        CharacterClass characterClass = corePlayerData.getCharacter(event.getSlot()).getClassType();
        RunicItemClass itemClass = RunicItemClass.getFromIdentifier(characterClass.getName());
        cachedClasses.put(event.getPlayer().getUniqueId(), itemClass);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cachedClasses.remove(event.getPlayer().getUniqueId());
    }

}
