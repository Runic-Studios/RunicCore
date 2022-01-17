package com.runicrealms.plugin.classes;

import com.runicrealms.plugin.item.util.ItemUtils;
import org.bukkit.inventory.ItemStack;

public enum SubClassEnum {

    /*
     archer
     */
    MARKSMAN("Marksman", ClassEnum.ARCHER, marksmanItem(), "Marksman is a master of single-target damage!"),
    SCOUT("Scout", ClassEnum.ARCHER, scoutItem(), "Scout weakens enemies with utility!"),
    WARDEN("Warden", ClassEnum.ARCHER, wardenItem(), "Warden controls the battlefield with crowd control!"),
    /*
     cleric
     */
    BARD("Bard", ClassEnum.CLERIC, bardItem(), "Bard controls the flow of battle with ally buffs and enemy debuffs!"),
    PALADIN("Paladin", ClassEnum.CLERIC, paladinItem(), "Paladin is a hybrid fighter and healer!"),
    PRIEST("Priest", ClassEnum.CLERIC, priestItem(), "Priest heals allies and keeps them strong!"),
    /*
     mage
     */
    CRYOMANCER("Cryomancer", ClassEnum.MAGE, cryomancerItem(), "Cryomancer freezes and slows enemies with crows control!"),
    PYROMANCER("Pyromancer", ClassEnum.MAGE, pyromancerItem(), "Pyromancer deals powerful area-of-effect damage!"),
    WARLOCK("Warlock", ClassEnum.MAGE, warlockItem(), "Warlock has utility and buffs to out-maneuver opponents!"),
    /*
     rogue
     */
    ASSASSIN("Assassin", ClassEnum.ROGUE, assassinItem(), "Assassin emerges from the shadows to quickly burst an opponent!"),
    DUELIST("Duelist", ClassEnum.ROGUE, duelistItem(), "Duelist is an excellent fighter against a single opponent!"),
    SWINDLER("Swindler", ClassEnum.ROGUE, swindlerItem(), "Swindler uses deception to manipulate enemies!"),
    /*
    warrior
     */
    BERSERKER("Berserker", ClassEnum.WARRIOR, berserkerItem(), "Berserker fights ferociously with abilities that cleave enemies!"),
    GUARDIAN("Guardian", ClassEnum.WARRIOR, guardianItem(), "Guardian excels as a tank, defending allies!"),
    INQUISITOR("Inquisitor", ClassEnum.WARRIOR, inquisitorItem(), "Inquisitor uses crowd control to disable enemies!");

    private final String name;
    private final ClassEnum baseClass;
    private final ItemStack itemStack;
    private final String description;

    SubClassEnum(String name, ClassEnum baseClass, ItemStack itemStack, String description) {
        this.name = name;
        this.baseClass = baseClass;
        this.itemStack = itemStack;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public ClassEnum getBaseClass() {
        return this.baseClass;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the enum value of a sub-class string
     *
     * @param name of sub-class
     * @return enum of sub-class
     */
    public static SubClassEnum getFromName(String name) {
        for (SubClassEnum subClassType : SubClassEnum.values()) {
            if (subClassType.getName().equalsIgnoreCase(name)) {
                return subClassType;
            }
        }
        return null;
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

    private static ItemStack wardenItem() {
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

    private static ItemStack warlockItem() {
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

    private static ItemStack swindlerItem() {
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
}
