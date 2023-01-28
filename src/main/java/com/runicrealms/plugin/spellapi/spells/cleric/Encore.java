package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Encore extends Spell implements MagicDamageSpell {
    private static final int COOLDOWN = 8;
    private static final int DAMAGE = 10;
    private static final int DURATION = 2;
    private static final int RADIUS = 4;
    private static final double DAMAGE_PER_LEVEL = 1.0D;
    private final Set<UUID> encoreCooldowns = new HashSet<>();

    public Encore() {
        super("Encore",
                "Every " + COOLDOWN + "s, your next basic attack " +
                        "deals an extra (" + DAMAGE + " + &f" + (int) DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage! " +
                        "It also reduces the active spell cooldowns of all allies " +
                        "within " + RADIUS + " blocks by " + DURATION + "s!",
                ChatColor.WHITE, CharacterClass.ROGUE, 0, 0);
        this.setIsPassive(true);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler
    public void onWeaponHit(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isBasicAttack()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (encoreCooldowns.contains(event.getPlayer().getUniqueId())) return;
        Player player = event.getPlayer();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.25F, 1.0F);
        event.getVictim().getWorld().spawnParticle
                (Particle.NOTE, event.getVictim().getLocation().add(0, 1.5, 0),
                        5, 1.0F, 0, 0, 0);
        DamageUtil.damageEntitySpell(DAMAGE, event.getVictim(), player, this);
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), RADIUS, RADIUS, RADIUS, target -> isValidAlly(player, target))) {
            Player ally = (Player) entity;
            ConcurrentHashMap.KeySetView<Spell, Long> spellsOnCD = RunicCore.getSpellAPI().getSpellsOnCooldown(ally.getUniqueId());
            if (spellsOnCD == null) continue;
            for (Spell spell : spellsOnCD) {
                RunicCore.getSpellAPI().reduceCooldown(ally, spell, DURATION);
            }
        }
        encoreCooldowns.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> encoreCooldowns.remove(player.getUniqueId()), COOLDOWN * 20L);
    }
}

