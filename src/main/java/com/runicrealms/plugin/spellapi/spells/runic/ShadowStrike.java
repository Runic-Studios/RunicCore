package com.runicrealms.plugin.spellapi.spells.runic;

import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class ShadowStrike extends Spell {

    private static final int DAMAGE_AMT = 25;
    private static final int DURATION = 3;
    private static List<UUID> strikers;

    public ShadowStrike() {
        super ("Shadow Strike",
                "You empower your artifact, causing your" +
                        "\nnext melee attack to deal " + DAMAGE_AMT + " additional" +
                        "\nspellʔ damage to your enemy and blind" +
                        "\nthem for " + DURATION + " second(s)! (Does not apply" +
                        "\nto ranged attacks)",
                ChatColor.WHITE, 10, 10);
        strikers = new ArrayList<>();
    }

    // spell execute code
    @Override
    public void executeSpell(Player player, SpellItemType type) {

        UUID uuid = player.getUniqueId();

        // check to ensure no stacking of spell
        strikers.remove(uuid);
        // ------------------------------------

        strikers.add(uuid);
        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 0.5f, 0.5f);
    }

    @EventHandler
    public void onStrike(WeaponDamageEvent e) {

        // ignore ranged attacks
        if (e.getIsRanged()) {
            return;
        }

        Player pl = e.getPlayer();
        Entity en = e.getEntity();

        if (!strikers.contains(pl.getUniqueId())) return;
        if (!(en instanceof LivingEntity)) return;

        strikers.remove(pl.getUniqueId());

        LivingEntity victim = (LivingEntity) en;

        // particles, sounds
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 0.25f, 0.1f);
        victim.getWorld().spawnParticle(Particle.REDSTONE, victim.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                new Particle.DustOptions(Color.PURPLE, 3));

        // damage victim
        DamageUtil.damageEntitySpell(DAMAGE_AMT, victim, pl);

        // blind
        if (victim instanceof Player) {
            victim.addPotionEffect
                    (new PotionEffect(PotionEffectType.BLINDNESS, DURATION * 20, 0));
        }
    }
}

