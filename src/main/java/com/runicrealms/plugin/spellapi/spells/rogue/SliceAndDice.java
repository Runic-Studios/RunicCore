package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class SliceAndDice extends Spell {

    private static final int DAMAGE_AMT = 25;
    private static final int DAMAGE_CAP = 300;
    private static final double PERCENT = .25;
    private static final int RADIUS = 3;
    private static final double LAUNCH_PATH_MULT = 1.5;

    public SliceAndDice() {
        super("Slice and Dice",
                "You launch yourself backwards " +
                        "in the air then blink forward, " +
                        "slashing enemies within " + RADIUS + " blocks " +
                        "for (&f" + DAMAGE_AMT + " + " + PERCENT + "x " +
                        "their missing health&7) as physical⚔ damage! " +
                        "Capped at " + DAMAGE_CAP + " against monsters.",
                ChatColor.WHITE, CharacterClass.ROGUE, 15, 30);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // spell variables, vectors
        Location original = pl.getLocation();
        Vector look = pl.getLocation().getDirection();
        Vector launchPath = new Vector(-look.getX(), 1.0, -look.getZ()).normalize();

        // particles, sounds
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 2.0f);
        pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation(),
                25, 0, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.BLACK, 20));

        pl.setVelocity(launchPath.multiply(LAUNCH_PATH_MULT));

        new BukkitRunnable() {
            @Override
            public void run() {
                Location current = pl.getLocation();
                pl.teleport(original);
                pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 2.0f);
                pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 2.0f);
                VectorUtil.drawLine(pl, Particle.REDSTONE, Color.BLACK, original, current, 0.5D, 25);
                pl.swingMainHand();
                for (Entity en : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
                    if (!(en instanceof LivingEntity))
                        continue;
                    if (isValidEnemy(pl, en)) {
                        pl.getWorld().spawnParticle(Particle.SWEEP_ATTACK, ((LivingEntity) en).getEyeLocation(), 5, 0, 0, 0, 0);
                        int amount = DAMAGE_AMT + percentMissingHealth(en, PERCENT);
                        if (!(en instanceof Player) && amount > DAMAGE_CAP)
                            amount = DAMAGE_CAP;
                        DamageUtil.damageEntityPhysical(amount, (LivingEntity) en, pl, false, false);
                    }
                }
            }
        }.runTaskLater(RunicCore.getInstance(), 15L);
    }
}
