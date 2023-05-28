package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.StaffAttackEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.listeners.StaffListener;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.particles.SlashEffect;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.runicitems.Stat;
import com.runicrealms.runicitems.item.RunicItemWeapon;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.RayTraceResult;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class SpectralBlade extends Spell {

    public SpectralBlade() {
        super("Spectral Blade", CharacterClass.MAGE);
        this.setIsPassive(true);
        this.setDescription("While you are shielded, your basic attacks " +
                "have the same hitbox as &aArcane Slash &7and deal " +
                "magic damage instead!");
    }

    /**
     * @param player          who attacked
     * @param runicItemWeapon the staff (for damage numbers)
     */
    private void bladeAttack(Player player, RunicItemWeapon runicItemWeapon) {
        int minDamage = runicItemWeapon.getWeaponDamage().getMin();
        int maxDamage = runicItemWeapon.getWeaponDamage().getMax();

        // Apply attack effects, random damage amount
        int randomNum = ThreadLocalRandom.current().nextInt(minDamage, maxDamage + 1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.2f);
        double distance = ((DistanceSpell) RunicCore.getSpellAPI().getSpell("Arcane Slash")).getDistance();
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        distance,
                        ArcaneSlash.BEAM_WIDTH,
                        entity -> isValidEnemy(player, entity)
                );
        if (rayTraceResult == null) {
            Location location = player.getTargetBlock(null, (int) distance).getLocation();
            location.setDirection(player.getLocation().getDirection());
            location.setY(player.getLocation().add(0, 1, 0).getY());
            SlashEffect.slashHorizontal(player, Particle.SPELL_WITCH);
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            SlashEffect.slashHorizontal(player, Particle.SPELL_WITCH);
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 2.0f);
            Collection<Entity> targets = player.getWorld().getNearbyEntities
                    (livingEntity.getLocation(), ArcaneSlash.BEAM_WIDTH, ArcaneSlash.BEAM_WIDTH, ArcaneSlash.BEAM_WIDTH, target -> isValidEnemy(player, target));
            // Scale attack off STR first
            double physicalDamageBonusPercent = Stat.getPhysicalDmgMult() * RunicCore.getStatAPI().getPlayerStrength(player.getUniqueId());
            double finalDamage = randomNum + Math.ceil(randomNum * physicalDamageBonusPercent);
            // Then pass to double-scale off INT
            targets.forEach(target -> DamageUtil.damageEntitySpell(finalDamage, (LivingEntity) target, player, this));
        }

        player.setCooldown(runicItemWeapon.getDisplayableItem().getMaterial(), StaffListener.STAFF_COOLDOWN);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isBasicAttack()) return;
        if (!RunicCore.getSpellAPI().isShielded(event.getPlayer().getUniqueId())) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        Player player = event.getPlayer();
        Pair<Boolean, RunicItemWeapon> result = StaffListener.verifyStaff(player, player.getInventory().getItemInMainHand());
        if (result.first) {
            event.setCancelled(true);
            bladeAttack(player, result.second);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onStaffAttack(StaffAttackEvent event) {
        if (event.isCancelled()) return;
        if (!RunicCore.getSpellAPI().isShielded(event.getPlayer().getUniqueId())) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        event.setCancelled(true);
        bladeAttack(event.getPlayer(), event.getRunicItemWeapon());
    }
}

