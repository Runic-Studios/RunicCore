package me.skyfallin.plugin.skill.skills;

import me.skyfallin.plugin.skill.skilltypes.Skill;
import me.skyfallin.plugin.skill.skilltypes.SkillItemType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.UUID;

public class Backstab extends Skill {

    HashMap<UUID, Long> stabbers = new HashMap<>();
    public Backstab() {
        super("Backstab", "Self buff. For the duration, striking enemies from behind deals 150% dmg",
                ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY, 20);
    }

    @Override
    public void onRightClick(Player player, SkillItemType type) {
        UUID uuid = player.getUniqueId();
        stabbers.put(uuid, System.currentTimeMillis());
        player.sendMessage(ChatColor.GREEN + "You are now backstabbing!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRONGOLEM_HURT, 0.5f, 1.0f);
        new BukkitRunnable() {
            @Override
            public void run() {
                stabbers.remove(uuid);
                player.sendMessage(ChatColor.GRAY + "You are no longer backstabbing.");
            }
        }.runTaskLater(plugin, 200L);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if(e.getDamager() instanceof Player) {
            Player player = (Player) e.getDamager();
            UUID uuid = player.getUniqueId();
            if(stabbers.containsKey(uuid)
                    && player.getLocation().getDirection().dot(e.getEntity().getLocation().getDirection()) >= 0.0D) {
                // if the dot-product of both entitys' vectors is greater than 0 (positive),
                // then they're facing the same direction and it's a back stab
                e.setDamage(e.getDamage() * 1.5);
                e.getEntity().getWorld().spawnParticle(Particle.CRIT,
                        e.getEntity().getLocation().add(0,1.5,0), 30, 0, 0.2F, 0.2F, 0.2F);
                e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 0.5F, 0.6F);
            }
        }
    }
}
