package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Sprint extends Spell implements MagicDamageSpell {

    private static final int DAMAGE_AMOUNT = 5;
    private static final double DAMAGE_PER_LEVEL = 1.75;
    private static final int DURATION = 5;
    private static final int SPEED_AMPLIFIER = 2;
    private final HashSet<UUID> sprinters = new HashSet<>();

    public Sprint() {
        super("Sprint",
                "For " + DURATION + " seconds, you gain a " +
                        "massive boost of speed! Your next melee attack against " +
                        "an enemy deals (" + DAMAGE_AMOUNT + " + &f" + DAMAGE_PER_LEVEL +
                        "x&7 lvl) spellʔ damage!",
                ChatColor.WHITE, ClassEnum.ROGUE, 10, 10);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DURATION * 20, SPEED_AMPLIFIER));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5F, 1.0F);
        new HorizCircleFrame(1).playParticle(Particle.TOTEM, player.getLocation(), Color.FUCHSIA);
        new HorizCircleFrame(1).playParticle(Particle.TOTEM, player.getEyeLocation(), Color.FUCHSIA);
        sprinters.add(player.getUniqueId());
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        if (!e.isAutoAttack()) return;
        if (!sprinters.contains(e.getPlayer().getUniqueId())) return;
        Player player = e.getPlayer();
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRASS_BREAK, 0.5f, 0.5f);
        player.getWorld().spawnParticle(Particle.CRIT_MAGIC, e.getVictim().getEyeLocation(), 15, 0.5f, 0.5f, 0.5f, 0);
        DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, e.getVictim(), player, this);
        sprinters.remove(player.getUniqueId());
    }
}

