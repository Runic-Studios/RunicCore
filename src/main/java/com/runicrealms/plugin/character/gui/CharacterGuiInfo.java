package com.runicrealms.plugin.character.gui;

import com.runicrealms.plugin.model.ClassInfo;
import com.runicrealms.plugin.model.PlayerData;

import java.util.HashMap;
import java.util.Map;

public class CharacterGuiInfo {

    private final Map<Integer, ClassInfo> characters = new HashMap<>();
    private int firstUnusedSlot = 1;

    public CharacterGuiInfo(PlayerData playerData) {
        for (Integer characterSlot : playerData.getPlayerCharacters().keySet()) {
            this.characters.put(characterSlot, playerData.getPlayerCharacters().get(characterSlot));
            this.findFirstUnusedSlot();
        }
//        try {
//            if (mongoData.has("character")) {
//                for (String key : mongoData.getSection("character").getKeys()) {
//                    this.characters.put(Integer.parseInt(key), new CharacterInfo(
//                            ClassEnum.getFromName(mongoData.get("character." + key + ".class.name", String.class)),
//                            mongoData.get("character." + key + ".class.exp", Integer.class),
//                            mongoData.get("character." + key + ".class.level", Integer.class)));
//
//                }
//            }
//            this.findFirstUnusedSlot();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /**
     * @param character
     */
    public void addCharacter(ClassInfo character) {
        this.characters.put(this.firstUnusedSlot, character);
        this.findFirstUnusedSlot();
    }

    /**
     * @param slot
     */
    public void removeCharacter(Integer slot) {
        this.characters.remove(slot);
        this.findFirstUnusedSlot();
    }

    /**
     *
     */
    private void findFirstUnusedSlot() {
        for (int i = 1; i <= 10; i++) {
            if (this.characters.get(i) == null) {
                this.firstUnusedSlot = i;
                break;
            }
        }
    }

    /**
     * @return
     */
    public int getFirstUnusedSlot() {
        return this.firstUnusedSlot;
    }

    /**
     * @return
     */
    public Map<Integer, ClassInfo> getCharacterInfo() {
        return this.characters;
    }

}
