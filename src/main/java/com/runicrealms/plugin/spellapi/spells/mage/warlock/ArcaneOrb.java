package com.runicrealms.plugin.spellapi.spells.mage.warlock;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class ArcaneOrb extends Spell {

    private static final int DURATION = 10;
    private static final double PERCENT = 25;
    private static final int RADIUS = 10;
    private final HashMap<UUID, Location> buffed = new HashMap<>();

    public ArcaneOrb() {
        super("Arcane Orb",
                "You summon an orb of arcane magic!" +
                        "\nFor the next " + DURATION + " seconds, all spellʔ" +
                        "\ndamage you deal is increased by " + (int) PERCENT + "%" +
                        "\nif you stand within " + RADIUS + " blocks" +
                        "\nof the orb!",
                ChatColor.WHITE, ClassEnum.MAGE, 15, 30);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        Location loc = pl.getLocation().clone().add(0, 2, 0);
        Location circleLoc = loc.clone().subtract(0, 2, 0);
        buffed.put(pl.getUniqueId(), circleLoc);
        new BukkitRunnable() {
            int count = 1;
            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                    buffed.remove(pl.getUniqueId());
                } else {
                    count += 1;
                    spawnSphere(loc);
                    createCircle(pl, circleLoc);
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 20L);
    }

    private void createCircle(Player pl, Location loc) {
        int particles = 50;
        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * (float) ArcaneOrb.RADIUS;
            z = Math.sin(angle) * (float) ArcaneOrb.RADIUS;
            loc.add(x, 0, z);
            pl.getWorld().spawnParticle(Particle.REDSTONE, loc, 5, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.FUCHSIA, 1));
            loc.subtract(x, 0, z);
        }
    }

    private void spawnSphere(Location loc) {
        for (double i = 0; i <= Math.PI; i += Math.PI / 12) {
            double radius = Math.sin(i);
            double y = Math.cos(i);
            for (double a = 0; a < Math.PI * 2; a+= Math.PI / 12) {
                double x = .9 * Math.cos(a) * radius;
                double z = .9 * Math.sin(a) * radius;
                loc.add(x, y, z);
                Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.SPELL_WITCH, loc, 1, 0, 0, 0, 0);
                loc.subtract(x, y, z);
            }
        }
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {

        Player pl = e.getPlayer();
        UUID id = pl.getUniqueId();
        if (buffed.get(id) == null) return;

        double dist = pl.getLocation().distance(buffed.get(id));

        if (dist <= RADIUS) {
            double percent = PERCENT / 100;
            int extraAmt = (int) (e.getAmount() * percent);
            e.setAmount(e.getAmount() + extraAmt);
        }
    }
}

