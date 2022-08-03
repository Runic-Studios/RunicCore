package com.runicrealms.plugin.classes.utilities;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.classes.SubClass;
import org.bukkit.entity.Player;

public class SubClassUtil {

    /**
     * Determines the appropriate subclass based on player class and specified position
     *
     * @param position (which sub-class? 1, 2, or 3)
     */
    public static SubClass determineSubClass(Player player, int position) {
        SubClass subClass = null;
        switch (RunicCoreAPI.getPlayerClass(player)) {
            case "Archer":
                if (position == 1)
                    subClass = SubClass.MARKSMAN;
                else if (position == 2)
                    subClass = SubClass.SCOUT;
                else
                    subClass = SubClass.WARDEN;
                break;
            case "Cleric":
                if (position == 1)
                    subClass = SubClass.BARD;
                else if (position == 2)
                    subClass = SubClass.PALADIN;
                else
                    subClass = SubClass.PRIEST;
                break;
            case "Mage":
                if (position == 1)
                    subClass = SubClass.CRYOMANCER;
                else if (position == 2)
                    subClass = SubClass.PYROMANCER;
                else
                    subClass = SubClass.WARLOCK;
                break;
            case "Rogue":
                if (position == 1)
                    subClass = SubClass.ASSASSIN;
                else if (position == 2)
                    subClass = SubClass.DUELIST;
                else
                    subClass = SubClass.SWINDLER;
                break;
            case "Warrior":
                if (position == 1)
                    subClass = SubClass.BERSERKER;
                else if (position == 2)
                    subClass = SubClass.GUARDIAN;
                else
                    subClass = SubClass.INQUISITOR;
                break;
        }
        return subClass;
    }
}
