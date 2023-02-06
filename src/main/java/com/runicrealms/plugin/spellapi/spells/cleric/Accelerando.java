package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Accelerando extends Spell {
    private static final int DURATION_I = 3;
    private static final int DURATION_II = 3;
    private static final int DURATION_III = 1;
    private static final int RADIUS = 10;

    public Accelerando() {
        super("Accelerando",
                "You inspire your team to charge into battle! " +
                        "First, you grant Speed I to allies within " +
                        RADIUS + " blocks for " + DURATION_I + "s, " +
                        "followed by Speed II for " + DURATION_II + "s, " +
                        "and finally Speed III for " + DURATION_III + "s!",
                ChatColor.WHITE, CharacterClass.CLERIC, 20, 15);
    }

    /**
     * @param player     to receive speed
     * @param duration   of speed (seconds)
     * @param multiplier of speed (0 for speed I)
     */
    private void applySpeed(Player player, int duration, int multiplier) {

        // begin sound effects
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5F, multiplier * 0.5F);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5F, 0.7F);

        // add player effects
        switch (multiplier) {
            case 1:
                addStatusEffect(player, RunicStatusEffect.SPEED_II, duration, false);
                break;
            case 2:
                addStatusEffect(player, RunicStatusEffect.SPEED_III, duration, false);
                break;
            default:
                addStatusEffect(player, RunicStatusEffect.SPEED_I, duration, false);
                break;
        }
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation(),
                25, 0.5f, 0.5f, 0.5f, 0, new Particle.DustOptions(Color.WHITE, 20));
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        // Speed I
        applySpeed(player, DURATION_I, 0);
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), RADIUS, RADIUS, RADIUS, target -> isValidAlly(player, target))) {
            applySpeed((Player) entity, DURATION_I, 0);
        }

        // Speed II
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            applySpeed(player, DURATION_II, 1);
            for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), RADIUS, RADIUS, RADIUS, target -> isValidAlly(player, target))) {
                applySpeed((Player) entity, DURATION_II, 1);
            }

            // Speed III
            Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
                applySpeed(player, DURATION_III, 2);
                for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), RADIUS, RADIUS, RADIUS, target -> isValidAlly(player, target))) {
                    applySpeed((Player) entity, DURATION_II, 2);
                }
            }, DURATION_II * 20L);
        }, DURATION_I * 20L);
    }
}
