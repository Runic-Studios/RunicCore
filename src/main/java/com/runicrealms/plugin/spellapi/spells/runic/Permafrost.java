package com.runicrealms.plugin.spellapi.spells.runic;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Permafrost extends Spell {

    private static final int DURATION_SPELL = 6;
    private static final int DURATION_SLOW = 2;
    private static final int SLOW_MULT = 1;
    private List<UUID> frosters;

    public Permafrost() {
        super ("Permafrost",
                "For " + DURATION_SPELL + " seconds, your melee weapon" +
                        "\nattacks gain an icy enchantment, slowing" +
                        "\n" + "enemies hit for " + DURATION_SLOW + " second(s).",
                ChatColor.WHITE, 12, 12);
        frosters = new ArrayList<>();
    }

    // spell execute code
    @Override
    public void executeSpell(Player player, SpellItemType type) {

        UUID uuid = player.getUniqueId();
        frosters.add(uuid);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 2.0f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.25f, 1.0f);

        new BukkitRunnable() {
            @Override
            public void run() {
                frosters.remove(uuid);
            }
        }.runTaskLater(RunicCore.getInstance(), DURATION_SPELL*20L);
    }

    @EventHandler
    public void onIcyHit(WeaponDamageEvent e) {

        Player pl = e.getPlayer();
        Entity en = e.getEntity();

        if (!frosters.contains(pl.getUniqueId())) return;
        if (!(en instanceof LivingEntity)) return;

        LivingEntity victim = (LivingEntity) en;

        // particles, sounds
        victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.25f, 1.75f);
        victim.getWorld().spawnParticle(Particle.BLOCK_DUST, victim.getEyeLocation(),
                5, 0.5F, 0.5F, 0.5F, 0, Material.PACKED_ICE.createBlockData());

        // slow victim
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, DURATION_SLOW*20, SLOW_MULT));
    }
}

