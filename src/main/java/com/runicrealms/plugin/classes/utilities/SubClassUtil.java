package com.runicrealms.plugin.classes.utilities;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.classes.SubClassEnum;
import org.bukkit.entity.Player;

public class SubClassUtil {

    /**
     * Determines the appropriate subclass based on player class and specified position
     *
     * @param position (which sub-class? 1, 2, or 3)
     */
    public static SubClassEnum determineSubClass(Player player, int position) {
        SubClassEnum subClassEnum = null;
        switch (RunicCoreAPI.getPlayerClass(player)) {
            case "archer":
                if (position == 1)
                    subClassEnum = SubClassEnum.MARKSMAN;
                else if (position == 2)
                    subClassEnum = SubClassEnum.SCOUT;
                else
                    subClassEnum = SubClassEnum.WARDEN;
                break;
            case "cleric":
                if (position == 1)
                    subClassEnum = SubClassEnum.BARD;
                else if (position == 2)
                    subClassEnum = SubClassEnum.PALADIN;
                else
                    subClassEnum = SubClassEnum.PRIEST;
                break;
            case "mage":
                if (position == 1)
                    subClassEnum = SubClassEnum.CRYOMANCER;
                else if (position == 2)
                    subClassEnum = SubClassEnum.PYROMANCER;
                else
                    subClassEnum = SubClassEnum.WARLOCK;
                break;
            case "rogue":
                if (position == 1)
                    subClassEnum = SubClassEnum.ASSASSIN;
                else if (position == 2)
                    subClassEnum = SubClassEnum.DUELIST;
                else
                    subClassEnum = SubClassEnum.SWINDLER;
                break;
            case "warrior":
                if (position == 1)
                    subClassEnum = SubClassEnum.BERSERKER;
                else if (position == 2)
                    subClassEnum = SubClassEnum.GUARDIAN;
                else
                    subClassEnum = SubClassEnum.INQUISITOR;
                break;
        }
        return subClassEnum;
    }
}
