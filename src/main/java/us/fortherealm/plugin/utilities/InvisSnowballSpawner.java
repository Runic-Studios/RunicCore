package us.fortherealm.plugin.utilities;

import net.minecraft.server.v1_13_R2.PacketPlayOutEntityDestroy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Consumer;

/**
 * Implements the Bukkit Consumer interface to spawn an armor stand that is invisible
 * and has no displayname to prevent "flickering" of armor stands.
 * @author Skyfallin_
 */
public class InvisSnowballSpawner implements Consumer<Snowball> {

    @Override
    public void accept(Snowball snowball) {

        // send packets to make snowball invisible
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(snowball.getEntityId());
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }

        //snowball.setVisible(false);
        //snowball.setCollidable(false);
        //armorStand.setCustomNameVisible(true);
        //armorStand.setInvulnerable(true);
        //armorStand.setGravity(false);
        //armorStand.setMarker(true);
        //armorStand.setCustomName(ChatColor.DARK_GRAY + "");
    }
}
