package com.runicrealms.plugin.group;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.group.gui.GroupCreateChoosePurposeGui;
import com.runicrealms.plugin.group.gui.GroupCreateChooseTypeGui;
import com.runicrealms.plugin.group.gui.GroupInfoGui;
import com.runicrealms.plugin.group.gui.GroupJoinGui;
import com.runicrealms.plugin.group.gui.GroupMainGui;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class GroupGuiManager implements Listener {

    public void registerEvents() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        Bukkit.getPluginManager().registerEvents(new GroupMainGui(), RunicCore.getInstance());
        Bukkit.getPluginManager().registerEvents(new GroupInfoGui(), RunicCore.getInstance());
        Bukkit.getPluginManager().registerEvents(new GroupJoinGui(), RunicCore.getInstance());
        Bukkit.getPluginManager().registerEvents(new GroupCreateChooseTypeGui(), RunicCore.getInstance());
        Bukkit.getPluginManager().registerEvents(new GroupCreateChoosePurposeGui(), RunicCore.getInstance());
        GroupMainGui.initInventory();
        GroupCreateChooseTypeGui.initInventory();
        GroupCreateChoosePurposeGui.initInventories();
    }

}
