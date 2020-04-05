package com.runicrealms.plugin.database;

import com.mongodb.client.model.Filters;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.cache.PlayerCache;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RunicDatabaseAPI implements IRunicDatabase {

    @Override
    public Document getPlayerFile(String uuid) {
        return RunicCore.getDatabaseManager().getPlayerData().find
                (Filters.eq("player_uuid", uuid)).first();
    }

    @Override
    public Document getCharacter(Document playerFile, int characterSlot) {
        Document embedded = (Document) playerFile.get("character");
        return (Document) embedded.get(characterSlot + "");
    }

    @Override
    public PlayerCache getPlayerCache(Document playerFile) {
        return null;
    }

    /**
     *
     * @param uuid of player, todo: change to UUID of character?
     * @param characterSlot number representing which char
     * @param inv player's current inv
     */
    @Override
    public void updateCharacterInv(String uuid, int characterSlot, Inventory inv) {
        updateDocumentField(uuid, "character." + characterSlot, serializedInventory(inv));
    }

    @Override
    public void updateDocumentField(String uuid, String identifier, Object value) {
        Document document = new Document("player_uuid", uuid); // change player_uuid to uuid
        Bson newValue = new Document(identifier, value);
        Bson updateOperation = new Document("$set", newValue);
        RunicCore.getDatabaseManager().getPlayerData().updateOne(document, updateOperation);
    }

    private Document serializedInventory(Inventory inv) {
        Document inventory = new Document();
        ItemStack[] contents = inv.getContents();
        String serializedInv = InventorySerializer.toBase64List(contents);
        inventory.append("inv", serializedInv);
        return inventory;
    }
}
