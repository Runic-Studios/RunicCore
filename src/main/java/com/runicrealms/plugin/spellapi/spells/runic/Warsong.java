package com.runicrealms.plugin.spellapi.spells.runic;

import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizCircleFrame;
import org.bukkit.*;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Warsong extends Spell {

    private static final int DURATION = 7;
    private static double PERCENT = 20;
    private static final int RADIUS = 10;
    private List<UUID> singers = new ArrayList<>();

    public Warsong() {
        super("Warsong",
                "For " + DURATION + " seconds, you sing a song of" +
                        "\nbattle! Each time an ally strikes an" +
                        "\nenemy with their artifact, if they" +
                        "\nare within " + RADIUS + " blocks of you," +
                        "\ntheir attacks deal an additional " + (int) PERCENT + "%" +
                        "\ndamage!", ChatColor.WHITE,14, 15);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        singers.add(pl.getUniqueId());
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 2.0F);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 1.0F);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.6F);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.2F);
        pl.getWorld().spawnParticle
                (Particle.NOTE, pl.getEyeLocation(), 15, 0.5F, 0.5F, 0.5F, 0);

        new BukkitRunnable() {
            @Override
            public void run() {
                singers.remove(pl.getUniqueId());
            }
        }.runTaskLaterAsynchronously(RunicCore.getInstance(), DURATION*20L);
    }

    @EventHandler
    public void onArtfactHit(WeaponDamageEvent e) {

        Player damager = e.getPlayer();


        if (RunicCore.getPartyManager().getPlayerParty(damager) == null) return;

        // check singers, if the damager has a buffer in the party, check the distance.
        // if it's short enough, (< radius), buff the damage.
        for (UUID id : singers) {

            if (RunicCore.getPartyManager().getPlayerParty(damager).hasMember(id)
                    && damager.getLocation().distance(Bukkit.getPlayer(id).getLocation()) <= RADIUS) {

                double percent = PERCENT / 100;
                int extraAmt = (int) (e.getAmount() * percent);
                if (extraAmt < 1) {
                    extraAmt = 1;
                }
                e.setAmount(e.getAmount() + extraAmt);
                damager.playSound(damager.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.25F, 1.4F);
                e.getEntity().getWorld().spawnParticle
                        (Particle.NOTE, e.getEntity().getLocation().add(0, 1.5, 0),
                                3, 0.3F, 0.3F, 0.3F, 0);
            }
        }
    }
}

