package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.item.GearScanner;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class Heal extends Spell {

    private final boolean healOverTime;
    private static final int HOT_DURATION = 5; // heal-over-time
    private static final int HEALING_AMT = 25;
    private static final float RADIUS = 7f;

    // constructor
    public Heal() {
        super("Heal",
                "You restore✦ " + HEALING_AMT + " health to all" +
                        "\nallies within " + (int) RADIUS + " blocks!",
                ChatColor.WHITE, ClassEnum.CLERIC, 10, 20);
        this.healOverTime = false;
    }

    public Heal(boolean healOverTime) {
        super("Heal",
                "You restore✦ " + HEALING_AMT + " health to all" +
                        "\nallies within " + (int) RADIUS + " blocks!",
                ChatColor.WHITE, ClassEnum.CLERIC, 10, 20);
        this.healOverTime = healOverTime;
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.swingMainHand();
        Location loc = pl.getLocation();
        pl.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.5f);
        pl.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 15, 1, 1, 1, 0);

        // heal people
        for (Entity entity : Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, RADIUS, RADIUS, RADIUS)) {

            // skip non-players
            if (!(entity instanceof Player)) continue;

            // heal party members and the caster
            Player ally = (Player) entity;
            if (verifyAlly(pl, ally)) {
                HealUtil.healPlayer(HEALING_AMT, ally, pl, true, false, false);
                if (healOverTime) {
                    new BukkitRunnable() {
                        int count = 1;
                        @Override
                        public void run() {
                            if (count > HOT_DURATION)
                                this.cancel();
                            else {
                                count += 1;
                                HealUtil.healPlayer((HEALING_AMT + GearScanner.getHealingBoost(pl)) / HOT_DURATION,
                                        ally, pl, false, false, false);
                            }
                        }
                    }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
                }
            }
        }
    }

    public static int getHotDuration() {
        return HOT_DURATION;
    }
}
