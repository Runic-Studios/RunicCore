package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class Riftwalk extends Spell implements MagicDamageSpell {
    private static final int DAMAGE = 20;
    private static final int COOLDOWN_REDUCTION = 3; // seconds
    private static final int RADIUS = 5;
    private static final double DAMAGE_PER_LEVEL = 3.0;

    public Riftwalk() {
        super("Riftwalk",
                "Upon exiting your &aBlink &7spell, " +
                        "you blast all enemies within " + RADIUS + " blocks " +
                        "with arcane magic, dealing (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage! If Riftwalk successfully hits " +
                        "a target, reduce the cooldown of &aBlink &7by " +
                        COOLDOWN_REDUCTION + "s!",
                ChatColor.WHITE, CharacterClass.MAGE, 0, 0);
        this.setIsPassive(true);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlinkCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (!(event.getSpell() instanceof Blink)) return;
        Player caster = event.getCaster();

        // Delay CDR, particle by one tick
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                () -> {
                    boolean foundEnemy = false;
                    caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.2f);
                    for (Entity entity : event.getCaster().getWorld().getNearbyEntities
                            (event.getCaster().getLocation(), RADIUS, RADIUS, RADIUS,
                                    target -> isValidEnemy(event.getCaster(), target))) {
                        DamageUtil.damageEntitySpell(DAMAGE, (LivingEntity) entity, event.getCaster(), this);
                        foundEnemy = true;
                    }
                    if (foundEnemy) {
                        RunicCore.getSpellAPI().reduceCooldown(event.getCaster(), "Blink", COOLDOWN_REDUCTION);
                    }

                }
                , 1L);
    }

}

