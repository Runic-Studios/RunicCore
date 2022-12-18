package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.ArmorStandAPI;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Circle;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class Bolster extends Spell {

    private static final int DURATION = 8;
    private static final double PERCENT = .25;
    private static final int RADIUS = 10;
    private final HashMap<UUID, Location> buffed;
    private ArmorStand armorStand;

    public Bolster() {
        super("Bolster",
                "You summon a banner of war for " + DURATION + "s! " +
                        "Yourself and allies who stand within " + RADIUS + " blocks of " +
                        "the banner are bolstered, receiving a " + (int) (PERCENT * 100) +
                        "% damage reduction buff!",
                ChatColor.WHITE, CharacterClass.WARRIOR, 15, 20);
        buffed = new HashMap<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        Location location = player.getLocation();
        Location bannerLoc = location.clone().subtract(0, 1.75, 0);
        this.armorStand = summonBanner(player, bannerLoc);
        if (armorStand == null) return;

        buffed.put(player.getUniqueId(), bannerLoc);
        if (RunicCore.getPartyAPI().hasParty(player.getUniqueId()))
            for (Player ally : RunicCore.getPartyAPI().getParty(player.getUniqueId()).getMembers()) // add allies
                buffed.put(ally.getUniqueId(), bannerLoc);

        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                    Bolster.this.armorStand.remove();
                    buffed.clear();
                } else {
                    count += 1;
                    Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(),
                            () -> Circle.createParticleCircle(player, location, RADIUS, Particle.VILLAGER_HAPPY));
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 20L);
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent event) {
        double reducedDamage = reduceDamage(event.getVictim(), event.getAmount());
        event.setAmount((int) reducedDamage);
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        double reducedDamage = reduceDamage(event.getVictim(), event.getAmount());
        event.setAmount((int) reducedDamage);
    }

    @EventHandler
    public void onSpellDamage(MagicDamageEvent event) {
        double reducedDamage = reduceDamage(event.getVictim(), event.getAmount());
        event.setAmount((int) reducedDamage);
    }

    /**
     * @param victim
     * @param damageAmount
     * @return
     */
    private double reduceDamage(Entity victim, double damageAmount) {
        if (buffed.get(victim.getUniqueId()) == null) return damageAmount;
        UUID id = victim.getUniqueId();
        double dist = victim.getLocation().distanceSquared(buffed.get(id)); // distance to banner
        if (dist > RADIUS * RADIUS) return damageAmount;
        victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 2.0f);
        return damageAmount * (1 - PERCENT);
    }

    /**
     * @param player
     * @param location
     * @return
     */
    private ArmorStand summonBanner(Player player, Location location) {
        ArmorStand armorStand = ArmorStandAPI.spawnArmorStand(location);
        if (armorStand == null) return null;
        if (armorStand.getEquipment() == null) return null;
        armorStand.setArms(true);
        armorStand.setMarker(false);
        armorStand.setCustomName(ChatColor.YELLOW + player.getName() + "'s Banner");
        armorStand.setCustomNameVisible(true);
        armorStand.getEquipment().setHelmet(new ItemStack(Material.BLUE_BANNER));
        player.getWorld().playSound(armorStand.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 0.5f);
        return armorStand;
    }
}
