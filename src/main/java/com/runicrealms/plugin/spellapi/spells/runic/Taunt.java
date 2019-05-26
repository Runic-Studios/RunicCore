package com.runicrealms.plugin.spellapi.spells.runic;

import com.runicrealms.plugin.professions.utilities.FloatingItemUtil;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import io.lumine.xikage.mythicmobs.skills.SkillCaster;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.SkillTrigger;
import org.bukkit.*;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class Taunt extends Spell {

    private static final int DAMAGE = 5;
    private List<Entity> hasHit = new ArrayList<>();

    // constructor
    public Taunt() {
        super("Taunt",
                "You throw your artifact in front of you," +
                        "\ndealing " + DAMAGE + " damage to the first monster" +
                        "\nhit and taunting it!", ChatColor.WHITE,1, 1);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        ItemStack artifact = pl.getInventory().getItem(0);
        if (artifact == null) {
            this.doCooldown = false;
            return;
        }

        Material artifactType = artifact.getType();
        int durability = ((Damageable) artifact.getItemMeta()).getDamage();

        Vector path = pl.getEyeLocation().getDirection().normalize().multiply(1.5);

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_SHULKER_SHOOT, 0.5f, 2.0f);
        Entity projectile = FloatingItemUtil.spawnFloatingItem(pl.getEyeLocation(), artifactType, 50, path, durability);

        new BukkitRunnable() {
            @Override
            public void run() {

                if (projectile.isOnGround() || projectile.isDead()) {
                    if (projectile.isOnGround()) {
                        projectile.remove();
                    }
                    this.cancel();
                }

                // prevent multiple hits
                for (Entity en : projectile.getNearbyEntities(1, 1, 1)) {
                    if (!(en instanceof LivingEntity)) continue;
                    if (en instanceof Monster && !hasHit.contains(projectile)) {
                        hasHit.add(projectile);
                        projectile.remove();
                        DamageUtil.damageEntitySpell(DAMAGE, (LivingEntity) en, pl);
                        ((Monster) en).setTarget(pl);
                        MythicMobs.inst().getAPIHelper().taunt(en, pl);
                        en.getWorld().playSound(en.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.2f);
                        en.getWorld().spawnParticle
                                (Particle.VILLAGER_ANGRY, en.getLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
    }
}

