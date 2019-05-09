package us.fortherealm.plugin.skillapi.skills.rogue;

import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.skillapi.skilltypes.Skill;
import us.fortherealm.plugin.skillapi.skilltypes.SkillItemType;
import us.fortherealm.plugin.utilities.DamageUtil;
import us.fortherealm.plugin.utilities.HologramUtil;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Backstab extends Skill {

    private static final int DAMAGE_AMT = 10;
    private static final int DURATION = 10;
    private HashMap<UUID, Long> stabbers = new HashMap<>();

    public Backstab() {
        super("Backstab",
                "For " + DURATION + " seconds, striking enemies from\n"
                        + "behind deals " + DAMAGE_AMT + " additional spell damage!",
                ChatColor.WHITE, 15, 10);
    }

    @Override
    public void executeSkill(Player player, SkillItemType type) {
        UUID uuid = player.getUniqueId();
        stabbers.put(uuid, System.currentTimeMillis());
        player.sendMessage(ChatColor.GREEN + "You are now backstabbing!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 0.5f, 1.0f);
        new BukkitRunnable() {
            @Override
            public void run() {
                stabbers.remove(uuid);
                player.sendMessage(ChatColor.GRAY + "You are no longer backstabbing.");
            }
        }.runTaskLater(plugin, DURATION*20L);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {

        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;
        if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        Player pl = (Player) e.getDamager();
        UUID uuid = pl.getUniqueId();
        LivingEntity le = (LivingEntity) e.getEntity();

        if (le.hasMetadata("NPC")) return;

        if (!stabbers.containsKey(uuid)) return;

        // if the dot-product of both entitys' vectors is greater than 0 (positive),
        // then they're facing the same direction and it's a backstab
        if (!(pl.getLocation().getDirection().dot(e.getEntity().getLocation().getDirection()) >= 0.0D)) return;

        // prevent an infinite damage loop
        e.setCancelled(true);
        DamageUtil.damageEntityMagic((DAMAGE_AMT), le, pl);

        le.getWorld().spawnParticle(Particle.CRIT_MAGIC, le.getEyeLocation(), 25, 0.25, 0.25, 0.25, 0);

        le.getWorld().playSound(le.getLocation(), Sound.ENTITY_WITCH_HURT, 0.5f, 0.8f);
    }
}

