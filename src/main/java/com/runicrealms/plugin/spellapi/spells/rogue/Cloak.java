package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Cloak extends Spell {

    private static final int DURATION = 5;
    private final Set<UUID> cloakers;
    private static final HashSet<UUID> markedForEarlyReveal = new HashSet<>();

    public Cloak() {
        super("Cloak",
                "For " + DURATION + " seconds, you vanish completely, " +
                        "causing you to appear invisible to " +
                        "players. During this time, you are " +
                        "immune to damage from monsters! " +
                        "Dealing damage or taking damage from " +
                        "players ends the effect early.",
                ChatColor.WHITE, ClassEnum.ROGUE, 30, 15);
        cloakers = new HashSet<>();
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // poof!
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.5f);
        pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                new Particle.DustOptions(Color.BLACK, 3));

        PacketPlayOutPlayerInfo packet =
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
                        ((CraftPlayer) pl).getHandle());

        // hide the player, prevent them from disappearing in tab
        for (Player ps : RunicCore.getCacheManager().getLoadedPlayers()) {
            ps.hidePlayer(plugin, pl);
            ((CraftPlayer) ps).getHandle().playerConnection.sendPacket(packet);
        }

        cloakers.add(pl.getUniqueId());
        pl.sendMessage(ChatColor.GRAY + "You vanished!");

        // reappear after duration or upon dealing damage. can't be tracked async :(
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= DURATION || markedForEarlyReveal.contains(pl.getUniqueId())) {
                    this.cancel();
                    Predator.getPredators().add(pl.getUniqueId());
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> Predator.getPredators().remove(pl.getUniqueId()), Predator.getDuration() * 20L);
                    cloakers.remove(pl.getUniqueId());
                    for (Player ps : RunicCore.getCacheManager().getLoadedPlayers()) {
                        ps.showPlayer(plugin, pl);
                    }
                    pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.5f);
                    pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                            new Particle.DustOptions(Color.BLACK, 3));
                    pl.sendMessage(ChatColor.GRAY + "You reappeared!");
                    markedForEarlyReveal.remove(pl.getUniqueId());
                } else {
                    count++;
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20);
    }

    /**
     * Player is immune to mob attacks
     */
    @EventHandler
    public void onDamage(MobDamageEvent e) {
        if (!(e.getVictim() instanceof Player))
            return;
        Player pl = (Player) e.getVictim();
        if (cloakers.contains(pl.getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {
        if (!(cloakers.contains(e.getPlayer().getUniqueId())
                || cloakers.contains(e.getVictim().getUniqueId()))) return;
        if (cloakers.contains(e.getPlayer().getUniqueId()))
            markedForEarlyReveal.add(e.getPlayer().getUniqueId());
        else
            markedForEarlyReveal.add(e.getVictim().getUniqueId());
    }

    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        if (!(cloakers.contains(e.getPlayer().getUniqueId())
                || cloakers.contains(e.getVictim().getUniqueId()))) return;
        if (cloakers.contains(e.getPlayer().getUniqueId()))
            markedForEarlyReveal.add(e.getPlayer().getUniqueId());
        else
            markedForEarlyReveal.add(e.getVictim().getUniqueId());
    }

    public static HashSet<UUID> getMarkedForEarlyReveal() {
        return markedForEarlyReveal;
    }
}
