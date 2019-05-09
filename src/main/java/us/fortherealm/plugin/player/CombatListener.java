package us.fortherealm.plugin.player;

import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import us.fortherealm.plugin.FTRCore;

import java.util.UUID;

public class CombatListener implements Listener {

    @EventHandler
    public void onCombat(EntityDamageByEntityEvent e) {

        // only listen for two players, or arrows
        if (!(e.getDamager() instanceof Player) && !(e.getDamager() instanceof Arrow)) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;

        // ignore NPCs
        if (e.getEntity().hasMetadata("NPC")) return;

        // grab our variables
        Player damager;
        if (e.getDamager() instanceof Arrow) {
            damager = (Player) ((Arrow) e.getDamager()).getShooter();
        } else {
            damager = (Player) e.getDamager();
        }

        // player cannot damage themselves
        if (damager == e.getEntity()) return;

        UUID damagerID = damager.getUniqueId();

        // inform the players when they first enter combat
        if (!FTRCore.getCombatManager().getPlayersInCombat().containsKey(damagerID)) {
            damager.sendMessage(ChatColor.RED + "You have entered combat!");
        }

        // add/refresh their combat timer every hit
        FTRCore.getCombatManager().addPlayer(damagerID, System.currentTimeMillis());
        if (e.getEntity() instanceof Player) {
            FTRCore.getCombatManager().getPvPers().add(damagerID);
        }

        // if the damager has a party, tag their party members and inform them
        tagPartyCombat(damager, e.getEntity());

        // apply same mechanics to victim if the victim is a player
        if (!(e.getEntity() instanceof Player)) return;
        Player victim = (Player) e.getEntity();
        UUID victimID = victim.getUniqueId();

        if (!FTRCore.getCombatManager().getPlayersInCombat().containsKey(victimID)) {
            victim.sendMessage(ChatColor.RED + "You have entered combat!");
        }

        FTRCore.getCombatManager().addPlayer(victimID, System.currentTimeMillis());

        tagPartyCombat(victim, victim);
    }

    private void tagPartyCombat(Player pl, Entity e) {
        if (FTRCore.getPartyManager().getPlayerParty(pl) != null) {

            for (Player member : FTRCore.getPartyManager().getPlayerParty(pl).getPlayerMembers()) {
                if (member == pl) continue;
                if (!FTRCore.getCombatManager().getPlayersInCombat().containsKey(member.getUniqueId())) {
                    member.sendMessage(ChatColor.RED + "Your party has entered combat!");
                }
                FTRCore.getCombatManager().addPlayer(member.getUniqueId(), System.currentTimeMillis());
                if (e instanceof Player) {
                    FTRCore.getCombatManager().getPvPers().add(member.getUniqueId());
                }
            }
        }
    }
}
