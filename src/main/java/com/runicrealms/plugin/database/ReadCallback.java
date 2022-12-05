package com.runicrealms.plugin.database;

import com.runicrealms.plugin.model.CharacterData;

public interface ReadCallback {

    /**
     * A function that can be run after a read request to redis or mongo
     */
    void onQueryComplete(CharacterData characterData); // todo: this should really implement session data to be more abstract
}
