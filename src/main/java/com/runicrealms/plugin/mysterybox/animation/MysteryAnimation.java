package com.runicrealms.plugin.mysterybox.animation;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

/**
 * Created by KissOfFate
 * Date: 7/17/2019
 * Time: 8:22 PM
 */
public abstract class MysteryAnimation implements Animation {
    private String _name;
    protected final Random _rand = new Random();
    protected int _tick;

    private BukkitTask task = null;

    public MysteryAnimation(String name, int tick) {
        this._name = name;
        this._tick = tick;
    }

    @Override
    public void spawn(Player player, Location location) {
        this.start();
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> this.onTick(player, location), 0, this._tick);
    }

    protected void start() {}

    protected void stop() {
        task.cancel();
    }

    public String getName() {
        return this._name;
    }
    public Random getRandom() { return this._rand; }

    protected abstract void onTick(Player player, Location location);

}
