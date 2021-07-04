package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class ArcaneShot extends Spell {

    private static final int DAMAGE = 15;
    private static final int DURATION = 3;
    private Arrow arcaneArrow;

    public ArcaneShot() {
        super("Arcane Shot",
                "You fire a magical arrow which " +
                        "deals " + DAMAGE + " spellʔ damage to its " +
                        "target and silences it for " + DURATION + " " +
                        "seconds, preventing it from " +
                        "healing or dealing damage!",
                ChatColor.WHITE, ClassEnum.ARCHER, 12, 25);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1f);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 2f, 2f);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 0.5f, 2f);
        arcaneArrow = pl.launchProjectile(Arrow.class);
        Vector vec = pl.getEyeLocation().getDirection().normalize().multiply(2);
        arcaneArrow.setVelocity(vec);
        arcaneArrow.setShooter(pl);
        EntityTrail.entityTrail(arcaneArrow, Color.FUCHSIA);
    }

    @EventHandler
    public void onPoisArrowHit(EntityDamageByEntityEvent e) {

        // only listen for arrows
        if (!e.getDamager().equals(this.arcaneArrow)) return;
        e.setCancelled(true);
        Player pl = (Player) ((Arrow) e.getDamager()).getShooter();
        LivingEntity livingEntity = (LivingEntity) e.getEntity();
        if (!verifyEnemy(pl, livingEntity)) return;

        // spell effect
        addStatusEffect(livingEntity, EffectEnum.SILENCE, DURATION);
        DamageUtil.damageEntitySpell(DAMAGE, livingEntity, pl, this);

        livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.BLOCK_SLIME_BLOCK_BREAK, 0.5f, 0.5f);
        livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 0.5f, 2.0f);
    }
}
