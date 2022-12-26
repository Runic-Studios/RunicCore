package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * There is a missing design pattern here for handling more complex shops. Somebody should yell at me later.
 *
 * @author Skyfallin
 */
public class RunicDungeonShop {

    private final int dungeonArmorPrice;
    private final int dungeonArtifactPrice;
    private final String dungeonCurrencyTemplateId;
    private final String[] artifactTemplateIdList;
    private final String armorTemplateIdPrefix;
    private static final String[] armorClassPrefixList = CharacterClass.getClassNames();
    private static final String[] armorTypes = new String[]{"helm", "chest", "leggings", "boots"};

    /**
     * Builds a RunicDungeonShop which can be used with .buildRunicShopGeneric to reduce boilerplate
     *
     * @param dungeonArmorPrice         price of each dungeon armor piece
     * @param dungeonArtifactPrice      price of each dungeon artifact
     * @param dungeonCurrencyTemplateId which RunicItem is the currency (dungeon tokens)
     * @param artifactTemplateIdList    an array of artifact template ids
     * @param armorTemplateIdPrefix     the prefix of each template id (i.e., sebaths-cave)
     */
    public RunicDungeonShop(int dungeonArmorPrice, int dungeonArtifactPrice, String dungeonCurrencyTemplateId,
                            @Nullable String[] artifactTemplateIdList, String armorTemplateIdPrefix) {
        this.dungeonArmorPrice = dungeonArmorPrice;
        this.dungeonArtifactPrice = dungeonArtifactPrice;
        this.dungeonCurrencyTemplateId = dungeonCurrencyTemplateId;
        this.artifactTemplateIdList = artifactTemplateIdList;
        this.armorTemplateIdPrefix = armorTemplateIdPrefix;
    }

    /**
     * Builds a RunicShopGeneric object using the additional dungeon shop information
     *
     * @param size        of the RunicShopGeneric
     * @param shopName    of the RunicShopGeneric
     * @param runicNpcIds to trigger the RunicShopGeneric
     * @return a RunicShopGeneric
     */
    public RunicShopGeneric buildRunicShopGeneric(int size, String shopName, Collection<Integer> runicNpcIds) {
        LinkedHashSet<RunicShopItem> dungeonShopItems = new LinkedHashSet<>();
        if (artifactTemplateIdList != null) {
            for (String artifactTemplateId : artifactTemplateIdList) {
                ItemStack itemStack = RunicItemsAPI.generateItemFromTemplate(artifactTemplateId).generateItem();
                Map<String, Integer> requiredItems = new HashMap<String, Integer>() {{
                    put(dungeonCurrencyTemplateId, dungeonArtifactPrice);
                }};
                dungeonShopItems.add(new RunicShopItem(requiredItems, itemStack));
            }
        }
        for (String armorClassPrefix : armorClassPrefixList) {
            for (String armorType : armorTypes) {
                String templateId = armorTemplateIdPrefix + "-" + armorClassPrefix + "-" + armorType; // sebaths-cave-archer-helm
                ItemStack itemStack = RunicItemsAPI.generateItemFromTemplate(templateId).generateItem();
                Map<String, Integer> requiredItems = new HashMap<String, Integer>() {{
                    put(dungeonCurrencyTemplateId, dungeonArmorPrice);
                }};
                dungeonShopItems.add(new RunicShopItem
                        (
                                requiredItems,
                                itemStack
                        ));
            }
        }
        return new RunicShopGeneric
                (
                        size,
                        shopName,
                        runicNpcIds,
                        dungeonShopItems,
                        artifactTemplateIdList != null
                                ?
                                new int[]{0, 1, 2, 3, 4, 9, 18, 27, 36, 10, 19, 28, 37, 11, 20, 29, 38, 12, 21, 30, 39, 13, 22, 31, 40}
                                :
                                new int[]{0, 9, 18, 27, 1, 10, 19, 28, 2, 11, 20, 29, 3, 12, 21, 30, 4, 13, 22, 31});
    }
}
