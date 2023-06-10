package com.runicrealms.plugin.api;

import com.runicrealms.plugin.model.TitleData;
import com.runicrealms.plugin.rdb.api.WriteCallback;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface TitleAPI {

    /**
     * Tries to retrieve a TitleData object from server memory
     *
     * @param uuid of the player
     * @return a TitleData object
     */
    TitleData getTitleData(UUID uuid);

    /**
     * Removes all prefixes AND suffixes for the given player
     *
     * @param player        remove titles for
     * @param writeCallback function to execute on completion
     */
    void removePrefixesAndSuffixes(Player player, WriteCallback writeCallback);

}
