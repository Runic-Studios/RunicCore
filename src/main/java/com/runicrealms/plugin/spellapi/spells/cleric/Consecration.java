package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Circle;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("FieldCanBeLocal")
public class Consecration extends Spell implements MagicDamageSpell {
    private static final int DAMAGE_AMT = 12;
    private static final double DAMAGE_PER_LEVEL = 0.25;
    private static final int DURATION = 8;
    private static final int RADIUS = 7;

    public Consecration() {
        super("Consecration", CharacterClass.CLERIC);
        this.setDescription("You conjure a ring of holy magic on the ground " +
                "for " + DURATION + "s, slowing enemies within " + RADIUS + " " +
                "blocks and dealing (" + DAMAGE_AMT + " + &f" + DAMAGE_PER_LEVEL
                + "x&7 lvl) magicʔ damage each second!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        final Location castLocation = player.getLocation();
        Spell spell = this;

        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                } else {
                    Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> Circle.createParticleCircle(player, castLocation, RADIUS, Particle.SPELL_INSTANT, Color.WHITE));
                    Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> Circle.createParticleCircle(player, castLocation, RADIUS - 3, Particle.SPELL_INSTANT, Color.WHITE));
                    player.getWorld().playSound(castLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 2.0f);
                    for (Entity en : player.getWorld().getNearbyEntities(castLocation, RADIUS, RADIUS, RADIUS)) {
                        if (!(isValidEnemy(player, en))) continue;
                        LivingEntity victim = (LivingEntity) en;
                        addStatusEffect(victim, RunicStatusEffect.SLOW_III, 3, false);
                        DamageUtil.damageEntitySpell(DAMAGE_AMT, victim, player, spell);
                    }
                    count += 1;
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }

    @Override
    public double getMagicDamage() {
        return 0;
    }

    @Override
    public void setMagicDamage(double magicDamage) {

    }

    @Override
    public double getMagicDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {

    }
}
