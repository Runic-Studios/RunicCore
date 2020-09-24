package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Enflame extends Spell {

    private final boolean areaOfEffect;
    private static final int DURATION = 5;
    private static final double PERCENT = 50;
    private static final int RADIUS = 5;
    private final HashSet<UUID> flamers = new HashSet<>();

    public Enflame() {
        super("Enflame",
                "For " + DURATION + " seconds, you ignite your blade" +
                        "\nwith pure flame, causing your weapon⚔" +
                        "\nattacks to deal " + (int) (PERCENT+100) + "% damage for" +
                        "\nthe duration!",
                ChatColor.WHITE, ClassEnum.ROGUE, 15, 20);
        this.areaOfEffect = false;
    }

    /**
     *
     * @param areaOfEffect used for tier set
     */
    public Enflame(boolean areaOfEffect) {
        super("Enflame",
                "For " + DURATION + " seconds, you ignite your blade" +
                        "\nwith pure flame, causing your weapon⚔" +
                        "\nattacks to deal " + (int) (PERCENT+100) + "% damage for" +
                        "\nthe duration!",
                ChatColor.WHITE, ClassEnum.ROGUE, 15, 20);
        this.areaOfEffect = areaOfEffect;
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // apply effects
        flamers.add(pl.getUniqueId());
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_LAVA_POP, 0.5f, 2f);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 2f);
        Cone.coneEffect(pl, Particle.FLAME, DURATION, 0, 20L, Color.GREEN);

        new BukkitRunnable() {
            @Override
            public void run() {
                flamers.remove(pl.getUniqueId());
            }
        }.runTaskLater(RunicCore.getInstance(), DURATION*20L);
    }

    /**
     * Damage nearby entities
     */
    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        if (!flamers.contains(e.getPlayer().getUniqueId())) return;
        double percent = PERCENT / 100;
        int extraAmt = (int) (e.getAmount() * percent);
        e.setAmount(e.getAmount() + extraAmt);
        Entity victim = e.getEntity();
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f, 1.0f);
        victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_LAVA_POP, 0.5f, 1);
        victim.getWorld().spawnParticle(Particle.FLAME, victim.getLocation(), 10, 0.5F, 0.5F, 0.5F, 0);
        if (areaOfEffect) {
            for (Entity en : e.getPlayer().getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
                if (!verifyEnemy(e.getPlayer(), en)) continue;
                DamageUtil.damageEntitySpell((e.getAmount() * 0.5), (LivingEntity) en, e.getPlayer(), 0);
                en.getWorld().playSound(en.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f, 1.0f);
                en.getWorld().playSound(en.getLocation(), Sound.BLOCK_LAVA_POP, 0.5f, 1);
                en.getWorld().spawnParticle(Particle.FLAME, en.getLocation(), 10, 0.5F, 0.5F, 0.5F, 0);
            }
        }
    }

    public static double getPercent() {
        return PERCENT;
    }

    public static int getRadius() {
        return RADIUS;
    }
}

