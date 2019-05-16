package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.entity.Entity;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@SuppressWarnings("FieldCanBeLocal")
public class Windstride extends Spell {

    // globals
    private static final int BUFF_DURATION = 10;
    private static final int SPEED_AMPLIFIER = 1;
    private static final int RADIUS = 10;

    // constructor
    public Windstride() {
        super("Windstride",
                "For " + BUFF_DURATION + " seconds, you grant a speed boost" +
                        "\nto yourself and all party members" +
                        "\nwithin " + RADIUS + " blocks!",
                ChatColor.WHITE, 1, 5);
    }

    // skill execute code
    @Override
    public void executeSkill(Player pl, SpellItemType type) {

        // apply the skill effects
        applySkill(pl);

        // if the user has a party, each party member gets the effects as well.
        if (RunicCore.getPartyManager().getPlayerParty(pl) != null) {
            for (Entity e : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {

                // skip our player, skip non-player entities
                if (e == pl) { continue; }
                if (!(e instanceof Player)) { continue; }

                if (RunicCore.getPartyManager().getPlayerParty(pl).hasMember(e.getUniqueId())) {
                    applySkill((Player) e);
                }
            }
        }
    }

    private void applySkill(Player pl) {

        // Begin sound effects
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5F, 0.7F);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5F, 0.7F);

        // Send player info message
        pl.sendMessage(ChatColor.GREEN + "You feel the wind at your back!");

        // Add player effects
        pl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, BUFF_DURATION * 20, SPEED_AMPLIFIER));
        pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation(),
                25, 0, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.WHITE, 20));

        // Begin system to remove effects
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
            pl.sendMessage(ChatColor.GRAY + "The strength of the wind leaves you.");
        }, BUFF_DURATION * 20);
    }
}
