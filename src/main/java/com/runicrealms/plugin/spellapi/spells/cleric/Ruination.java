package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("FieldCanBeLocal")
public class Ruination extends Spell implements HealingSpell, MagicDamageSpell {
    private static final int BASE_DURATION = 2;
    private static final int DAMAGE = 20;
    private static final int HEAL = 30;
    private static final int MAX_DURATION = 6;
    private static final int RADIUS = 3;
    private static final double DAMAGE_PER_LEVEL = 1.0D;
    private static final double HEALING_PER_LEVEL = 0.75;

    public Ruination() {
        super("Ruination",
                "For the next " + BASE_DURATION + "s, a " + RADIUS + " block radius around you " +
                        "becomes a realm of death! Enemies within the field " +
                        "take (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage per second! " +
                        "Additionally, every time this spell deals damage to an enemy, " +
                        "you heal✦ for (" + HEAL + " + &f" + HEALING_PER_LEVEL + "x&7 lvl) health and " +
                        "extend the duration of this spell by 1s, " +
                        "up to a max of 6s!",
                ChatColor.WHITE, CharacterClass.CLERIC, 20, 30);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5F, 1.0F);
        final int[] duration = {BASE_DURATION};
        Spell spell = this;
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > duration[0]) {
                    this.cancel();
                } else {
                    count += 1;
                    player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_GENERIC_DRINK, 1.0F, 2.0F);
                    boolean incrementDuration = false;
                    new HorizontalCircleFrame(RADIUS, false).playParticle(player, Particle.SOUL, player.getEyeLocation());
                    for (Entity entity : player.getWorld().getNearbyEntities
                            (player.getLocation(), RADIUS, RADIUS, RADIUS, target -> isValidEnemy(player, target))) {
                        incrementDuration = true;
                        DamageUtil.damageEntitySpell(DAMAGE, (LivingEntity) entity, player, spell);
                    }
                    if (incrementDuration && duration[0] < MAX_DURATION) {
                        duration[0] += 1;
                        HealUtil.healPlayer(HEAL, player, player, false, spell);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @Override
    public int getHeal() {
        return HEAL;
    }

    @Override
    public double getHealingPerLevel() {
        return HEALING_PER_LEVEL;
    }
}

