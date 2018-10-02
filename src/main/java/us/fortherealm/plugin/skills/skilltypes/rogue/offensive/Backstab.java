package us.fortherealm.plugin.skills.skilltypes.rogue.offensive;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.Skill;

import java.util.HashMap;
import java.util.UUID;

public class Backstab extends Skill {

    public Backstab() {
        super("Backstab", "Self buff. For the duration, striking enemies from behind deals 150% dmg");
    }

    @Override
    public void executeSkill() {
        UUID uuid = getPlayer().getUniqueId();
        getPlayer().sendMessage(ChatColor.GREEN + "You are now backstabbing!");
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_IRONGOLEM_HURT, 0.5f, 1.0f);
        new BukkitRunnable() {
            @Override
            public void run() {
                Skill.delActiveSkill(Backstab.this);
                getPlayer().sendMessage(ChatColor.GRAY + "You are no longer backstabbing.");
            }
        }.runTaskLater(Main.getInstance(), 200L);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player))
            return;
        Player damager = (Player) e.getDamager();
        UUID uuid = damager.getUniqueId();

        for (Skill skill : Skill.getActiveSkills()) {
            if (!(skill instanceof Backstab))
                continue;

            if (!(e.getDamager().equals(skill.getPlayer())))
                continue;

            if (damager.getLocation().getDirection().dot(e.getEntity().getLocation().getDirection()) < 0.0D)
                return;

            e.setDamage(e.getDamage() * 1.5);
            e.getEntity().getWorld().spawnParticle(Particle.CRIT,
                    e.getEntity().getLocation().add(0, 1.5, 0), 30, 0, 0.2F, 0.2F, 0.2F);
            e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 0.5F, 0.6F);

            return;
        }
    }
}
