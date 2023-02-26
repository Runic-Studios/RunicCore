package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;

@SuppressWarnings("deprecation")
public class FeatheredFriend extends Spell {
    private static final int COOLDOWN = 6;
    private static final int MAX_ENEMIES = 3;
    private static final double DURATION = 1.0D;
//    private final Map<UUID, ShoulderParrot> parrotMap = new ConcurrentHashMap<>();

    public FeatheredFriend() {
        super("Feathered Friend",
                "While above 50% max HP, your parrot companion joins you! " +
                        "While your parrot is active, your basic attacks deal 10% more " +
                        "damage, and any damage you deal from your spells " +
                        "also silences enemies for " + (int) DURATION + "s!",
                ChatColor.WHITE, CharacterClass.ROGUE, 0, 0);
        this.setIsPassive(true);
//        startParrotTask();
    }

}

