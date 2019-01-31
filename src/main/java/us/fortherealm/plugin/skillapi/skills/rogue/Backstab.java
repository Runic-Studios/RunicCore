package us.fortherealm.plugin.skillapi.skills.rogue;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.skillapi.skilltypes.Skill;
import us.fortherealm.plugin.skillapi.skilltypes.SkillItemType;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Backstab extends Skill {

    // globals, duration measured in seconds
    private static final double DAMAGE_MULT = 1.5;
    private static final int DURATION = 10;
    private HashMap<UUID, Long> stabbers = new HashMap<>();

    // constructor
    public Backstab() {
        super("Backstab",
                "For " + DURATION + " seconds, striking enemies from\nbehind deals " + (int) (DAMAGE_MULT*100) + "% weapon damage!",
                ChatColor.WHITE, 1, 5);
    }

    // skill execute code
    @Override
    public void executeSkill(Player player, SkillItemType type) {
        UUID uuid = player.getUniqueId();
        stabbers.put(uuid, System.currentTimeMillis());
        player.sendMessage(ChatColor.GREEN + "You are now backstabbing!");
        // TODO: add particle effect here
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 0.5f, 1.0f);
        new BukkitRunnable() {
            @Override
            public void run() {
                stabbers.remove(uuid);
                player.sendMessage(ChatColor.GRAY + "You are no longer backstabbing.");
            }
        }.runTaskLater(plugin, DURATION*20L);
    }

    // extra damage listener
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if(e.getDamager() instanceof Player) {
            Player player = (Player) e.getDamager();
            UUID uuid = player.getUniqueId();
            if(stabbers.containsKey(uuid)
                    && player.getLocation().getDirection().dot(e.getEntity().getLocation().getDirection()) >= 0.0D) {
                // if the dot-product of both entitys' vectors is greater than 0 (positive),
                // then they're facing the same direction and it's a back stab
                e.setDamage(e.getDamage() * DAMAGE_MULT);
                e.getEntity().getWorld().spawnParticle(Particle.REDSTONE, e.getEntity().getLocation(),
                        25, 0, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.RED, 1));

                e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 0.5F, 0.6F);
            }
        }
    }
}

