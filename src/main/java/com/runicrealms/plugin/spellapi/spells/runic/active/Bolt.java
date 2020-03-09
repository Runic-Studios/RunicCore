package com.runicrealms.plugin.spellapi.spells.runic.active;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Objects;

@SuppressWarnings("FieldCanBeLocal")
public class Bolt extends Spell {

    private static final int DAMAGE_AMT = 15;
    private static final int MAX_DIST = 8;
    private static final int RADIUS = 5;

    public Bolt() {
        super("Bolt",
                "You summon a bolt of lightning" +
                        "\nup to " + MAX_DIST + " blocks away, dealing" +
                        "\n" + DAMAGE_AMT + " spellʔ damage to enemies" +
                        "\nwithin " + RADIUS + " blocks!",
                ChatColor.WHITE, ClassEnum.RUNIC, 6, 10);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        Location boltLoc = pl.getTargetBlock(null, MAX_DIST).getLocation();
        pl.getWorld().spigot().strikeLightningEffect(boltLoc, true);
        pl.getWorld().playSound(boltLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 1.0f);

        for (Entity en : Objects.requireNonNull(boltLoc.getWorld()).getNearbyEntities(boltLoc, RADIUS, RADIUS, RADIUS)) {
            if (!verifyEnemy(pl, en)) continue;
            LivingEntity le = (LivingEntity) en;
            le.getWorld().spawnParticle(Particle.CRIT_MAGIC, le.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
            DamageUtil.damageEntitySpell(DAMAGE_AMT, le, pl, false);
        }
    }
}

