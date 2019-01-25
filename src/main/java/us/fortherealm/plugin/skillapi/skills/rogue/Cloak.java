package us.fortherealm.plugin.skillapi.skills.rogue;

import net.minecraft.server.v1_13_R2.PacketPlayOutPlayerInfo;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skillapi.skilltypes.Skill;
import us.fortherealm.plugin.skillapi.skilltypes.SkillItemType;

@SuppressWarnings("FieldCanBeLocal")
public class Cloak extends Skill {

    private static final int DURATION = 5;

    // constructor
    public Cloak() {
        super("Cloak", "For " + DURATION + " seconds, you vanish completely from view.",
                ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY, 1, 5);
    }

    @Override
    public void onRightClick(Player pl, SkillItemType type) {

        // poof!
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.5f);
        pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                new Particle.DustOptions(Color.BLACK, 3));
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer)pl).getHandle());
        for (Player ps : Bukkit.getOnlinePlayers()) {
            ps.hidePlayer(pl);
            // prevent player from disappearing in tab
            ((CraftPlayer)ps).getHandle().playerConnection.sendPacket(packet);
        }
        pl.sendMessage(ChatColor.GRAY + "You vanished!");

        // reappear after duration
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player ps : Bukkit.getOnlinePlayers()) {
                    ps.showPlayer(pl);
                }
                pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.5f);
                pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                        new Particle.DustOptions(Color.BLACK, 3));
                pl.sendMessage(ChatColor.GRAY + "You reappeared.");
            }
        }.runTaskLater(Main.getInstance(), DURATION*20);
    }
}
