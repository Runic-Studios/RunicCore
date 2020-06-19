package com.runicrealms.plugin.group;

import com.runicrealms.api.chat.ChatChannel;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;

public class GroupChannel extends ChatChannel {

    @Override
    public String getPrefix() {
        return "&9[&3Group&9] &a[%core_class_prefix%|%core_level%] &r";
    }

    public String getConsolePrefix() {
        return "&9[&3Group&9] &r";
    }

    @Override
    public String getName() {
        return "group";
    }

    @Override
    public Collection<Player> getRecipients(Player player) {
        if (RunicCore.getGroupManager().getPlayerGroup(player) != null) {
            return RunicCore.getGroupManager().getPlayerGroup(player).getMembers();
        } else {
            player.sendMessage(ChatColor.RED + "You must be in a group to use group chat!");
        }
        return new HashSet<>();
    }

    @Override
    public String getMessageFormat() {
        return "%luckperms_meta_name_color%%player_name%: &r%message%";
    }

}
