package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Smite extends Spell {

    private static final int DAMAGE_AMT = 8;
    private static final float RADIUS = 3f;

    public Smite() {
        super("Smite",
                "You smite all enemies within " + (int) RADIUS + " blocks," +
                        "\ndealing " + DAMAGE_AMT + " spellʔ damage to them." +
                        "\nUndead enemies take double damage!",
                ChatColor.WHITE, ClassEnum.CLERIC, 8, 20);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
        pl.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, pl.getEyeLocation(), 15, 0.75F, 0.5F, 0.75F, 0);

        for (Entity en : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {

            // skip non-living, armor stands
            if (!(en instanceof LivingEntity)) continue;
            LivingEntity le = (LivingEntity) en;

            // heal party members and the caster
            int damage = DAMAGE_AMT;
            if (MythicMobs.inst().getMobManager().getActiveMob(en.getUniqueId()).isPresent()) {
                ActiveMob am = MythicMobs.inst().getMobManager().getActiveMob(en.getUniqueId()).get();
                if (am.getFaction() != null && am.getFaction().equalsIgnoreCase("undead")) {
                    damage = 2*DAMAGE_AMT;
                }
            }
            if (verifyEnemy(pl, le)) {
                DamageUtil.damageEntitySpell(damage, le, pl, false);
            }
        }
    }
}
