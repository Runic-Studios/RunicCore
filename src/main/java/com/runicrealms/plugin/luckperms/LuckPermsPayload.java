package com.runicrealms.plugin.luckperms;

import java.util.UUID;

public interface LuckPermsPayload {

    void saveToData(LuckPermsData data);

    UUID owner();

}
