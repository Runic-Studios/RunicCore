package com.runicrealms.plugin.spellapi.spells.runic.active;

import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import com.runicrealms.plugin.RunicCore;
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
public class Reflect extends Spell {

    private static final int DAMAGE = 2;
    private static final int DURATION = 8;
    private List<UUID> reflectedPlrs = new ArrayList<>();

    public Reflect() {
        super("Reflect",
                "For " + DURATION + " seconds, you gain a" +
                        "\nreflective enchantment! Enemies who strike" +
                        "\n you suffer " + DAMAGE + " spellʔ damage" +
                        "\neach attack!",
                ChatColor.WHITE,16, 15);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        reflectedPlrs.add(pl.getUniqueId());
        pl.getWorld().spawnParticle
                (Particle.CRIT, pl.getEyeLocation(), 25, 0.5F, 0.5F, 0.5F, 0);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 2.0f);

        new BukkitRunnable() {
            @Override
            public void run() {
                reflectedPlrs.remove(pl.getUniqueId());
            }
        }.runTaskLaterAsynchronously(RunicCore.getInstance(), DURATION*20L);
    }

    @EventHandler
    public void onPlayerDamage(WeaponDamageEvent e) {

        if (!(e.getEntity() instanceof Player)) return;
        Player damager = e.getPlayer();
        Player victim = (Player) e.getEntity();
        UUID id = victim.getUniqueId();

        // skip party members
        if (RunicCore.getPartyManager().getPlayerParty(victim) != null
                && RunicCore.getPartyManager().getPlayerParty(victim).hasMember(damager.getUniqueId())) return;

        if (this.reflectedPlrs.contains(id)) {
            victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.25f, 2.0f);
            victim.getWorld().spawnParticle
                    (Particle.CRIT, victim.getEyeLocation(), 15, 0.5F, 0.5F, 0.5F, 0);
            DamageUtil.damageEntitySpell(DAMAGE, damager, victim, false);
        }
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent e) {

        if (!(e.getVictim() instanceof Player)) return;
        if (!(e.getDamager() instanceof LivingEntity)) return;
        LivingEntity damager = (LivingEntity) e.getDamager();
        Player victim = (Player) e.getVictim();
        UUID id = victim.getUniqueId();

        if (this.reflectedPlrs.contains(id)) {
            victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.25f, 2.0f);
            victim.getWorld().spawnParticle
                    (Particle.CRIT, victim.getEyeLocation(), 15, 0.5F, 0.5F, 0.5F, 0);
            DamageUtil.damageEntitySpell(DAMAGE, damager, victim, false);
        }
    }
}

