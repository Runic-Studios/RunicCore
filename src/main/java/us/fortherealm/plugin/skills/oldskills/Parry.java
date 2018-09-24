//package us.fortherealm.plugin.skills.skills;
//
//import us.fortherealm.plugin.oldskills.skilltypes.Skill;
//import us.fortherealm.plugin.oldskills.skilltypes.SkillItemType;
//import org.bukkit.*;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.entity.EntityDamageEvent;
//import org.bukkit.event.player.PlayerQuitEvent;
//import org.bukkit.util.Vector;
//
//import java.util.HashMap;
//import java.util.UUID;
//
//public class Parry extends Skill {
//
//    private HashMap<UUID, Long> noFall = new HashMap<>();
//
//    public Parry() {
//        super("Parry", "You leap", ChatColor.WHITE, ClickType.LEFT_CLICK_ONLY, 8);
//    }
//
//    @Override
//    public void onLeftClick(Player player, SkillItemType type) {
//
//        UUID uuid = player.getUniqueId();
//        Vector look = player.getLocation().getDirection();
//        Vector launchPath = new Vector(-look.getX(), 1.0, -look.getZ()).normalize();
//        //player.setFallDistance(-999999999.0f);
//        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 0.5f, 2.0f);
//        player.getWorld().spigot().playEffect(player.getLocation(),
//                Effect.CLOUD, 0, 0, 0.3F, 0.3F, 0.3F, 0.01F, 15, 16);
//        player.setVelocity(launchPath.multiply(1.5));
//        noFall.put(uuid, System.currentTimeMillis());
//
//        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
//            @Override
//            public void run() {
//                noFall.remove(uuid);
//                player.sendMessage(ChatColor.GRAY + "You lost safefall!");
//            }
//        },40); // after 2 secs
//    }
//
//    @EventHandler
//    public void onFallDamage(EntityDamageEvent e) {
//        if (e.getEntity() instanceof Player && e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
//            UUID uuid = e.getEntity().getUniqueId();
//            if (noFall.containsKey(uuid)) {
//                e.setCancelled(true);
//            } else if (!noFall.containsKey(uuid)) {
//                // Do nothing
//            }
//
//        }
//    }
//
//    @EventHandler
//    public void onQuit(PlayerQuitEvent e) {
//        UUID uuid = e.getPlayer().getUniqueId();
//        if (noFall.containsKey(uuid)) {
//            noFall.remove(uuid);
//        }
//    }
//}