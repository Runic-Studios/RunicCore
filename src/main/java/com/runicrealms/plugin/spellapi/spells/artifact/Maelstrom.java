package com.runicrealms.plugin.spellapi.spells.artifact;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.ArtifactSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.runicitems.item.event.RunicItemArtifactTriggerEvent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class Maelstrom extends Spell implements ArtifactSpell {

    private static final int DURATION = 4;
    private static final int RADIUS = 3;
    private static final double DAMAGE_PERCENT = 0.75;

    public Maelstrom() {
        super("Maelstrom", "", ChatColor.WHITE, ClassEnum.MAGE, 30, 0);
        this.setIsPassive(false);
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onArtifactUse(RunicItemArtifactTriggerEvent e) {
        if (!e.getRunicItemArtifact().getTemplateId().equals(getArtifactId())) return;
        if (isOnCooldown(e.getPlayer())) return;
        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll > getChance()) return;
        int damage = (int) ((e.getRunicItemArtifact().getWeaponDamage().getRandomValue() * DAMAGE_PERCENT) + RunicCoreAPI.getPlayerStrength(e.getPlayer().getUniqueId()));
        maelstromTask(e.getPlayer(), e.getVictim(), damage);
        e.setSpell(this);
    }

    private void maelstromTask(Player player, Entity victim, int damage) {
        Location castLocation = victim.getLocation();
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                } else {
                    createCircle(player, castLocation);
                    player.getWorld().playSound(castLocation, Sound.ENTITY_CAT_HISS, 0.5f, 0.1f);
                    for (Entity en : player.getWorld().getNearbyEntities(castLocation, RADIUS, RADIUS, RADIUS)) {
                        if (!(verifyEnemy(player, en))) continue;
                        LivingEntity victim = (LivingEntity) en;
                        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5f, 2.0f);
                        DamageUtil.damageEntitySpell(damage, victim, player);
                    }
                    count += 1;
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }

    private void createCircle(Player player, Location location) {
        int particles = 50;
        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * (float) RADIUS;
            z = Math.sin(angle) * (float) RADIUS;
            location.add(x, 0, z);
            player.getWorld().spawnParticle(Particle.CRIT_MAGIC, location, 5, 0, 0, 0, 0);
            player.getWorld().spawnParticle(Particle.REDSTONE, location, 5, 0, 0, 0, 0, new Particle.DustOptions(Color.NAVY, 1));
            location.subtract(x, 0, z);
        }
    }

    @Override
    public String getArtifactId() {
        return "runeforged-scepter";
    }

    @Override
    public double getChance() {
        return 0.35;
    }
}

