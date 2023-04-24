package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Ironhide extends Spell implements MagicDamageSpell {
    private static final int DURATION = 6;
    private final Set<UUID> ironPlayers = new HashSet<>();
    private int damage = 0;
    private double damagePerLevel = 1.25;

    public Ironhide() {
        super("Ironhide", CharacterClass.WARRIOR);
        this.setDescription("For " + DURATION + "s, you gain a defensive " +
                "enchantment! Enemies who strike " +
                "you suffer (" + damage + " + &f" + damagePerLevel +
                "x&7 lvl) magicʔ damage! You are slowed slightly for the duration.");
    }

    /**
     * @param damager who hit the iron tank
     * @param victim  the player who cast Ironhide
     */
    private void damageAttacker(Player damager, Player victim) {
        victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.25f, 2.0f);
        victim.getWorld().spawnParticle
                (Particle.CRIT, victim.getEyeLocation(), 15, 0.5F, 0.5F, 0.5F, 0);
        DamageUtil.damageEntitySpell(damage, damager, victim, this);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        ironPlayers.add(player.getUniqueId());
        player.setGlowing(true);
        player.getWorld().spawnParticle
                (Particle.CRIT_MAGIC, player.getEyeLocation(), 25, 0.5F, 0.5F, 0.5F, 0);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 0.5f);
        addStatusEffect(player, RunicStatusEffect.SLOW_I, DURATION, false);

        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> {
            ironPlayers.remove(player.getUniqueId());
            player.setGlowing(false);
        }, DURATION * 20L);
    }

    @Override
    public double getMagicDamage() {
        return damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = (int) magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }

    @EventHandler
    public void onMagicDamage(MagicDamageEvent event) {
        if (!this.ironPlayers.contains(event.getVictim().getUniqueId())) return; // the tank
        if (!(event.getVictim() instanceof Player victim)) return;
        if (event.isCancelled()) return;
        if (event.getSpell().getName().equalsIgnoreCase(this.getName()))
            return; // prevent infinite looping
        Player damager = event.getPlayer();
        damageAttacker(damager, victim);
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent event) {
        if (!(event.getVictim() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof LivingEntity damager)) return;
        UUID id = victim.getUniqueId();
        if (this.ironPlayers.contains(id)) {
            victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.25f, 2.0f);
            victim.getWorld().spawnParticle
                    (Particle.CRIT, victim.getEyeLocation(), 15, 0.5F, 0.5F, 0.5F, 0);
            DamageUtil.damageEntitySpell(damage, damager, victim, this);
        }
    }

    @EventHandler
    public void onPlayerDamage(PhysicalDamageEvent event) {
        if (!this.ironPlayers.contains(event.getVictim().getUniqueId())) return; // the tank
        if (!(event.getVictim() instanceof Player victim)) return;
        if (event.isCancelled()) return;
        Player damager = event.getPlayer();
        damageAttacker(damager, victim);
    }
}

