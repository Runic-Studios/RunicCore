package com.runicrealms.plugin.api;

public interface WriteCallback {

    /**
     * A callback function that is run after a data write to redis or mongo
     */
    void onWriteComplete();
}
