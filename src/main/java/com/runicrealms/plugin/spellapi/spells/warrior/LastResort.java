package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.UUID;

public class LastResort extends Spell {

    private static final int COOLDOWN = 360;
    private static final double PERCENT = .25;
    private static final int RADIUS = 5;
    private final HashSet<UUID> resorters;

    public LastResort() {
        super("Last Resort",
                "Upon death, you are resurrected✦ with " +
                        (int) (PERCENT * 100) + "% health, blasting back enemies within " +
                        RADIUS + " blocks! Last Resort cannot occur " +
                        "more than once every " + COOLDOWN + "s.",
                ChatColor.WHITE, ClassEnum.MAGE, 0, 0);
        this.resorters = new HashSet<>();
        this.setIsPassive(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRunicDeath(RunicDeathEvent e) {
        if (!hasPassive(e.getVictim().getUniqueId(), this.getName())) return;
        if (resorters.contains(e.getVictim().getUniqueId())) return;
        Player pl = e.getVictim();
        e.setCancelled(true);
        resorters.add(pl.getUniqueId());
        HealUtil.healPlayer((pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * PERCENT),
                pl, pl, false, this);
        beginLastResort(pl);
        pl.sendMessage(ChatColor.GREEN + "Your Last Resort has spared you from death!");
        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, () -> {
            pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
            pl.sendMessage(ChatColor.GREEN + "Your Last Resort is now available!");
            resorters.remove(e.getVictim().getUniqueId());
        }, COOLDOWN * 20L);
    }

    private void beginLastResort(Player pl) {
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5F, 1.0F);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
        pl.getWorld().spigot().strikeLightningEffect(pl.getLocation(), true);
        // knockback
        for (Entity entity : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!isValidEnemy(pl, entity)) continue;
            Vector force = pl.getLocation().toVector().subtract(entity.getLocation().toVector()).multiply(-0.25).setY(0.3);
            entity.setVelocity(force);
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.01F, 0.5F);
        }
    }
}

