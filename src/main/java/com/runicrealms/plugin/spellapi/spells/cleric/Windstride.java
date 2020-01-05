package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.entity.Entity;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@SuppressWarnings("FieldCanBeLocal")
public class Windstride extends Spell {

    // globals
    private static final int BUFF_DURATION = 10;
    private static final int SPEED_AMPLIFIER = 2;
    private static final int RADIUS = 10;

    // constructor
    public Windstride() {
        super("Windstride",
                "For " + BUFF_DURATION + " seconds, you grant a massive" +
                        "\nspeed boost to yourself and all" +
                        "\nallies within " + RADIUS + " blocks!",
                ChatColor.WHITE, 20, 15);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // apply the spell effects
        applySpell(pl);

        // if the user has a party, each party member gets the effects as well.
        if (RunicCore.getPartyManager().getPlayerParty(pl) != null) {

            for (Entity en : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {

                if (!(en instanceof LivingEntity)) continue;

                LivingEntity le = (LivingEntity) en;

                // skip our player, skip non-player entities
                if (le == pl)  continue;

                if (RunicCore.getPartyManager().getPlayerParty(pl) != null
                        && RunicCore.getPartyManager().getPlayerParty(pl).hasMember(le.getUniqueId())) {
                    // send player info message
                    le.sendMessage(ChatColor.GREEN + "You feel the wind at your back!");
                    applySpell((Player) le);
                }
            }
        }
    }

    private void applySpell(Player pl) {

        // begin sound effects
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5F, 0.7F);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5F, 0.7F);

        // add player effects
        pl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, BUFF_DURATION * 20, SPEED_AMPLIFIER));
        pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation(),
                25, 0, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.WHITE, 20));

        // begin system to remove effects
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(),
                () -> pl.sendMessage(ChatColor.GRAY + "The strength of the wind leaves you."), BUFF_DURATION * 20);
    }
}
