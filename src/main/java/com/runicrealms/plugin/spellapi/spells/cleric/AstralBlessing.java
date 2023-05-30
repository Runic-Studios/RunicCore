package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.ShieldingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AstralBlessing extends Spell implements DurationSpell, ShieldingSpell {
    private final Map<UUID, BukkitTask> blessingMap = new ConcurrentHashMap<>();
    private double duration;
    private double shield;
    private double shieldPerLevel;

    public AstralBlessing() {
        super("Astral Blessing", CharacterClass.CLERIC);
        this.setIsPassive(true);
        this.setDescription("Enemies hit by &aStarlight &7are marked for the next " + duration + "s. " +
                "Basic attacking a marked player consumes the " +
                "mark and applies a (" + shield + " + &f" + shieldPerLevel +
                "x&7 lvl) health shield to the " +
                "ally who struck the marked player.");
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = (int) duration;
    }

    @Override
    public double getShield() {
        return shield;
    }

    @Override
    public void setShield(double shield) {
        this.shield = shield;
    }

    @Override
    public double getShieldingPerLevel() {
        return shieldPerLevel;
    }

    @Override
    public void setShieldPerLevel(double shieldingPerLevel) {
        this.shieldPerLevel = shieldingPerLevel;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMagicDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (event.getSpell() == null) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!(event.getSpell() instanceof Starlight)) return;
        applyBlessing(event.getPlayer(), event.getVictim());
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!blessingMap.containsKey(event.getVictim().getUniqueId())) return;
        blessingMap.get(event.getVictim().getUniqueId()).cancel();
        blessingMap.remove(event.getVictim().getUniqueId());
        Player caster = event.getPlayer();
        RunicCore.getSpellAPI().shieldPlayer(caster, caster, shield, this);
    }

    private void applyBlessing(Player caster, LivingEntity victim) {
        blessingMap.put(victim.getUniqueId(), new BukkitRunnable() {
            double count = 0;

            @Override
            public void run() {
                if (count >= duration) {
                    this.cancel();
                    blessingMap.remove(victim.getUniqueId());
                } else {
                    count += 1;
                    new HorizontalCircleFrame((float) 0.5, false).playParticle(caster, Particle.REDSTONE, victim.getEyeLocation(), 30, Color.YELLOW);
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 20L));
    }

    @EventHandler
    public void onMobDeath(MythicMobDeathEvent event) {
        if (!blessingMap.containsKey(event.getEntity().getUniqueId())) return;
        blessingMap.get(event.getEntity().getUniqueId()).cancel();
        blessingMap.remove(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(RunicDeathEvent event) {
        if (!blessingMap.containsKey(event.getVictim().getUniqueId())) return;
        blessingMap.get(event.getVictim().getUniqueId()).cancel();
        blessingMap.remove(event.getVictim().getUniqueId());
    }

}

