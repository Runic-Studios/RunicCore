package com.runicrealms.plugin.character.gui;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.database.MongoData;

import java.util.HashMap;
import java.util.Map;

public class CharacterGuiInfo {

    private Map<Integer, CharacterInfo> characters = new HashMap<Integer, CharacterInfo>();
    private int firstUnusedSlot;

    public CharacterGuiInfo(MongoData mongoData) {
        if (mongoData.has("character")) {
            for (String key : mongoData.getSection("character").getKeys()) {
                this.characters.put(Integer.parseInt(key), new CharacterInfo(
                        ClassEnum.getFromName(mongoData.get("character." + key + ".class.name", String.class)),
                        mongoData.get("character." + key + ".class.exp", Integer.class),
                        mongoData.get("character." + key + ".class.level", Integer.class)));

            }
        }
        this.findFirstUnusedSlot();
    }

    public void addCharacter(CharacterInfo character) {
        this.characters.put(this.firstUnusedSlot, character);
        this.findFirstUnusedSlot();
    }

    public void removeCharacter(Integer slot) {
        this.characters.remove(slot);
        this.findFirstUnusedSlot();
    }

    public void findFirstUnusedSlot() {
        for (int i = 1; i <= 10; i++) {
            if (this.characters.get(i) == null){
                this.firstUnusedSlot = i;
                break;
            }
        }
    }

    public int getFirstUnusedSlot() {
        return this.firstUnusedSlot;
    }

    public Map<Integer, CharacterInfo> getCharacterInfo() {
        return this.characters;
    }

}
