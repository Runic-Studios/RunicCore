package com.runicrealms.plugin.party;

import com.runicrealms.api.chat.ChatChannel;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ColorUtil;
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
    public TextComponent createMessage(Player player, String message) {
        return createMessage(player, message, false);
    }

    @Override
    public TextComponent createSpyMessage(Player player, Player spy, String message) {
        return createMessage(player, message, true);
    }

    private TextComponent createMessage(Player player, String message, boolean spy) {
        String ownerName = "";
        if (spy) {
            Party party = RunicCore.getPartyAPI().getParty(player.getUniqueId());
            ownerName = "Unknown";
            if (party != null) ownerName = party.getLeader().getName();
        }
        TextComponent textComponent = new TextComponent(ColorUtil.format(PlaceholderAPI.setPlaceholders(player, "&a[" + (spy ? ownerName + "'s " : "") + "Party] %luckperms_meta_name_color%%player_name%: &f")) + message);
        String title = PlaceholderAPI.setPlaceholders(player, "%core_prefix%");
        if (title.isEmpty()) title = "None";
        String titleColor = ColorUtil.format(PlaceholderAPI.setPlaceholders(player, "%core_name_color%"));
        textComponent.setHoverEvent(new HoverEvent
                (
                        HoverEvent.Action.SHOW_TEXT,
                        new Text(
                                ChatColor.DARK_AQUA + "Title: " + titleColor + title +
                                        ChatColor.GREEN + PlaceholderAPI.setPlaceholders(player, "\n%core_class% lv. %core_level%")
                        )
                )
        );
        return textComponent;
    }

    @Override
    public boolean isSpyable() {
        return true;
    }

    @Override
    public boolean canSpy(Player sender, Player spy) {
        return RunicCore.getPartyAPI().getParty(sender.getUniqueId()) != null;
    }

}