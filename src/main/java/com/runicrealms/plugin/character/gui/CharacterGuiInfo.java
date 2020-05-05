package com.runicrealms.plugin.character.gui;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.database.MongoData;

import java.util.HashMap;
import java.util.Map;

public class CharacterGuiInfo {

    private Map<Integer, CharacterInfo> characters = new HashMap<Integer, CharacterInfo>();
    private int firstUnusedSlot = 1;

    public CharacterGuiInfo(MongoData mongoData) {
        try {
            if (mongoData.has("character")) {
                for (String key : mongoData.getSection("character").getKeys()) {
                    this.characters.put(Integer.parseInt(key), new CharacterInfo(
                            ClassEnum.getFromName(mongoData.get("character." + key + ".class.name", String.class)),
                            mongoData.get("character." + key + ".class.exp", Integer.class),
                            mongoData.get("character." + key + ".class.level", Integer.class)));

                }
            }
            this.findFirstUnusedSlot();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addCharacter(CharacterInfo character) {
        this.characters.put(this.firstUnusedSlot, character);
        this.findFirstUnusedSlot();
    }

    public void removeCharacter(Integer slot) {
        this.characters.remove(slot);
        this.findFirstUnusedSlot();
    }

    private void findFirstUnusedSlot() {
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
