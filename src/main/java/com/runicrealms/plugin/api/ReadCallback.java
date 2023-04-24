package com.runicrealms.plugin.api;

import com.runicrealms.plugin.model.SessionDataMongo;

public interface ReadCallback {

    /**
     * A callback function that is run after a data read from redis or mongo
     *
     * @return the SessionDataMongo object that was created from the read
     */
    SessionDataMongo onReadComplete();
}
