package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HelixParticleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.UUID;

public class Enrage extends Spell {

    private static final int CHANNEL_DURATION = 2;
    private static final int BUFF_DURATION = 8;
    private static final int DAMAGE_AMT = 5;
    private static final HashSet<UUID> ragers = new HashSet<>();

    public Enrage() {
        super("Enrage",
                "For " + CHANNEL_DURATION + "s, you channel a deep " +
                        "rage, slowing your speed. After, " +
                        "you gain an immense boost of speed " +
                        "and your weapon⚔ attacks deal " + DAMAGE_AMT + " extra " +
                        "spellʔ damage for " + BUFF_DURATION + "s!",
                ChatColor.WHITE, ClassEnum.WARRIOR, 12, 25);
    }
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // apply preliminary particle effects
        pl.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, CHANNEL_DURATION * 20, 2));
        pl.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, CHANNEL_DURATION * 20, 2));
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 1.0f);
        new HelixParticleFrame(0.5F, 3, 2.5F).playParticle(Particle.REDSTONE, pl.getLocation(), Color.RED);
        //pl.sendMessage(ChatColor.GRAY + "You begin to feel a surge of power!");

        // after the player has channeled the spell
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> enrage(pl), CHANNEL_DURATION * 20L);
    }

    private void enrage(Player pl) {

        // particles, sounds
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
        pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation(),
                25, 0, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.RED, 20));

        // potion effect, dmg effect
        pl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, BUFF_DURATION * 20, 1));
        ragers.add(pl.getUniqueId());

        // remove damage buff
        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, () -> {
            // sound
            pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.5f, 1.0f);
            ragers.remove(pl.getUniqueId());
        }, BUFF_DURATION * 20L);
    }

    /*
     * Activate on-hit effects
     */
    @EventHandler
    public void onSuccessfulHit(WeaponDamageEvent e) {

        if (!e.isAutoAttack()) return;
        if (!ragers.contains(e.getPlayer().getUniqueId())) return;
        if(e.isCancelled()) return;

        Player pl = e.getPlayer();
        Entity en = e.getEntity();

        if (!(en instanceof LivingEntity)) return;

        LivingEntity le = (LivingEntity) en;

        le.getWorld().spawnParticle(Particle.CRIT_MAGIC, le.getEyeLocation(), 25, 0.25, 0.25, 0.25, 0);
        le.getWorld().playSound(le.getLocation(), Sound.ENTITY_WITCH_HURT, 0.5f, 0.8f);

        DamageUtil.damageEntitySpell(DAMAGE_AMT, le, pl, this);
    }

    public static int getBuffDuration() {
        return BUFF_DURATION;
    }

    public static HashSet<UUID> getRagers() {
        return ragers;
    }
}

