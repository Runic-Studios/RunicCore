package com.runicrealms.plugin.spellapi.spellutil;

import com.runicrealms.plugin.api.event.AllyVerifyEvent;
import com.runicrealms.plugin.events.EnemyVerifyEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TargetUtil {


    /**
     * Method to check for valid enemy before applying damage calculation. True if enemy can be damaged.
     *
     * @param caster player who used spell
     * @param victim mob or player who was hit by spell
     * @return whether target is valid
     */
    public static boolean isValidEnemy(@NotNull Player caster, @NotNull Entity victim) {
        EnemyVerifyEvent enemyVerifyEvent = new EnemyVerifyEvent(caster, victim);
        Bukkit.getServer().getPluginManager().callEvent(enemyVerifyEvent);
        return !enemyVerifyEvent.isCancelled();
    }

    /**
     * Method to check for valid enemy before applying healing / buff spell calculation. True if enemy can be healed.
     *
     * @param caster player who used spell
     * @param ally   entity who was hit by spell
     * @return whether target is valid
     */
    public static boolean isValidAlly(@NotNull Player caster, @NotNull Entity ally) {
        AllyVerifyEvent allyVerifyEvent = new AllyVerifyEvent(caster, ally);
        Bukkit.getServer().getPluginManager().callEvent(allyVerifyEvent);
        return !allyVerifyEvent.isCancelled();
    }
}
