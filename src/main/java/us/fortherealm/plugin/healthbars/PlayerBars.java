//package us.fortherealm.plugin.healthbars;
//
//import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
//import org.bukkit.attribute.Attribute;
//import org.bukkit.boss.BarColor;
//import org.bukkit.boss.BarStyle;
//import org.bukkit.entity.Player;
//import us.fortherealm.plugin.Main;
//
//import java.util.List;
//
//public class PlayerBars {
//private BossBar bossBar = Bukkit.createBossBar("", BarColor.RED, BarStyle.SEGMENTED_10);
//private HashMap<UUID, Long> playersInCombat = new HashMap<>();
//    // display the enemy's health bar
//            if (victim instanceof Player) {
//        List<Player> players = bossBar.getPlayers();
//        if (players.contains(damager)) {
//            bossBar.removePlayer((Player) damager);
//        }
//        String name = plugin.getConfig().get(victim.getUniqueId() + ".info.name").toString();
//        int health = (int) (victim.getHealth() - e.getDamage());
//        int maxHealth = (int) (((Player) victim).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
//        bossBar = Bukkit.createBossBar(name + " " + ChatColor.RED + health + ChatColor.DARK_RED + " ❤", BarColor.RED, BarStyle.SEGMENTED_10);//"/" + maxHealth +
//        bossBar.setProgress((double) health / maxHealth);
//        bossBar.addPlayer((Player) damager);
//        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
//            if (!playersInCombat.containsKey(damager.getUniqueId())) {
//                bossBar.removePlayer((Player) damager);
//            }
//        }, 60L); // three seconds
//}
