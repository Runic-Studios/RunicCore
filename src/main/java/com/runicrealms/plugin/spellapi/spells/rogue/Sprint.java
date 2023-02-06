package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Sprint extends Spell implements MagicDamageSpell {

    private static final int DAMAGE_AMOUNT = 5;
    private static final double DAMAGE_PER_LEVEL = 1.75;
    private static final int DURATION = 5;
    private final Map<UUID, BukkitTask> sprintTasks = new HashMap<>();

    public Sprint() {
        super("Sprint",
                "For " + DURATION + "s, you gain a " +
                        "massive boost of speed! While the speed persists, your first melee attack against " +
                        "an enemy deals (" + DAMAGE_AMOUNT + " + &f" + DAMAGE_PER_LEVEL +
                        "x&7 lvl) magicʔ damage!",
                ChatColor.WHITE, CharacterClass.ROGUE, 10, 10);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        addStatusEffect(player, RunicStatusEffect.SPEED_II, DURATION, false);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5F, 1.0F);
        new HorizontalCircleFrame(1, false).playParticle(player, Particle.TOTEM, player.getLocation(), Color.FUCHSIA);
        new HorizontalCircleFrame(1, false).playParticle(player, Particle.TOTEM, player.getEyeLocation(), Color.FUCHSIA);
        BukkitTask sprintDamageTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> sprintTasks.remove(player.getUniqueId()), DURATION * 20L);
        sprintTasks.put(player.getUniqueId(), sprintDamageTask);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!event.isBasicAttack()) return;
        if (!sprintTasks.containsKey(event.getPlayer().getUniqueId())) return;
        Player player = event.getPlayer();
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRASS_BREAK, 0.5f, 0.5f);
        player.getWorld().spawnParticle(Particle.CRIT_MAGIC, event.getVictim().getEyeLocation(), 15, 0.5f, 0.5f, 0.5f, 0);
        DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, event.getVictim(), player, this);
        sprintTasks.remove(player.getUniqueId());
    }
}

