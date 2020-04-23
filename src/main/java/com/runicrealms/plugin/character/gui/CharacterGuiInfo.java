package com.runicrealms.plugin.character.gui;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.database.MongoData;

import java.util.ArrayList;
import java.util.List;

public class CharacterGuiInfo {

    private List<CharacterInfo> characters;
    private int firstUnusedSlot;

    public CharacterGuiInfo(MongoData mongoData) {
        this.characters = new ArrayList<CharacterInfo>(10);
        for (String key : mongoData.getSection("character").getKeys()) {
            this.characters.set(Integer.parseInt(key), new CharacterInfo(
                    ClassEnum.getFromName(mongoData.get("character." + key + ".class.name", String.class)),
                    mongoData.get("character." + key + ".class.exp", Integer.class),
                    mongoData.get("character." + key + ".class.level", Integer.class)));

        }
        for (int i = 0; i < this.characters.size(); i++) {
            if (this.characters.get(i) == null){
                this.firstUnusedSlot = i + 1;
            }
        }
    }

    public void addCharacter(CharacterInfo character) {
        this.characters.add(character);
        for (int i = 0; i < this.characters.size(); i++) {
            if (this.characters.get(i) == null){
                this.firstUnusedSlot = i + 1;
            }
        }
    }

    public void removeCharacter(Integer slot) {
        this.characters.remove(slot);
        for (int i = 0; i < this.characters.size(); i++) {
            if (this.characters.get(i) == null){
                this.firstUnusedSlot = i + 1;
            }
        }
    }

    public int getFirstUnusedSlot() {
        return this.firstUnusedSlot;
    }

    public List<CharacterInfo> getCharacterInfo() {
        return this.characters;
    }

}
