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

    private static final int DURATION = 6;
    private static double PERCENT = 40;
    private static final int RADIUS = 10;
    private List<UUID> singers;

    public Warsong() {
        super("Warsong",
                "You sing a song of battle, granting a buff" +
                        "\nto all party members within " + RADIUS + " blocks!" +
                        "\nFor " + DURATION + " seconds, the buff increases the" +
                        "\nweapon⚔ damage of your allies by " + (int) PERCENT + "%!" +
                        "\nThis spell has no effect on yourself.",
                ChatColor.WHITE, 15, 15);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 2.0F);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 1.0F);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.6F);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.2F);
        pl.getWorld().spawnParticle
                (Particle.NOTE, pl.getEyeLocation(), 15, 0.5F, 0.5F, 0.5F, 0);

        // buff all players within 10 blocks
        if (RunicCore.getPartyManager().getPlayerParty(pl) != null) {
            for (Player memeber : RunicCore.getPartyManager().getPlayerParty(pl).getPlayerMembers()) {
                if (pl.getLocation().distance(memeber.getLocation()) > RADIUS) continue;
                this.singers = new ArrayList<>();
                this.singers.add(memeber.getUniqueId());
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                singers.clear();
            }
        }.runTaskLaterAsynchronously(RunicCore.getInstance(), DURATION*20L);
    }

    @EventHandler
    public void onArtfactHit(WeaponDamageEvent e) {

        Player damager = e.getPlayer();
        if (this.singers == null) return;
        if (!singers.contains(damager.getUniqueId())) return;

        double percent = PERCENT / 100;
        int extraAmt = (int) (e.getAmount() * percent);
        if (extraAmt < 1) {
            extraAmt = 1;
        }
        e.setAmount(e.getAmount() + extraAmt);
        damager.getWorld().playSound(damager.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.25F, 1.0F);
        e.getEntity().getWorld().spawnParticle
                (Particle.NOTE, e.getEntity().getLocation().add(0, 1.5, 0),
                        5, 0.3F, 0.3F, 0.3F, 0);
    }
}

