package com.runicrealms.plugin.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Syntax;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.RunicCommon;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@CommandAlias("tempban")
@CommandPermission("runiccore.tempban")
public class TempbanCMD extends BaseCommand {

    private static UUID getUUID(String name) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream()));
            String uuid = (((JsonObject) new JsonParser().parse(in)).get("id")).toString().replaceAll("\"", "");
            uuid = uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
            in.close();
            return UUID.fromString(uuid);
        } catch (Exception e) {
            return null;
        }
    }

    public static String prettyPrintMillis(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(days > 1 ? " days " : " day ");
        if (hours > 0) sb.append(hours).append(hours > 1 ? " hours " : " hour ");
        if (minutes > 0) sb.append(minutes).append(minutes > 1 ? " minutes" : " minute");
        return sb.toString().trim();
    }

    @Default
    @CatchUnknown
    @Syntax("<player> <hours>")
    @CommandCompletion("@online @range:1-100")
    public void onCommand(CommandSender sender, @Single String targetName, int hours) {
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            final UUID uuid = getUUID(targetName);
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "Could not find player " + targetName);
                return;
            }
            long timeUntilUnban = hours * 60L * 60 * 1000;
            long unbanTimestamp = System.currentTimeMillis() + timeUntilUnban;
            Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + targetName + " permission set runic.tempbanned");
                Player target = Bukkit.getPlayer(uuid);
                if (target != null) {
                    target.kickPlayer(ChatColor.RED + "You have been temporarily banned for " + prettyPrintMillis(timeUntilUnban) +
                            ".\nTo appeal this ban, contact a moderator on our discord server.");
                }
            });
            RunicCommon.getLuckPermsAPI().savePayload(RunicCommon.getLuckPermsAPI().createPayload(uuid, (data) -> data.set("runic.tempban.timestamp", unbanTimestamp)));
            sender.sendMessage(ChatColor.GREEN + "Temporarily banned " + targetName + " for " + hours + " hours.");
        });
    }

}
