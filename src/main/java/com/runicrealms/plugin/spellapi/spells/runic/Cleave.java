package com.runicrealms.plugin.spellapi.spells.runic;

import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Cleave extends Spell {

    private static final int DAMAGE_AMT = 10;
    private static final int RADIUS = 4;
    private static List<UUID> cleavers;

    public Cleave() {
        super ("Cleave",
                "You empower your artifact, causing your" +
                        "\nnext melee attack to deal " + DAMAGE_AMT + " spellʔ damage" +
                        "\nto all enemies within " + RADIUS + " blocks. (Does not" +
                        "\napply to ranged attacks)",
                ChatColor.WHITE, 10, 10);
        cleavers = new ArrayList<>();
    }

    // spell execute code
    @Override
    public void executeSpell(Player player, SpellItemType type) {

        UUID uuid = player.getUniqueId();

        // check to ensure no stacking of spell
        cleavers.remove(uuid);
        // ------------------------------------

        cleavers.add(uuid);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 0.5f);
    }

    @EventHandler
    public void onCleave(WeaponDamageEvent e) {

        // ignore ranged attacks
        if (e.getIsRanged()) {
            return;
        }

        Player pl = e.getPlayer();
        Entity en = e.getEntity();

        if (!cleavers.contains(pl.getUniqueId())) return;
        if (!(en instanceof LivingEntity)) return;

        cleavers.remove(pl.getUniqueId());

        LivingEntity victim = (LivingEntity) en;

        // particles, sounds
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.25f, 0.5f);
        victim.getWorld().spawnParticle(Particle.CRIT, victim.getEyeLocation(), 15, 0.5F, 0.5F, 0.5F, 0);

        // damage victim
        DamageUtil.damageEntityWeapon(DAMAGE_AMT, victim, pl, false);

        // damage nearby victims
        for (Entity nearby : victim.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {

            if (!(nearby instanceof LivingEntity)) continue;

            if (nearby == victim) continue;

            nearby.getWorld().spawnParticle(Particle.CRIT, ((LivingEntity) nearby).getEyeLocation(), 15, 0.5F, 0.5F, 0.5F, 0);
            DamageUtil.damageEntitySpell(DAMAGE_AMT, ((LivingEntity) nearby), pl);
        }
    }
}

