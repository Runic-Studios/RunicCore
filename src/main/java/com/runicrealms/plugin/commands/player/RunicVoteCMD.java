package com.runicrealms.plugin.commands.player;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.RunicCombatExpEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.UUID;

public class RunicVoteCMD implements CommandExecutor, Listener {

    private static final double VOTE_AMT = 2;
    private final HashMap<UUID, Double> votingBonuses = new HashMap<>();

    public RunicVoteCMD() {
        RunicCore.getInstance().getServer().getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // runicvote <player>
        if (!sender.isOp())
            return true;

        Player pl = Bukkit.getPlayer(args[0]);
        if (pl == null)
            return true;

        if (!votingBonuses.containsKey(pl.getUniqueId())) {
            votingBonuses.put(pl.getUniqueId(), VOTE_AMT);
            Bukkit.getScheduler().scheduleAsyncDelayedTask(RunicCore.getInstance(), () -> {
                votingBonuses.remove(pl.getUniqueId());
                pl.sendMessage(ChatColor.GRAY + "Your voting experience bonus has expired!");
            }, 3600 * 20L); // one hour
        } else {
            double currentAmt = votingBonuses.get(pl.getUniqueId());
            votingBonuses.put(pl.getUniqueId(), currentAmt + VOTE_AMT); // bonus can stack
        }

        pl.sendMessage(ChatColor.GREEN + "Your voting experience bonus is now " + votingBonuses.get(pl.getUniqueId()).intValue() + "%!");

        return true;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExperienceGain(RunicCombatExpEvent event) {
        if (event.getRunicExpSource() == RunicCombatExpEvent.RunicExpSource.QUEST) return;
        if (event.getRunicExpSource() == RunicCombatExpEvent.RunicExpSource.OTHER) return;
        if (votingBonuses.get(event.getPlayer().getUniqueId()) == null) return;
        // calculate voting exp modifier (if applicable)
        double votePercent = votingBonuses.get(event.getPlayer().getUniqueId()) / 100;
        event.setBonus(RunicCombatExpEvent.BonusType.VOTE, votePercent);
    }
}
