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
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Ironhide extends Spell implements MagicDamageSpell {

    private static final int DAMAGE = 15;
    private static final double DAMAGE_PER_LEVEL = 1.25;
    private static final int DURATION = 6;
    private final Set<UUID> ironPlayers = new HashSet<>();

    public Ironhide() {
        super("Ironhide",
                "For " + DURATION + "s, you gain a defensive " +
                        "enchantment! Enemies who strike " +
                        "you suffer (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL +
                        "x&7 lvl) magicʔ damage! You are slowed slightly for the duration.",
                ChatColor.WHITE, CharacterClass.WARRIOR, 16, 20);
    }

    /**
     * @param damager who hit the iron tank
     * @param victim  the player who cast Ironhide
     */
    private void damageAttacker(Player damager, Player victim) {
        victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.25f, 2.0f);
        victim.getWorld().spawnParticle
                (Particle.CRIT, victim.getEyeLocation(), 15, 0.5F, 0.5F, 0.5F, 0);
        DamageUtil.damageEntitySpell(DAMAGE, damager, victim, this);
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
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler
    public void onMagicDamage(MagicDamageEvent event) {
        if (!this.ironPlayers.contains(event.getVictim().getUniqueId())) return; // the tank
        if (!(event.getVictim() instanceof Player)) return;
        if (event.isCancelled()) return;
        if (event.getSpell().getName().equalsIgnoreCase(this.getName()))
            return; // prevent infinite looping
        Player damager = event.getPlayer();
        Player victim = (Player) event.getVictim();
        damageAttacker(damager, victim);
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent event) {

        if (!(event.getVictim() instanceof Player)) return;
        if (!(event.getDamager() instanceof LivingEntity)) return;
        LivingEntity damager = (LivingEntity) event.getDamager();
        Player victim = (Player) event.getVictim();
        UUID id = victim.getUniqueId();

        if (this.ironPlayers.contains(id)) {
            victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.25f, 2.0f);
            victim.getWorld().spawnParticle
                    (Particle.CRIT, victim.getEyeLocation(), 15, 0.5F, 0.5F, 0.5F, 0);
            DamageUtil.damageEntitySpell(DAMAGE, damager, victim, this);
        }
    }

    @EventHandler
    public void onPlayerDamage(PhysicalDamageEvent event) {
        if (!this.ironPlayers.contains(event.getVictim().getUniqueId())) return; // the tank
        if (!(event.getVictim() instanceof Player)) return;
        if (event.isCancelled()) return;
        Player damager = event.getPlayer();
        Player victim = (Player) event.getVictim();
        damageAttacker(damager, victim);
    }
}

