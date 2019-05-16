package com.runicrealms.plugin.tutorial.commands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.util.TabCompleteUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CaptainDefeated implements SubCommand, Listener {

    private TutorialSC tutorialSC;
    private Plugin plugin = RunicCore.getInstance();
    private List<UUID> lookers = new ArrayList<>();

    public CaptainDefeated(TutorialSC tutorialSC) {
        this.tutorialSC = tutorialSC;
    }


    @Override
    public void onConsoleCommand(CommandSender sender, String[] args) {
        Player pl = Bukkit.getPlayer(args[1]);
        destroyShip(args, pl);
    }

    @Override
    public void onOPCommand(Player sender, String[] args) {
        this.onUserCommand(sender, args);
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {
        Player pl = Bukkit.getPlayer(args[1]);
        destroyShip(args, pl);
    }

    @Override
    public String permissionLabel() {
        return "set.level";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        return TabCompleteUtil.getPlayers(commandSender, strings, plugin);
    }

    /**
     * This method destroys the ship on tutorial island.
     */
    private void destroyShip(String[] args, Player pl) {

        if (args.length != 2) return;

        Location shipLoc = new Location(Bukkit.getWorld("Alterra"), -2211, 26, 1762);

//        pl.sendBlockChange(pl.getLocation(), Material.TNT.createBlockData());
//        pl.sendBlockChange(pl.getLocation().add(0, 0, 2), Material.TNT.createBlockData());
//        pl.sendBlockChange(pl.getLocation().add(2, 0, -2), Material.TNT.createBlockData());
//        pl.sendBlockChange(pl.getLocation().add(-2, 0, 3), Material.TNT.createBlockData());
//        pl.sendBlockChange(pl.getLocation().add(3, 0, -3), Material.TNT.createBlockData());

        pl.playSound(pl.getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0f, 1.0f);
        pl.playSound(pl.getLocation(), Sound.ENTITY_CAT_HISS, 1.0f, 0.2f);

        pl.sendTitle(ChatColor.DARK_RED + "Time to Go!", "", 10, 60, 10);
        lookers.add(pl.getUniqueId());

        // get the player away!
        new BukkitRunnable() {
            @Override
            public void run() {
                pl.playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
                pl.teleport(new Location(Bukkit.getWorld("Alterra"), -2192.5, 25, 1773, 122.5f, -10));
                pl.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 1));
            }
        }.runTaskLater(RunicCore.getInstance(), 60);

        // blow up the ship!
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >=5) {
                    this.cancel();
                    lookers.remove(pl.getUniqueId());
                } else {
                    pl.spawnParticle(Particle.EXPLOSION_LARGE, shipLoc, 15, 10, 10, 10);
                    pl.spawnParticle(Particle.LAVA, shipLoc, 25, 10, 10, 10, 0);
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
                    count++;
                }
            }
        }.runTaskTimerAsynchronously(plugin, 60, 15);
    }

    @EventHandler
    public void onLookAway(PlayerMoveEvent e) {
        if (lookers.contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }
}
