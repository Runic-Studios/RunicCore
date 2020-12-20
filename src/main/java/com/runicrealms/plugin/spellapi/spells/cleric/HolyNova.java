package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

@SuppressWarnings("FieldCanBeLocal")
public class HolyNova extends Spell {

    private static final int DAMAGE_AMT = 5;
    private static final int DURATION = 5;
    private static final int HEAL_AMT = 8;
    private static final float RADIUS = 5f;
    private static final double GEM_BOOST = 50;

    // constructor
    public HolyNova() {
        super("Holy Nova",
                "For " + DURATION + " seconds, you pulse with holy" +
                        "\npower, conjuring rings of light" +
                        "\nthat deal " + DAMAGE_AMT + " spellʔ damage to enemies" +
                        "\nand restore✦ " + HEAL_AMT + " health to allies!" +
                        "\n" + ChatColor.DARK_RED + "Gem Bonus: " + (int) GEM_BOOST + "%",
                ChatColor.WHITE, ClassEnum.CLERIC, 12, 25);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // begin effect
        BukkitRunnable nova = new BukkitRunnable() {
            @Override
            public void run() {
                spawnRing(pl);
            }
        };
        nova.runTaskTimer(RunicCore.getInstance(), 0, 20);

        // cancel effect
        new BukkitRunnable() {
            @Override
            public void run() {
                nova.cancel();
            }
        }.runTaskLater(RunicCore.getInstance(), DURATION*20);
    }

    private void spawnRing(Player pl) {

        Location loc = pl.getLocation();
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 1.0F);

        Location location1 = pl.getEyeLocation();
        int particles = 50;
        float radius = RADIUS;

        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * radius;
            z = Math.sin(angle) * radius;
            location1.add(x, 0, z);
            pl.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, location1, 1, 0, 0, 0, 0);
            location1.subtract(x, 0, z);
        }

        for (Entity en : Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, RADIUS, RADIUS, RADIUS)) {

            if (!(en instanceof LivingEntity)) continue;

            LivingEntity le = (LivingEntity) en;

            // Executes the damage aspect of spell
            if (verifyEnemy(pl, en)) {
                DamageUtil.damageEntitySpell(DAMAGE_AMT, le, pl, GEM_BOOST);
            }

            // heal party members
            if (en instanceof Player && verifyAlly(pl, en)) {
                HealUtil.healPlayer(HEAL_AMT, ((Player) le), pl, true, true, false);
            }
        }
    }
}
