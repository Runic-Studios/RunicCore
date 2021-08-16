package com.runicrealms.plugin.commands;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MapLink implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] strings) {
        BaseComponent component = new TextComponent(ChatColor.GREEN + "Click here to open the Runic Realms online map: " + ChatColor.DARK_GREEN + "https://runicrealms.com/map");
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://runicrealms.com/map"));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent("Runic Realms Map")}));
        sender.spigot().sendMessage(component);
        return true;
    }

}
