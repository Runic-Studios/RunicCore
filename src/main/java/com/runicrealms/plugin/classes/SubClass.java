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
    MARKSMAN("Marksman", 1, CharacterClass.ARCHER, marksmanItem(), "Marksman is a master of mobility and long-range &cphysical⚔ &7attacks!"),
    STORMSHOT("Stormshot", 2, CharacterClass.ARCHER, stormshotItem(), "Stormshot is a master of lightning &3magicʔ&7, slinging area-of-effect spells!"),
    WARDEN("Warden", 3, CharacterClass.ARCHER, wardenItem(), "Warden is the keeper of the forest, &ahealing✦ &7allies through the power of nature!"),
    /*
     Cleric
     */
    BARD("Bard", 1, CharacterClass.CLERIC, bardItem(), "Bard is a hybrid &3magicalʔ &7and &cphysical⚔ &7fighter who controls the flow of battle with &aally buffs &7and &aenemy debuffs&7!"),
    LIGHTBRINGER("Lightbringer", 2, CharacterClass.CLERIC, lightbringerItem(), "Lightbringer blasts enemies with light to &aheal✦ &7allies and keep them strong!"),
    SOULREAVER("Soulreaver", 3, CharacterClass.CLERIC, soulreaverItem(), "Soulreaver uses dark &3magicʔ &7to bring death to their enemies on the front lines!"),
    /*
     Mage
     */
    CRYOMANCER("Cryomancer", 1, CharacterClass.MAGE, cryomancerItem(), "Cryomancer freezes and slows enemies with &fcrowd control&7!"),
    PYROMANCER("Pyromancer", 2, CharacterClass.MAGE, pyromancerItem(), "Pyromancer deals powerful area-of-effect &3magicʔ &7damage!"),
    SPELLSWORD("Spellsword", 3, CharacterClass.MAGE, spellswordItem(), "Spellsword uses magical melee attacks to &ashield &7allies!"),
    /*
     Rogue
     */
    CORSAIR("Corsair", 1, CharacterClass.ROGUE, corsairItem(), "Corsair uses &cphysical⚔ " +
            "&7projectiles to control the flow of battle!"),
    NIGHTCRAWLER("Nightcrawler", 2, CharacterClass.ROGUE, nightcrawlerItem(), "Nightcrawler emerges " +
            "from the &8shadows &7to quickly burst an opponent with &cphysical⚔ &7strikes!"),
    WITCH_HUNTER("Witch Hunter", 3, CharacterClass.ROGUE, witchHunterItem(), "Witch Hunter brands a single enemy " +
            "for persecution!"),
    /*
    Warrior
     */
    BERSERKER("Berserker", 1, CharacterClass.WARRIOR, berserkerItem(), "Berserker fights ferociously with &cphysical⚔ &7attacks that cleave enemies!"),
    EARTHSHAKER("Earthshaker", 2, CharacterClass.WARRIOR, earthshakerItem(), "Earthshaker excels " +
            "as " +
            "a &ftank&7, defending allies from harm!"),
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

    private static ItemStack soulreaverItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGQ5MTdiNTFlNDYyMzA1Yzc3NjczNTM3MGQ4MmQ3MmNmOTU5Njg1MmRmMWM0YmI5ODNmMmViMDIwYTRlNjg1NyJ9fX0=");
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

    private static ItemStack spellswordItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmUwMzIzNzczYzBkYTViNDE3NzE4NTAwYTQ0OGFlM2RiZjg3ZDQ5YWMwNjhjZDUzZjAxNTAyZjRjMDMxNjE1MyJ9fX0=");
    }

    private static ItemStack nightcrawlerItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjg1YzFlZTYxZjJiZDQ0M2MwYTllNjE3ZjM3MjAzY2RmZjQ0MGJmYTJkMDBiNmRkMzZmZjgzNGNkODcwMmQ5In19fQ==");
    }

    private static ItemStack witchHunterItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjI3NGUxNjA1MjMzNDI1MDkxZjdiMjgzN2E0YmI4ZjRjODA0ZGFjODBkYjllNGY1OTlmNTM1YzAzYWZhYjBmOCJ9fX0=");
    }

    private static ItemStack corsairItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmExNGE0ZDJkMDJkY2QyNmExNzU1ZWI4NTYxYjcxYjM0ZDU5ZjQ1MThkOTk0NGExNDJjMmUxMTU2ODg2NzdkMSJ9fX0=");
    }

    private static ItemStack berserkerItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjZmNDNjYWFkYjQwMzNjNDFjN2YzNDcwMmM0N2ZmM2IyMWNlOTc3MTBjOGQ2NTMwN2Y1ODc2ZWU0NWMzZjRlNSJ9fX0=");
    }

    private static ItemStack earthshakerItem() {
        return ItemUtils.getHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjdkYjQ2NDRkYTc1MjcxZWQzZTVhN2UwODJjMDJlOWNjZDc4ODQyN2JmNWFiNDQyMTNiYjRjMTc3ZTFiMSJ9fX0=");
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
