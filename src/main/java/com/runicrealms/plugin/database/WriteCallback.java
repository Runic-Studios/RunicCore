package com.runicrealms.plugin.database;

public interface WriteCallback {

    /**
     * A callback function that is run after a document has been modified for character / player
     */
    void onWriteComplete();
}
