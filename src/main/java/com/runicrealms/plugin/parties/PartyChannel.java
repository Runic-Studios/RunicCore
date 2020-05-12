package com.runicrealms.plugin.parties;

import com.runicrealms.api.chat.ChatChannel;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PartyChannel extends ChatChannel {

    @Override
    public String getPrefix() {
        return "&a[&2Party&a]&r ";
    }

    @Override
    public String getName() {
        return "party";
    }

    @Override
    public List<Player> getRecipients(Player player) {
        List<Player> recipients = new ArrayList<>();
        if (RunicCore.getPartyManager().getPlayerParty(player) != null) {
            for (Player target : RunicCore.getPartyManager().getPlayerParty(player).getPlayerMembers()) {
                if (target != null) {
                    recipients.add(target);
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "You must be in a party to use party chat!");
        }

        return recipients;
    }

    @Override
    public String getMessageFormat() {
        return "%luckperms_meta_name_color%%player_name%: &r%message%";
    }
}