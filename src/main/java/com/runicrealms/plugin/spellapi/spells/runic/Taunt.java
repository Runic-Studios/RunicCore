package com.runicrealms.plugin.spellapi.spells.runic;

import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
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
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Taunt extends Spell {

    private static final int DAMAGE = 5;
    private static double PERCENT = 50;
    private HashMap<UUID, UUID> markedEntities;
    private List<Entity> hasHit;

    // constructor
    public Taunt() {
        super("Taunt",
                "You throw your artifact in front of you," +
                        "\ndealing " + DAMAGE + " damage to the first monster hit" +
                        "\nand taunting it, forcing it to attack you!" +
                        "\nThe monster is then marked, increasing" +
                        "\nall damage you deal to it by " + (int) PERCENT + "%!", ChatColor.WHITE,8, 10);
        markedEntities = new HashMap<>();
        hasHit = new ArrayList<>();
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        ItemStack artifact = pl.getInventory().getItem(0);
        if (artifact == null) {
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
                        markedEntities.put(pl.getUniqueId(), en.getUniqueId());
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

    /**
     * Deal extra damage to our marked entity
     */
    @EventHandler
    public void onWeapDamage(WeaponDamageEvent e) {

        if (markedEntities.containsKey(e.getPlayer().getUniqueId())
                && markedEntities.get(e.getPlayer().getUniqueId()) == e.getEntity().getUniqueId()) {
            double percent = PERCENT / 100;
            int extraAmt = (int) (e.getAmount() * percent);
            if (extraAmt < 1) {
                extraAmt = 1;
            }
            e.setAmount(e.getAmount() + extraAmt);
        }
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {

        if (markedEntities.containsKey(e.getPlayer().getUniqueId())
                && markedEntities.get(e.getPlayer().getUniqueId()) == e.getEntity().getUniqueId()) {
            double percent = PERCENT / 100;
            int extraAmt = (int) (e.getAmount() * percent);
            if (extraAmt < 1) {
                extraAmt = 1;
            }
            e.setAmount(e.getAmount() + extraAmt);
        }
    }
}

