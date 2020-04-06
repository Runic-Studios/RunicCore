package com.runicrealms.plugin.database;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.util.DatabaseUtil;
import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

public class CharacterAPI implements ICharacterAPI {

    @Override
    public Document getCharacter(Document playerFile, int characterSlot) {
        Document embedded = (Document) playerFile.get("character");
        return (Document) embedded.get(characterSlot + "");
    }

    /**
     *
     * @param uuid of player
     * @param characterSlot number representing which char
     * @param inv player's current inv
     */
    @Override
    public void updateCharacterInv(String uuid, int characterSlot, Inventory inv) {
        RunicCore.getDatabaseManager().getAPI().updateDocumentField
                ("2343243243243", "character." + characterSlot + ".inventory", DatabaseUtil.serializedInventory(inv));
    }

    @Override
    public void updateCharacterLoc(String uuid, int characterSlot, Location loc) {
        RunicCore.getDatabaseManager().getAPI().updateDocumentField
                ("2343243243243", "character." + characterSlot + ".location", DatabaseUtil.serializedLocation(loc)); // todo : uuid
    }
}
