package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import net.minecraft.server.v1_15_R1.PacketPlayOutPlayerInfo;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Cloak extends Spell {

    private static final int DURATION = 5;
    private List<UUID> cloakers;
    private HashSet<UUID> hasDealtDamage;

    // constructor
    public Cloak() {
        super("Cloak",
                "For " + DURATION + " seconds, you vanish completely," +
                        "\ncausing you to appear invisible to" +
                        "\nplayers. During this time, you are" +
                        "\nimmune to damage from monsters!" +
                        "\nDealing damage ends the effect" +
                        "\nearly.",
                ChatColor.WHITE, ClassEnum.ROGUE, 10, 15);
        cloakers = new ArrayList<>();
        hasDealtDamage = new HashSet<>();
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // poof!
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.5f);
        pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                new Particle.DustOptions(Color.BLACK, 3));

        PacketPlayOutPlayerInfo packet =
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
                        ((CraftPlayer)pl).getHandle());

        // hide the player, prevent them from disappearing in tab
        for (Player ps : RunicCore.getCacheManager().getLoadedPlayers()) {
            ps.hidePlayer(plugin, pl);
            ((CraftPlayer)ps).getHandle().playerConnection.sendPacket(packet);
        }

        cloakers.add(pl.getUniqueId());
        pl.sendMessage(ChatColor.GRAY + "You vanished!");
        hasDealtDamage.add(pl.getUniqueId());

        // reappear after duration or upon dealing damage. can't be tracked async :(
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= DURATION || hasDealtDamage.contains(pl.getUniqueId())) {
                    this.cancel();
                    cloakers.remove(pl.getUniqueId());
                    for (Player ps : RunicCore.getCacheManager().getLoadedPlayers()) {
                        ps.showPlayer(plugin, pl);
                    }
                    pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.5f);
                    pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                            new Particle.DustOptions(Color.BLACK, 3));
                    pl.sendMessage(ChatColor.GRAY + "You reappeared!");
                    hasDealtDamage.remove(pl.getUniqueId());
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
        if (!(e.getVictim() instanceof Player)) return;
        Player pl = (Player) e.getVictim();
        if (cloakers.contains(pl.getUniqueId())) {
            e.setCancelled(true);
        }
    }

    /**
     * Reveal the player after dealing damage
     */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player pl = (Player) e.getDamager();
        if (!cloakers.contains(pl.getUniqueId())) return;
        if (hasDealtDamage.contains(pl.getUniqueId())) return;
        hasDealtDamage.add(pl.getUniqueId());
    }
}
