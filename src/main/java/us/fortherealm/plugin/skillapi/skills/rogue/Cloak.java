package us.fortherealm.plugin.skillapi.skills.rogue;

import net.minecraft.server.v1_13_R2.PacketPlayOutPlayerInfo;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.FTRCore;
import us.fortherealm.plugin.skillapi.skilltypes.Skill;
import us.fortherealm.plugin.skillapi.skilltypes.SkillItemType;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Cloak extends Skill {

    private static final int DURATION = 5;
    private HashMap<UUID, Boolean> hasDealtDamage;

    // constructor
    public Cloak() {
        super("Cloak", "For " + DURATION + " seconds, you vanish completely from view." +
                "\nDealing damage ends the effect early.",
                ChatColor.WHITE, 10, 15);
        hasDealtDamage = new HashMap<>();
    }

    @Override
    public void executeSkill(Player pl, SkillItemType type) {

        // poof!
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.5f);
        pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                new Particle.DustOptions(Color.BLACK, 3));
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer)pl).getHandle());

        // hide the player, prevent them from disappearing in tab
        for (Player ps : Bukkit.getOnlinePlayers()) {
            ps.hidePlayer(plugin, pl);
            ((CraftPlayer)ps).getHandle().playerConnection.sendPacket(packet);
        }

        pl.sendMessage(ChatColor.GRAY + "You vanished!");
        hasDealtDamage.put(pl.getUniqueId(), false);

        // reappear after duration or upon dealing damage. can't be tracked async :(
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= DURATION || hasDealtDamage.get(pl.getUniqueId())) {
                    this.cancel();
                    for (Player ps : Bukkit.getOnlinePlayers()) {
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
        }.runTaskTimer(FTRCore.getInstance(), 0, 20);
    }

    /**
     * Reveal the player after dealing damage
     */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player pl = (Player) e.getDamager();
        if (!hasDealtDamage.containsKey(pl.getUniqueId())) return;
        hasDealtDamage.put(pl.getUniqueId(), true);
    }
}
