package com.runicrealms.plugin.party;

import com.runicrealms.api.chat.ChatChannel;
import com.runicrealms.plugin.RunicCore;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;

public class PartyChannel extends ChatChannel {

    public String getConsolePrefix() {
        return "&a[Party] &r";
    }

    @Override
    public String getPrefix() {
        return "&a[Party] &r";
    }

    @Override
    public String getName() {
        return "party";
    }

    @Override
    public Collection<Player> getRecipients(Player player) {
        if (RunicCore.getPartyAPI().getParty(player.getUniqueId()) != null) {
            return RunicCore.getPartyAPI().getParty(player.getUniqueId()).getMembersWithLeader();
        } else {
            player.sendMessage(ChatColor.RED + "You must be in a party to use party chat!");
        }
        return new HashSet<>();
    }

    @Override
    public String getMessageFormat() {
        return "%luckperms_meta_name_color%%player_name%: &r%message%";
    }

    @Override
    public TextComponent getTextComponent(Player player, String finalMessage) {
        TextComponent textComponent = new TextComponent(finalMessage);
        textComponent.setHoverEvent(new HoverEvent
                (
                        HoverEvent.Action.SHOW_TEXT,
                        new Text(PlaceholderAPI.setPlaceholders(player,
                                ChatColor.DARK_AQUA + "Title: " + ChatColor.AQUA + "%core_prefix%" +
                                        ChatColor.GREEN + "\n%core_class% lv. %core_level%"
                        ))
                )
        );
        return textComponent;
    }

}