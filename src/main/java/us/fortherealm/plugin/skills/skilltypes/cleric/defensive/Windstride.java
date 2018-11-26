package us.fortherealm.plugin.skills.skilltypes.cleric.defensive;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.events.gameplay.skills.SkillImpactEvent;
import us.fortherealm.plugin.skills.skilltypes.TargetingSkill;

@SuppressWarnings("FieldCanBeLocal")
public class Windstride extends TargetingSkill<Player> {

    private static final int BUFF_DURATION = 10;
    private static final int SPEED_AMPLIFIER = 1;
    private static final int RADIUS = 10;

    // constructor
    public Windstride() {
        super("Windstride",
                "You increase the movement speed of yourself and all party members" +
                        "within " + RADIUS + "blocks by an amount", 1, true);
    }

    @Override
    public void executeSkill() {

        // apply the skill effects
        applySkill(getPlayer());

        // if the user has a party, each party member gets the effects as well.
        if (Main.getPartyManager().getPlayerParty(getPlayer()) != null) {
            for (Entity e : getPlayer().getNearbyEntities(RADIUS, RADIUS, RADIUS)) {

                // skip our player, skip non-player entities
                if (e == getPlayer()) { continue; }
                if (!(e instanceof Player)) { continue; }

                if (Main.getPartyManager().getPlayerParty(getPlayer()).hasMember(e.getUniqueId())) {
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
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            pl.sendMessage(ChatColor.GRAY + "The strength of the wind leaves you.");
        }, BUFF_DURATION * 20);
    }
}

