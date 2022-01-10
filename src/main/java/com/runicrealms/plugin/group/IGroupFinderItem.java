package com.runicrealms.plugin.group;

public interface IGroupFinderItem {
    int getMinLevel();

    QueueReason getQueueReason();

    String getSkullPlayerName();

    String getMenuItemName();

    String[] getMenuItemDescription();
}
