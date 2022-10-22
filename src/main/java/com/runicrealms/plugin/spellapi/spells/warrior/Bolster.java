package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.ArmorStandAPI;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Bolster extends Spell {

    private static final int DURATION = 8;
    private static final double PERCENT = .25;
    private static final int RADIUS = 10;
    private final HashMap<UUID, Location> buffed;
    private ArmorStand warbanner;

    public Bolster() {
        super("Bolster",
                "You summon a banner of war for " + DURATION + "s! " +
                        "Yourself and allies who stand within " + RADIUS + " blocks of " +
                        "the banner are bolstered, receiving a " + (int) (PERCENT * 100) +
                        "% damage reduction buff!",
                ChatColor.WHITE, ClassEnum.WARRIOR, 15, 20);
        buffed = new HashMap<>();
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        Location plLoc = pl.getLocation();
        Location bannerLoc = plLoc.clone().subtract(0, 1.75, 0);
        this.warbanner = summonBanner(pl, bannerLoc);
        if (warbanner == null) return;

        buffed.put(pl.getUniqueId(), bannerLoc);
        if (RunicCoreAPI.hasParty(pl))
            for (Player ally : RunicCore.getPartyManager().getPlayerParty(pl).getMembers()) // add allies
                buffed.put(ally.getUniqueId(), bannerLoc);

        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                    warbanner.remove();
                    buffed.clear();
                } else {
                    count += 1;
                    createCircle(pl, plLoc);
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 20L);
    }

    private ArmorStand summonBanner(Player pl, Location loc) {
        ArmorStand armorStand = ArmorStandAPI.spawnArmorStand(loc);
        if (armorStand == null) return null;
        if (armorStand.getEquipment() == null) return null;
        armorStand.setArms(true);
        armorStand.setMarker(false);
        armorStand.setCustomNameVisible(false);
        armorStand.getEquipment().setHelmet(new ItemStack(Material.RED_BANNER));
        pl.getWorld().playSound(armorStand.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 0.5f);
        return armorStand;
    }

    private void createCircle(Player pl, Location loc) {
        int particles = 50;
        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * (float) RADIUS;
            z = Math.sin(angle) * (float) RADIUS;
            loc.add(x, 0, z);
            pl.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 5, 0, 0, 0, 0);
            loc.subtract(x, 0, z);
        }
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent e) {
        double reducedDamage = reduceDamage(e.getVictim(), e.getAmount());
        e.setAmount((int) reducedDamage);
    }

    @EventHandler
    public void onSpellDamage(MagicDamageEvent e) {
        double reducedDamage = reduceDamage(e.getVictim(), e.getAmount());
        e.setAmount((int) reducedDamage);
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent e) {
        double reducedDamage = reduceDamage(e.getVictim(), e.getAmount());
        e.setAmount((int) reducedDamage);
    }

    private double reduceDamage(Entity victim, double damageAmount) {
        if (buffed.get(victim.getUniqueId()) == null) return damageAmount;
        UUID id = victim.getUniqueId();
        double dist = victim.getLocation().distanceSquared(buffed.get(id)); // distance to banner
        if (dist > RADIUS * RADIUS) return damageAmount;
        victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 2.0f);
        return damageAmount * (1 - PERCENT);
    }
}
