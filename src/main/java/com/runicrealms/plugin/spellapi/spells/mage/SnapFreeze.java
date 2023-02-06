package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SnapFreeze extends Spell implements MagicDamageSpell {
    private static final int DAMAGE_AMOUNT = 20;
    private static final int DAMAGE_PER_LEVEL = 1;
    private static final int MAX_DIST = 4;
    private static final int RADIUS = 1;
    private static final double DURATION = 0.5;
    private static final double PERIOD = 0.5;
    private final Map<UUID, Set<UUID>> damageMap = new HashMap<>();

    public SnapFreeze() {
        super("Snap Freeze",
                "You cast a wave of frost in a forward line. " +
                        "Enemies hit by the spell take (" + DAMAGE_AMOUNT + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage and are rooted for " + DURATION + "s!",
                ChatColor.WHITE, CharacterClass.MAGE, 10, 25);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Location castLocation = player.getEyeLocation();
        freeze(player, castLocation);
        new BukkitRunnable() {
            double count = 1;

            @Override
            public void run() {
                if (count > MAX_DIST) {
                    this.cancel();
                    damageMap.remove(player.getUniqueId());
                } else {
                    count += 1 * PERIOD;
                    castLocation.add(castLocation.getDirection());
                    freeze(player, castLocation);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, (long) PERIOD * 20L);
    }

    private void freeze(Player player, Location location) {
        if (!damageMap.containsKey(player.getUniqueId()))
            damageMap.put(player.getUniqueId(), new HashSet<>());
        new HorizontalCircleFrame(RADIUS, true).playParticle(player, Particle.BLOCK_CRACK, location);
        player.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, 0.5f, 0.5f);
        for (Entity entity : player.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS, target -> isValidEnemy(player, target))) {
            if (damageMap.get(player.getUniqueId()).contains(entity.getUniqueId())) continue;
            DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, (LivingEntity) entity, player, this);
            addStatusEffect((LivingEntity) entity, RunicStatusEffect.ROOT, DURATION, true);
            damageMap.get(player.getUniqueId()).add(entity.getUniqueId());
        }
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }


}

