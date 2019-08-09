package com.runicrealms.plugin.spellapi.spells.runic;

import com.runicrealms.plugin.outlaw.OutlawManager;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import com.runicrealms.plugin.RunicCore;

@SuppressWarnings("FieldCanBeLocal")
public class FireAura extends Spell {

    private static final int DURATION = 4;
    private static final int DAMAGE_AMT = 3;
    private static final int PERIOD = 1;
    private static final int RADIUS = 3;

    public FireAura() {
        super ("Fire Aura",
                "For " + DURATION + " seconds, you emit an aura of" +
                        "\nflame, damaging enemies within " + RADIUS + " blocks" +
                        "\n" + "every " + PERIOD + " second(s) for " + DAMAGE_AMT + " spellʔ damage." +
                        "\n" + ChatColor.DARK_RED + "Gem Bonus: 50%",
                ChatColor.WHITE, 10, 12);
    }

    // spell execute code
    @Override
    public void executeSpell(Player player, SpellItemType type) {

        new BukkitRunnable() {
            int count = 1;
            @Override
            public void run() {

                if (count > DURATION) {

                    this.cancel();

                } else {

                    count += PERIOD;

                    // particles
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 0.25f, 1.25f);
                    player.getWorld().spawnParticle
                            (Particle.FLAME, player.getEyeLocation(), 15, 0.75F, 0.5F, 0.75F, 0);

                    for (Entity en : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {

                        if (!(en instanceof LivingEntity)) continue;

                        LivingEntity victim = (LivingEntity) en;

                        // outlaw check
                        if (victim instanceof Player && (!OutlawManager.isOutlaw(((Player) victim)) || !OutlawManager.isOutlaw(player))) {
                            continue;
                        }

                        // skip NPCs
                        if (victim.hasMetadata("NPC")) continue;

                        // skip party members
                        if (RunicCore.getPartyManager().getPlayerParty(player) != null
                                && RunicCore.getPartyManager().getPlayerParty(player).hasMember(victim.getUniqueId())) {
                            continue;
                        }

                        // damage enemies
                        DamageUtil.damageEntitySpell(DAMAGE_AMT, victim, player, true);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, PERIOD*20L);
    }
}

