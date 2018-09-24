//package us.fortherealm.plugin.skills.skills;
//
//import us.fortherealm.plugin.oldskills.skilltypes.Skill;
//import us.fortherealm.plugin.oldskills.skilltypes.SkillItemType;
//import org.bukkit.*;
//import org.bukkit.entity.Player;
//import org.bukkit.potion.PotionEffect;
//import org.bukkit.potion.PotionEffectType;
//import org.bukkit.scheduler.BukkitRunnable;
//
//import java.util.HashMap;
//import java.util.UUID;
//
//// TODO: add party check for movement boost
//public class Windstride extends Skill {
//
//    private HashMap<UUID, Player> striders = new HashMap<>();
//
//    public Windstride() {
//        super("Windstride", "You increase the movement speed of yourself and all party members by 50 units."
//                , ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY, 20);
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                Player player = null;
//                for(UUID key : striders.keySet()) {
//                    player = striders.get(key);
//                    if (player != null) {
//                        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0,1.5,0), 1 /*particle amount */, 0.0f,  0.0f, 0.0f, 0.01);
//                    }
//                }
//            }
//        }.runTaskTimerAsynchronously(plugin, 20L, 3L); // bit of a period to reduce lag, bit of a delay to reduce lag on initialization
//    }
//
//    @Override
//    public void onRightClick(Player player, SkillItemType type) {
//        UUID uuid = player.getUniqueId();
//        // TODO: change speed of all party members
//        if (striders.containsKey(uuid)) {
//            player.sendMessage("You already benefited from windstride and cant get it again.");
//        } else {
//            striders.put(uuid, player);
//            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_LAUNCH, 0.5F, 0.7F);
//            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 0.5F, 0.7F);
//            player.sendMessage(ChatColor.GREEN + "You feel the wind at your back!");
//            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1)); //200 ticks = 10s (Speed I)
//            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
//                @Override
//                public void run() {
//                    player.sendMessage(ChatColor.GRAY + "The strength of the wind leaves you.");
//                    striders.remove(uuid); // remove particle effect
//                }
//            }, 200);
//        }
//    }
//}
