package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import com.runicrealms.plugin.RunicCore;

@SuppressWarnings("FieldCanBeLocal")
public class HolyNova extends Spell {

    private static final int DAMAGE_AMT = 5;
    private static final int DURATION = 6;
    private static final float RADIUS = 2.5f;

    // constructor
    public HolyNova() {
        super("Holy Nova", "For " + DURATION + " seconds, you pulse with holy power," +
                        "\nconjuring rings of light magic which" +
                        "\ndeal " + DAMAGE_AMT + " damage to enemies!",
                ChatColor.WHITE, 1, 5);
    }

    @Override
    public void executeSkill(Player pl, SpellItemType type) {

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
            location1.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, location1, 1, 0, 0, 0, 0);
            location1.subtract(x, 0, z);
        }

        for (Entity entity : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {

            // only damageable entities
            if (!(entity instanceof Damageable)) { continue; }

            // skip NPCs
            if (entity.hasMetadata("NPC")) { continue; }

            // skip the caster
            if(entity.equals(pl)) { continue; }

            // skip party members
            if (RunicCore.getPartyManager().getPlayerParty(pl) != null
                    && RunicCore.getPartyManager().getPlayerParty(pl).hasMember(entity.getUniqueId())) { continue; }

            // Executes the skill
            if (entity.getType().isAlive()) {
                LivingEntity victim = (LivingEntity) entity;
                DamageUtil.damageEntityMagic(DAMAGE_AMT, victim, pl);
            }
        }
    }
}
