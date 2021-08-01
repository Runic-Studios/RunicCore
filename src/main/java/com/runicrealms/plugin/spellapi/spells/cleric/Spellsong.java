package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Spellsong extends Spell {

    private final double bonus;
    private static final int DURATION = 6;
    private static final double PERCENT = 40;
    private static final int RADIUS = 10;
    private final List<UUID> singers;

    public Spellsong() {
        super("Spellsong",
                "You sing a song of battle, granting a buff " +
                        "to all allies within " + RADIUS + " blocks! " +
                        "For " + DURATION + "s, the buff increases the " +
                        "spellʔ damage of you and your allies " +
                        "by " + (int) PERCENT + "%!",
                ChatColor.WHITE, ClassEnum.CLERIC, 15, 15);
        singers = new ArrayList<>();
        this.bonus = 0;
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 2.0F);
        startParticleTask(pl);

        // buff caster
        singers.add(pl.getUniqueId());

        // buff all players within 10 blocks
        if (RunicCore.getPartyManager().getPlayerParty(pl) != null) {
            for (Player memeber : RunicCore.getPartyManager().getPlayerParty(pl).getMembersWithLeader()) {
                if (pl.getLocation().distance(memeber.getLocation()) > RADIUS) continue;
                singers.add(memeber.getUniqueId());
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                singers.clear();
            }
        }.runTaskLaterAsynchronously(RunicCore.getInstance(), DURATION * 20L);
    }

    private void startParticleTask(Player pl) {
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                    return;
                }
                count++;
                pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 1.0F);
                pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.6F);
                pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.2F);
                pl.getWorld().spawnParticle
                        (Particle.NOTE, pl.getEyeLocation(), 15, 0.75F, 0.75F, 0.75F, 0);
            }
        }.runTaskTimer(plugin, 0, 20L);
    }

    @EventHandler
    public void onSpellHit(SpellDamageEvent e) {

        Player damager = e.getPlayer();
        if (singers == null) return;
        if (!singers.contains(damager.getUniqueId())) return;

        double percent = (PERCENT + bonus) / 100;
        int extraAmt = (int) (e.getAmount() * percent);
        if (extraAmt < 1) extraAmt = 1;
        e.setAmount(e.getAmount() + extraAmt);
        damager.getWorld().playSound(damager.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.25F, 1.0F);
        e.getVictim().getWorld().spawnParticle
                (Particle.NOTE, e.getVictim().getLocation().add(0, 1.5, 0),
                        5, 1.0F, 0, 0, 0); // 0.3F
    }

    public static double getPERCENT() {
        return PERCENT;
    }
}

