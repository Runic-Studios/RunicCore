package com.runicrealms.plugin.classes;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.util.ItemUtils;
import com.runicrealms.plugin.model.SkillTreePosition;
import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.Jedis;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public enum SubClass {

    /*
     archer
     */
    MARKSMAN("Marksman", CharacterClass.ARCHER, marksmanItem(), "Marksman is a master of single-target damage!"),
    RANGER("Ranger", CharacterClass.ARCHER, rangerItem(), "Ranger controls the battlefield with crowd control!"),
    SCOUT("Scout", CharacterClass.ARCHER, scoutItem(), "Scout eludes enemies with mobility!"),
    /*
     cleric
     */
    BARD("Bard", CharacterClass.CLERIC, bardItem(), "Bard controls the flow of battle with ally buffs and enemy debuffs!"),
    PALADIN("Paladin", CharacterClass.CLERIC, paladinItem(), "Paladin is a hybrid fighter and healer!"),
    PRIEST("Priest", CharacterClass.CLERIC, priestItem(), "Priest heals allies and keeps them strong!"),
    /*
     mage
     */
    ARCANIST("Arcanist", CharacterClass.MAGE, arcanistItem(), "Arcanist has utility and buffs to out-maneuver opponents!"),
    CRYOMANCER("Cryomancer", CharacterClass.MAGE, cryomancerItem(), "Cryomancer freezes and slows enemies with crowd control!"),
    PYROMANCER("Pyromancer", CharacterClass.MAGE, pyromancerItem(), "Pyromancer deals powerful area-of-effect damage!"),
    /*
     rogue
     */
    ASSASSIN("Assassin", CharacterClass.ROGUE, assassinItem(), "Assassin emerges from the shadows to quickly burst an opponent!"),
    DUELIST("Duelist", CharacterClass.ROGUE, duelistItem(), "Duelist is an excellent fighter against a single opponent!"),
    PIRATE("Pirate", CharacterClass.ROGUE, pirateItem(), "Pirate uses projectiles to control the battle!"),
    /*
    warrior
     */
    BERSERKER("Berserker", CharacterClass.WARRIOR, berserkerItem(), "Berserker fights ferociously with abilities that cleave enemies!"),
    GUARDIAN("Guardian", CharacterClass.WARRIOR, guardianItem(), "Guardian excels as a tank, defending allies!"),
    INQUISITOR("Inquisitor", CharacterClass.WARRIOR, inquisitorItem(), "Inquisitor uses crowd control to disable enemies!");

    public static final Set<SubClass> ARCHER_SUBCLASSES = new LinkedHashSet<>();
    public static final Set<SubClass> CLERIC_SUBCLASSES = new LinkedHashSet<>();
    public static final Set<SubClass> MAGE_SUBCLASSES = new LinkedHashSet<>();
    public static final Set<SubClass> ROGUE_SUBCLASSES = new LinkedHashSet<>();
    public static final Set<SubClass> WARRIOR_SUBCLASSES = new LinkedHashSet<>();

    static {
        for (SubClass subClass : SubClass.values()) {
            switch (subClass.getBaseClass()) {
                case ANY:
                    break;
                case ARCHER:
                    ARCHER_SUBCLASSES.add(subClass);
                    break;
                case CLERIC:
                    CLERIC_SUBCLASSES.add(subClass);
                    break;
                case MAGE:
                    MAGE_SUBCLASSES.add(subClass);
                    break;
                case ROGUE:
                    ROGUE_SUBCLASSES.add(subClass);
                    break;
                case WARRIOR:
                    WARRIOR_SUBCLASSES.add(subClass);
                    break;
            }
        }
    }

    private final String name;
    private final CharacterClass baseClass;
    private final ItemStack itemStack;
    private final String description;

    SubClass(String name, CharacterClass baseClass, ItemStack itemStack, String description) {
        this.name = name;
        this.baseClass = baseClass;
        this.itemStack = itemStack;
        this.description = description;
    }

    /**
     * Determines the appropriate subclass based on player class and specified position
     *
     * @param position (which sub-class? 1, 2, or 3)
     * @return the SubClass enum, or null if not found
     */
    public static SubClass determineSubClass(String playerClass, SkillTreePosition position) {
        SubClass subClass;
        int value = position.getValue();

        switch (playerClass.toLowerCase()) {
            case "archer":
                if (value == 1)
                    subClass = SubClass.MARKSMAN;
                else if (value == 2)
                    subClass = SubClass.RANGER;
                else
                    subClass = SubClass.SCOUT;
                break;
            case "cleric":
                if (value == 1)
                    subClass = SubClass.BARD;
                else if (value == 2)
                    subClass = SubClass.PALADIN;
                else
                    subClass = SubClass.PRIEST;
                break;
            case "mage":
                if (value == 1)
                    subClass = SubClass.ARCANIST;
                else if (value == 2)
                    subClass = SubClass.CRYOMANCER;
                else
                    subClass = SubClass.PYROMANCER;
                break;
            case "rogue":
                if (value == 1)
                    subClass = SubClass.ASSASSIN;
                else if (value == 2)
                    subClass = SubClass.DUELIST;
                else
                    subClass = SubClass.PIRATE;
                break;
            case "warrior":
                if (value == 1)
                    subClass = SubClass.BERSERKER;
                else if (value == 2)
                    subClass = SubClass.GUARDIAN;
                else
                    subClass = SubClass.INQUISITOR;
                break;
            default:
                return null;
        }
        return subClass;
    }

    /**
     * Overloaded method for use when player is offline (uses redis)
     *
     * @param uuid          of the player to check
     * @param characterSlot of the character to check
     * @param position      of the skill tree (1, 2, etc.)
     * @param jedis         the jedis resource
     * @return their subclass, or null (bad!)
     */
    public static SubClass determineSubClass(UUID uuid, int characterSlot, SkillTreePosition position, Jedis jedis) {
        String playerClass = RunicCore.getCharacterAPI().getPlayerClass(uuid, characterSlot, jedis);
        return determineSubClass(playerClass, position);
    }

    private static ItemStack marksmanItem() {
        return ItemUtils.getHead
                ("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTU4" +
                        "ZjE2YmZmNjQ5ODhjNDRlN2JjYzJjYTc4NTJlYjM5YjI0ZTYwZWRhYWQ1ZmU0ODgzZjY3OWUwZjNjOTYyIn19fQ==");
    }

    private static ItemStack scoutItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3R" +
                "leHR1cmUvNWU2MjFjM2E2MDc2ZjY3OTg1YmZjNzY1NWYzNzViNDhjM2M1ZDg4MzQwZjIxMmNiZjEyMzBhZDRmYjBhZGJlOCJ9fX0=");
    }

    private static ItemStack rangerItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3" +
                "RleHR1cmUvYmViZjgxYmMwN2M1Zjg2NzY1ZmMwZjcxZTUxNmI3YWI3YjgyYWE2MzlkOTRkYjA5MWZkOTJlMDYxYWIzMDVjIn19fQ==");
    }

    private static ItemStack bardItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3" +
                "RleHR1cmUvZWM1NmY4Zjk2ZDE0MWUyYWI0MmE1ODkzMjZjNmFiZjYzNTc4NmZhMmM4NzA5ZWZkNDZmZGYyOWY3YTJjOTI3NCJ9fX0=");
    }

    private static ItemStack paladinItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3R" +
                "leHR1cmUvZWY2M2FhOWEzZjk4MzIzNTNmZDc4ZmU2OTc5NjM5YzcwOWMxMDU2YzdhODExNjNkMjllZjk0ZDA5OTI1YzMifX19");
    }

    private static ItemStack priestItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3R" +
                "leHR1cmUvNWRhNDgzNDljMDEwNDkyNTU2NzgzOWM4ZWQ5MTIwNjg4MzgwMzQ5MTE4YjUyNDNiMjU3OGExNjg3MmEzZmVmMyJ9fX0=");
    }

    private static ItemStack cryomancerItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3R" +
                "leHR1cmUvYWIxZWUyMjM5ODllMmQ5NzgzNzVjZGMzMmFjNzk4YjJiOGUwYTYzYmQ3OGFjNmFiZDQ3MGY5MWQ3ZTVmYTM3NCJ9fX0=");
    }

    private static ItemStack pyromancerItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3R" +
                "leHR1cmUvNDA4MGJiZWZjYTg3ZGMwZjM2NTM2YjY1MDg0MjVjZmM0Yjk1YmE2ZThmNWU2YTQ2ZmY5ZTljYjQ4OGE5ZWQifX19");
    }

    private static ItemStack arcanistItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3R" +
                "leHR1cmUvY2NiM2QyMmFlMDAxMjE4NjFlZTY1ZTg1MDJjMzEzNzg0YTI2OGU0MmExMzBlM2Q2N2RlZDNhNjg0OGVhZjk2OCJ9fX0=");
    }

    private static ItemStack assassinItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3R" +
                "leHR1cmUvMjZkOWRmZjBiZTkzNTlmODExODIyYmZlNGJkOGRkNDBkMDVhZmNjZGJkMTE0ZGUwODFlMWJmNDY5MzA3MWVmYiJ9fX0=");
    }

    private static ItemStack duelistItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3R" +
                "leHR1cmUvYmIxNzliNTY0ODc2MGRiNjY1NTg1YTMwZWM4YjFiZThjM2QyNTFjYTMwNzUwNjA1OTJhYzU1YmI5ZDg1M2U4In19fQ==");
    }

    private static ItemStack pirateItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3R" +
                "leHR1cmUvOTg4MGE1YjM2OTcyZWFmNTY2M2Y5Y2ExYjBiZDAwZWE4YzRmMDU1ZmM2ZTJhOWU3YTIyMDZlYzM5OTA5ZGVhMSJ9fX0=");
    }

    private static ItemStack berserkerItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3R" +
                "leHR1cmUvZjdkYjQ2NDRkYTc1MjcxZWQzZTVhN2UwODJjMDJlOWNjZDc4ODQyN2JmNWFiNDQyMTNiYjRjMTc3ZTFiMSJ9fX0=");
    }

    private static ItemStack guardianItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3R" +
                "leHR1cmUvMzgyZWU4NWZlNmRjNjMyN2RhZDIwMmZjYTkzYzlhOTRhYzk5YjdiMTY5NzUyNGJmZTk0MTc1ZDg4NzI1In19fQ==");
    }

    private static ItemStack inquisitorItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3R" +
                "leHR1cmUvMjBkOThiNTI0MjQ1MWI4ZjUyNzZmZWE5ODNkM2MyODRhNDZiNGFjOTA0ODBiZDUwODNkNDg3YjlmZmY3NDMyZiJ9fX0=");
    }

    public CharacterClass getBaseClass() {
        return this.baseClass;
    }

    public String getDescription() {
        return this.description;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public String getName() {
        return this.name;
    }
}
