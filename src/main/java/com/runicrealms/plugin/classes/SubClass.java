package com.runicrealms.plugin.classes;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.util.ItemUtils;
import com.runicrealms.plugin.model.SkillTreePosition;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public enum SubClass {

    /*
     Archer
     */
    MARKSMAN("Marksman", 1, CharacterClass.ARCHER, marksmanItem(), "Marksman is a master of mobility and long-range &cphysical⚔ attacks!"),
    STORMSHOT("Stormshot", 2, CharacterClass.ARCHER, stormshotItem(), "Stormshot is a master of lightning &3magicʔ&7, slinging area-of-effect spells!"),
    WARDEN("Warden", 3, CharacterClass.ARCHER, wardenItem(), "Warden is the keeper of the forest, &ahealing✦ &7allies through the power of nature!"),
    /*
     Cleric
     */
    BARD("Bard", 1, CharacterClass.CLERIC, bardItem(), "Bard is a hybrid &3magicalʔ &7and &cphysical⚔ &7fighter who controls the flow of battle with &aally buffs &7and &aenemy debuffs&7!"),
    HERETIC("Heretic", 2, CharacterClass.CLERIC, hereticItem(), "Heretic uses dark &3magicʔ &7to bring death to their enemies!"),
    LIGHTBRINGER("Lightbringer", 3, CharacterClass.CLERIC, lightbringerItem(), "Lightbringer blasts enemies with light to &aheal✦ &7allies and keep them strong!"),
    /*
     Mage
     */
    ARCANIST("Arcanist", 1, CharacterClass.MAGE, arcanistItem(), "Arcanist has &abuffs &7to empower &3magicalʔ &7allies!"),
    CRYOMANCER("Cryomancer", 2, CharacterClass.MAGE, cryomancerItem(), "Cryomancer freezes and slows enemies with &fcrowd control&7!"),
    PYROMANCER("Pyromancer", 3, CharacterClass.MAGE, pyromancerItem(), "Pyromancer deals powerful area-of-effect &3magicʔ &7damage!"),
    /*
     Rogue
     */
    ASSASSIN("Assassin", 1, CharacterClass.ROGUE, assassinItem(), "Assassin emerges from the &8shadows &7to quickly burst an opponent with &cphysical⚔ &7strikes!"),
    CORSAIR("Corsair", 2, CharacterClass.ROGUE, corsairItem(), "Corsair uses &cphysical⚔ &7projectiles to control the flow of battle!"),
    DUELIST("Duelist", 3, CharacterClass.ROGUE, duelistItem(), "Duelist is an excellent &cphysical⚔ &7fighter against a single opponent!"),
    /*
    Warrior
     */
    BERSERKER("Berserker", 1, CharacterClass.WARRIOR, berserkerItem(), "Berserker fights ferociously with &cphysical⚔ &7attacks that cleave enemies!"),
    GUARDIAN("Guardian", 2, CharacterClass.WARRIOR, guardianItem(), "Guardian excels as a &ftank&7, defending allies from harm!"),
    PALADIN("Paladin", 3, CharacterClass.WARRIOR, paladinItem(), "Paladin is a hybrid &3magicalʔ &7fighter and &ahealer✦&7!");

    public static final Set<SubClass> ARCHER_SUBCLASSES = new LinkedHashSet<>();
    public static final Set<SubClass> CLERIC_SUBCLASSES = new LinkedHashSet<>();
    public static final Set<SubClass> MAGE_SUBCLASSES = new LinkedHashSet<>();
    public static final Set<SubClass> ROGUE_SUBCLASSES = new LinkedHashSet<>();
    public static final Set<SubClass> WARRIOR_SUBCLASSES = new LinkedHashSet<>();

    static {
        for (SubClass subClass : SubClass.values()) {
            switch (subClass.getCharacterClass()) {
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
    private final int position;
    private final CharacterClass characterClass;
    private final ItemStack itemStack;
    private final String description;

    /**
     * @param name           of the subclass
     * @param position       of the subclass (is this the first? fourth? it's alphabetical)
     * @param characterClass the base class
     * @param itemStack      the head item for ui
     * @param description    of the subclass
     */
    SubClass(String name, int position, CharacterClass characterClass, ItemStack itemStack, String description) {
        this.name = name;
        this.position = position;
        this.characterClass = characterClass;
        this.itemStack = itemStack;
        this.description = description;
    }

    /**
     * Determines the appropriate subclass based on player class and specified position
     *
     * @param position (which sub-class? 1, 2, or 3)
     * @return the SubClass enum, or null if not found
     */
    @Nullable
    public static SubClass determineSubClass(CharacterClass characterClass, SkillTreePosition position) {
        for (SubClass subClass : SubClass.values()) {
            if (subClass.getCharacterClass() != characterClass) continue;
            if (subClass.getPosition() == position.getValue())
                return subClass;
        }
        return null;
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
        String className = RunicCore.getCharacterAPI().getPlayerClass(uuid, characterSlot, jedis);
        CharacterClass characterClass = CharacterClass.getFromName(className);
        return determineSubClass(characterClass, position);
    }

    private static ItemStack marksmanItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWQ0ZGE3Zjc0Mzg1MTliZTIwMmU1OGM5MWYyOWI4NjllYmQ5ZGUyZWZiMWJiZjQ0NWY3NDVkOWZiMTIyODcifX19");
    }

    private static ItemStack stormshotItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmViMTBjYjg3NTNkYTgwMDMwMzIwYmUyMzg5MWExM2ZmYzI4MmQ4NWU2ZDJiNzg2YmNlZjRlYmYyMzFhZDJlYSJ9fX0=");
    }

    private static ItemStack wardenItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTU4ZjE2YmZmNjQ5ODhjNDRlN2JjYzJjYTc4NTJlYjM5YjI0ZTYwZWRhYWQ1ZmU0ODgzZjY3OWUwZjNjOTYyIn19fQ==");
    }

    private static ItemStack bardItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3" +
                "RleHR1cmUvZWM1NmY4Zjk2ZDE0MWUyYWI0MmE1ODkzMjZjNmFiZjYzNTc4NmZhMmM4NzA5ZWZkNDZmZGYyOWY3YTJjOTI3NCJ9fX0=");
    }

    private static ItemStack hereticItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTE5ZWQ0MjNkNTc1MDk0Mzc1YmU4OWI3ZDJmOTAwODE0NDZmZmI2YzhkMjQ0YTExOWE3NmE4OGJmNGNiNDA2NCJ9fX0=");
    }

    private static ItemStack lightbringerItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2EyNDM1ZDI3YWZlZGM1YTZjNDAwMzEwYzhkYmFhZDZjZjYwMmMwZDdmYWRlNGExNzVjZWU2NjllY2NmNTUwNyJ9fX0=");
    }

    private static ItemStack cryomancerItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzE2OGIzYjg4MGU4MGVmY2JmY2M3MDQ3YWE3MGMxNjViNTM3MjFkNTM4ZWRiNmNiYWQ1YzM2MDhlOGQzMTFmZSJ9fX0=");
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

    private static ItemStack corsairItem() {
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

    private static ItemStack paladinItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3R" +
                "leHR1cmUvZWY2M2FhOWEzZjk4MzIzNTNmZDc4ZmU2OTc5NjM5YzcwOWMxMDU2YzdhODExNjNkMjllZjk0ZDA5OTI1YzMifX19");
    }

    public CharacterClass getCharacterClass() {
        return this.characterClass;
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

    public int getPosition() {
        return position;
    }
}
