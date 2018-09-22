package us.fortherealm.plugin.skill.skills;

import us.fortherealm.plugin.skill.skilltypes.Skill;
import us.fortherealm.plugin.skill.skilltypes.SkillItemType;
import us.fortherealm.plugin.skill.skilltypes.skillutil.Cone;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class Enrage extends Skill {

    private HashMap<UUID, Player> ragers = new HashMap<>();

    public Enrage() {
        super("Enrage", "channel for 5s, buff for 10s",
                ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY, 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = null;
                for(UUID key : ragers.keySet()) {
                    player = ragers.get(key);
                    if (player != null) {
                        player.getWorld().spawnParticle(Particle.REDSTONE, player.getEyeLocation(), 5, 0.2f,  0.2f, 0.2f, 0.01);
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20L, 3L);
    }

    @Override
    public void onRightClick(Player player, SkillItemType type) {

        UUID uuid = player.getUniqueId();
        ragers.put(uuid, player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 99));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100, 128));
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 2));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 1.0f);
        Cone.coneEffect(player, Particle.REDSTONE, 4, 0, 3);
        player.sendMessage(ChatColor.GRAY + "You begin to feel a surge of power!");
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage(ChatColor.GREEN + "You become enraged!");
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_GROWL, 0.5f, 1.0f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 1));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.sendMessage(ChatColor.GRAY + "You no longer feel enraged.");
                        ragers.remove(uuid);
                    }
                }.runTaskLater(plugin, 200L);
            }
        }.runTaskLater(plugin, 100L);
    }
}
