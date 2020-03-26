package com.runicrealms.plugin.player.combat;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.mounts.MountListener;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class CombatListener implements Listener {

    private static final int PARTY_TAG_RANGE = 100;

    @EventHandler
    public void onMobDamage(MobDamageEvent e) {

        // only listen for player, or arrows. ignore NPCs
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getDamager() instanceof Player) && !(e.getDamager() instanceof Arrow)) return;
        if (!(e.getVictim() instanceof LivingEntity)) return;
        if (e.getVictim().hasMetadata("NPC")) return;

        // grab our variables
        Player damager;
        if (e.getDamager() instanceof Arrow && ((Arrow) e.getDamager()).getShooter() instanceof Player) {
            damager = (Player) ((Arrow) e.getDamager()).getShooter();
        } else {
            damager = (Player) e.getDamager();
        }

        if (damager == null) return;
        UUID damagerID = damager.getUniqueId();

        // remove their mount
        dismount(damager);

        // add/refresh their combat timer every hit
        RunicCore.getCombatManager().addPlayer(damagerID);

        // if the damager has a party, tag their party members and inform them
        tagPartyCombat(damager, e.getVictim());
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {
        tagCombat(e.getPlayer(), e.getEntity());
    }

    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        tagCombat(e.getPlayer(), e.getEntity());
    }

    private void tagCombat(Player damager, Entity victim) {

        // ignore NPCs
        if (victim.hasMetadata("NPC")) return;
        // only listen for players (mobs handled separately)
        if (!(victim instanceof Player)) return;

        // ignore party members
        if (RunicCore.getPartyManager().getPlayerParty(damager) != null
                && RunicCore.getPartyManager().getPlayerParty(damager).hasMember((Player) victim)) {
            return;
        }

        // player cannot damage themselves
        if (damager == victim) return;

        UUID damagerID = damager.getUniqueId();

        dismount(damager);

        // add/refresh their combat timer every hit
        RunicCore.getCombatManager().addPlayer(damagerID);
        if (victim instanceof Player) {
            RunicCore.getCombatManager().getPvPers().add(damagerID);
        }

        // if the damager has a party, tag their party members and inform them
        tagPartyCombat(damager, victim);

        // apply same mechanics to victim if the victim is a player
        if (!(victim instanceof Player)) return;
        UUID victimID = victim.getUniqueId();
        dismount((Player) victim);
        RunicCore.getCombatManager().addPlayer(victimID);
        tagPartyCombat((Player) victim, victim);
    }

    private void tagPartyCombat(Player pl, Entity e) {
        if (RunicCore.getPartyManager().getPlayerParty(pl) != null) {

            for (Player member : RunicCore.getPartyManager().getPlayerParty(pl).getPlayerMembers()) {

                if (pl.getLocation().getWorld() != member.getLocation().getWorld()) continue;

                // only tag players in 100 block range
                if (pl.getLocation().distance(member.getLocation()) > PARTY_TAG_RANGE) continue;

                if (member == pl) continue;

                dismount(member);

                RunicCore.getCombatManager().addPlayer(member.getUniqueId());
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
