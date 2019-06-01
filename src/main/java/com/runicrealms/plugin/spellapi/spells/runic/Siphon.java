package com.runicrealms.plugin.spellapi.spells.runic;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.player.ManaManager;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Siphon extends Spell {

    private static final int AMOUNT = 5;
    private static final int DURATION = 5;
    private List<UUID> drainers;

    public Siphon() {
        super ("Siphon",
                "For " + DURATION + " seconds, your melee weapon" +
                        "\nattacks drain " + AMOUNT + " mana from your" +
                        "\nenemies!",
                ChatColor.WHITE, 1, 1);
        drainers = new ArrayList<>();
    }

    // spell execute code
    @Override
    public void executeSpell(Player player, SpellItemType type) {

        UUID uuid = player.getUniqueId();
        drainers.add(uuid);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.25f, 2.0f);
        player.getWorld().spawnParticle(Particle.SPELL_WITCH, player.getEyeLocation(), 15, 0.5F, 0.5F, 0.5F, 0);

        new BukkitRunnable() {
            @Override
            public void run() {
                drainers.remove(uuid);
            }
        }.runTaskLater(RunicCore.getInstance(), DURATION*20L);
    }

    @EventHandler
    public void onDrainingHit(WeaponDamageEvent e) {

        Player pl = e.getPlayer();
        Entity en = e.getEntity();

        if (!drainers.contains(pl.getUniqueId())) return;
        if (!(en instanceof Player)) return;

        Player victim = (Player) en;

        // skip party members
        if (RunicCore.getPartyManager().getPlayerParty(pl) != null
                && RunicCore.getPartyManager().getPlayerParty(pl).hasMember(victim.getUniqueId())) return;

        // particles, sounds
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WITCH_DRINK, 0.25f, 2f);
        victim.getWorld().spawnParticle(Particle.SPELL_WITCH, victim.getEyeLocation(), 3, 0.3F, 0.3F, 0.3F, 0);

        // reduce victims mana if they are not 0
        if (RunicCore.getManaManager().getCurrentManaList().get(victim.getUniqueId()) <= 0) return;
        RunicCore.getManaManager().subtractMana(victim, AMOUNT);
    }
}

