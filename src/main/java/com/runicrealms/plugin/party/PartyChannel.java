package com.runicrealms.plugin.party;

import com.runicrealms.api.chat.ChatChannel;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;

public class PartyChannel extends ChatChannel {

    @Override
    public String getPrefix() {
        return "&2[Party] &a[%core_class_prefix%|%core_level%] &r";
    }

    public String getConsolePrefix() {
        return "&2[Party] &r";
    }

    @Override
    public String getName() {
        return "party";
    }

    @Override
    public Collection<Player> getRecipients(Player player) {
        if (RunicCore.getPartyManager().getPlayerParty(player) != null) {
            return RunicCore.getPartyManager().getPlayerParty(player).getMembersWithLeader();
        } else {
            player.sendMessage(ChatColor.RED + "You must be in a party to use party chat!");
        }
        return new HashSet<>();
    }

    @Override
    public String getMessageFormat() {
        return "%luckperms_meta_name_color%%player_name%: &r%message%";
    }

}