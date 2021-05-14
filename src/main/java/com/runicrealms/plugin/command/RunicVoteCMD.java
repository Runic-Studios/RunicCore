package com.runicrealms.plugin.command;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.RunicExpEvent;
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
    public void onExperienceGain(RunicExpEvent e) {
        if (e.getRunicExpSource() == RunicExpEvent.RunicExpSource.QUEST) return;
        if (e.getRunicExpSource() == RunicExpEvent.RunicExpSource.OTHER) return;
        if (votingBonuses.get(e.getPlayer().getUniqueId()) == null) return;
        // calculate voting exp modifier (if applicable)
        double votePercent = votingBonuses.get(e.getPlayer().getUniqueId()) / 100;
        double voteBoost = votePercent * e.getOriginalAmount();
        e.setFinalAmount(e.getFinalAmount() + (int) voteBoost);
    }
}
