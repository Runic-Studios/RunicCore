package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;

public class CallOfTheDeep extends Spell implements MagicDamageSpell {
    private static final int DAMAGE = 20;
    private static final int RADIUS = 4;
    private static final double DAMAGE_PER_LEVEL = 1.0D;
    private static final double DURATION = 4D;

    public CallOfTheDeep() {
        super("Call of the Deep",
                "After pulling your target towards you, " +
                        "your &aHarpoon &7spell now summons a whirlpool " +
                        "at their feet for " + DURATION + "s! The whirlpool is " + RADIUS + " " +
                        "blocks wide, deals (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL + "x&7 lvl) " +
                        "magicʔ damage to enemies inside each second and attempts to drag them " +
                        "into the center.",
                ChatColor.WHITE, CharacterClass.ROGUE, 0, 0);
        this.setIsPassive(true);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler(priority = EventPriority.HIGH) // runs last
    public void onPredatorHit(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        // Summon whirlpool a few ticks after harpoon is landed
        if (event.getSpell() != null && event.getSpell() instanceof Harpoon) {
            Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                    () -> summonWhirlPool(event.getPlayer(), event.getVictim()), 15L); // 0.75s
        }
    }

    /**
     * Creates the whirlpool effect
     *
     * @param caster    who cast the spell
     * @param recipient of the harpoon
     */
    private void summonWhirlPool(Player caster, LivingEntity recipient) {
        Spell spell = this;
        Location castLocation = recipient.getLocation();
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {

                count++;
                if (count > DURATION)
                    this.cancel();

                new HorizontalCircleFrame(RADIUS, false).playParticle(caster, Particle.REDSTONE,
                        castLocation, Color.fromRGB(0, 64, 128));
                new HorizontalCircleFrame(RADIUS - 1, false).playParticle(caster, Particle.REDSTONE,
                        castLocation, Color.fromRGB(0, 89, 179));
                new HorizontalCircleFrame(RADIUS - 2, false).playParticle(caster, Particle.REDSTONE,
                        castLocation, Color.fromRGB(0, 102, 204));

                recipient.getWorld().playSound(castLocation,
                        Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 0.5f, 1.0f);

                for (Entity entity : recipient.getWorld().getNearbyEntities(castLocation, RADIUS,
                        RADIUS, RADIUS, target -> isValidEnemy(caster, target))) {
                    DamageUtil.damageEntitySpell(DAMAGE, (LivingEntity) entity, caster, spell);
                    // summon to middle
                    entity.teleport(castLocation);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }

}

