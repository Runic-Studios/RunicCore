package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MagicDamageEvent;
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

public class ArcaneOrb extends Spell {

    public static final int RADIUS = 12;
    private static final int DURATION = 10;
    private static final double PERCENT = 100;
    private static final HashMap<UUID, Location> arcaneOrbMap = new HashMap<>();

    public ArcaneOrb() {
        super("Arcane Orb",
                "You summon an orb of arcane magic! " +
                        "For the next " + DURATION + " seconds, you and your allies " +
                        "receive a " + (int) PERCENT + "% bonus to all spellʔ " +
                        "damage dealt by standing within " + RADIUS + " blocks " +
                        "of the orb!",
                ChatColor.WHITE, ClassEnum.MAGE, 40, 30);
    }

    public static HashMap<UUID, Location> getArcaneOrbMap() {
        return arcaneOrbMap;
    }

    private void createCircle(Player player, Location loc) {
        int particles = 50;
        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * (float) ArcaneOrb.RADIUS;
            z = Math.sin(angle) * (float) ArcaneOrb.RADIUS;
            loc.add(x, 0, z);
            player.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.FUCHSIA, 1));
            loc.subtract(x, 0, z);
        }
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        Location loc = player.getLocation().clone().add(0, 2, 0);
        Location circleLoc = loc.clone().subtract(0, 2, 0);
        arcaneOrbMap.put(player.getUniqueId(), circleLoc);
        if (RunicCoreAPI.hasParty(player)) {
            for (Player ally : RunicCore.getPartyManager().getPlayerParty(player).getMembers()) // add allies
                arcaneOrbMap.put(ally.getUniqueId(), circleLoc);
        }
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                    arcaneOrbMap.remove(player.getUniqueId());
                } else {
                    count += 1;
                    spawnSphere(loc);
                    createCircle(player, circleLoc);
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 20L);
    }

    @EventHandler
    public void onSpellDamage(MagicDamageEvent event) {

        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        if (arcaneOrbMap.get(id) == null) return;

        double dist = player.getLocation().distanceSquared(arcaneOrbMap.get(id));

        if (dist <= RADIUS * RADIUS) {
            double percent = PERCENT / 100;
            int extraAmt = (int) (event.getAmount() * percent);
            event.setAmount(event.getAmount() + extraAmt);
        }
    }

    private void spawnSphere(Location loc) {
        for (double i = 0; i <= Math.PI; i += Math.PI / 12) {
            double radius = Math.sin(i);
            double y = Math.cos(i);
            for (double a = 0; a < Math.PI * 2; a += Math.PI / 12) {
                double x = .9 * Math.cos(a) * radius;
                double z = .9 * Math.sin(a) * radius;
                loc.add(x, y, z);
                Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.SPELL_WITCH, loc, 1, 0, 0, 0, 0);
                loc.subtract(x, y, z);
            }
        }
    }
}

