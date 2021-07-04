package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Reflect extends Spell implements MagicDamageSpell {

    private static final int DAMAGE = 10;
    private static final double DAMAGE_PER_LEVEL = 0.75;
    private static final int DURATION = 6;
    private final List<UUID> reflectedPlrs = new ArrayList<>();

    public Reflect() {
        super("Reflect",
                "For " + DURATION + "s, you gain a reflective " +
                        "enchantment! Enemies who strike " +
                        "you suffer (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL +
                        "x&7 lvl) spellʔ damage!",
                ChatColor.WHITE, ClassEnum.WARRIOR, 16, 20);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        reflectedPlrs.add(pl.getUniqueId());
        pl.getWorld().spawnParticle
                (Particle.CRIT_MAGIC, pl.getEyeLocation(), 25, 0.5F, 0.5F, 0.5F, 0);
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
                && RunicCore.getPartyManager().getPlayerParty(victim).hasMember(damager)) return;

        if (this.reflectedPlrs.contains(id)) {
            victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.25f, 2.0f);
            victim.getWorld().spawnParticle
                    (Particle.CRIT, victim.getEyeLocation(), 15, 0.5F, 0.5F, 0.5F, 0);
            DamageUtil.damageEntitySpell(DAMAGE, damager, victim, this);
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
            DamageUtil.damageEntitySpell(DAMAGE, damager, victim, this);
        }
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}

