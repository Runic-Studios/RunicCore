package com.runicrealms.plugin.player.combat;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.mounts.MountListener;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class CombatListener implements Listener {

    private static int RANGE = 100;

    @EventHandler
    public void onCombat(EntityDamageByEntityEvent e) {

        // only listen for two players, or arrows
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getDamager() instanceof Player) && !(e.getDamager() instanceof Arrow)) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;

        // ignore NPCs
        if (e.getEntity().hasMetadata("NPC")) return;

        // grab our variables
        Player damager;
        if (e.getDamager() instanceof Arrow && ((Arrow) e.getDamager()).getShooter() instanceof Player) {
            damager = (Player) ((Arrow) e.getDamager()).getShooter();
        } else {
            damager = (Player) e.getDamager();
        }

        if (damager == null) return;
        int slot = damager.getInventory().getHeldItemSlot();
        if (slot != 0) return;

        // ignore party members
        if (e.getEntity() instanceof Player &&
                RunicCore.getPartyManager().getPlayerParty(damager) != null
                && RunicCore.getPartyManager().getPlayerParty(damager).hasMember((Player) e.getEntity())) {
            return;
        }

        // player cannot damage themselves
        if (damager == e.getEntity()) return;

        UUID damagerID = damager.getUniqueId();

        // inform the players when they first enter combat
        if (!RunicCore.getCombatManager().getPlayersInCombat().containsKey(damagerID)) {
            damager.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You have entered combat!"));
        }

        dismount(damager);

        // add/refresh their combat timer every hit
        RunicCore.getCombatManager().addPlayer(damagerID, System.currentTimeMillis());
        if (e.getEntity() instanceof Player) {
            RunicCore.getCombatManager().getPvPers().add(damagerID);
        }

        // if the damager has a party, tag their party members and inform them
        tagPartyCombat(damager, e.getEntity());

        // apply same mechanics to victim if the victim is a player
        if (!(e.getEntity() instanceof Player)) return;
        Player victim = (Player) e.getEntity();
        UUID victimID = victim.getUniqueId();

        if (!RunicCore.getCombatManager().getPlayersInCombat().containsKey(victimID)) {
            victim.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You have entered combat!"));
        }

        dismount(victim);

        RunicCore.getCombatManager().addPlayer(victimID, System.currentTimeMillis());

        tagPartyCombat(victim, victim);
    }

    private void tagPartyCombat(Player pl, Entity e) {
        if (RunicCore.getPartyManager().getPlayerParty(pl) != null) {

            for (Player member : RunicCore.getPartyManager().getPlayerParty(pl).getPlayerMembers()) {

                if (pl.getLocation().getWorld() != member.getLocation().getWorld()) continue;

                // only tag players in 100 block range
                if (pl.getLocation().distance(member.getLocation()) > RANGE) continue;

                if (member == pl) continue;
                if (!RunicCore.getCombatManager().getPlayersInCombat().containsKey(member.getUniqueId())) {
                    member.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Your party has entered combat!"));
                }

                dismount(member);

                RunicCore.getCombatManager().addPlayer(member.getUniqueId(), System.currentTimeMillis());
                if (e instanceof Player) {
                    RunicCore.getCombatManager().getPvPers().add(member.getUniqueId());
                }
            }
        }
    }

    private void dismount(Player pl) {
        if (MountListener.mounted.containsKey(pl.getUniqueId())) {
            MountListener.mounted.get(pl.getUniqueId()).remove();
            MountListener.mounted.remove(pl.getUniqueId());
            pl.playSound(pl.getLocation(), Sound.ENTITY_HORSE_HURT, 0.5f, 1.0f);
            pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation(),
                    25, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.fromRGB(210, 180, 140), 20));
        }
    }
}
