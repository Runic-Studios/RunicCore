package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;

public class DivineShield extends Spell {

    private static final int DURATION = 2;
    private static final double PERCENT = .10;
    private static final double PERCENT_REDUCTION = .75;

    public DivineShield() {
        super ("Divine Shield",
                "Your healing<3 spells have a " + PERCENT + "% " +
                        "chance to grant a divine shield to your ally, " +
                        "granting them " + PERCENT_REDUCTION + "% damage " +
                        "reduction for " + DURATION + "s!",
                ChatColor.WHITE, ClassEnum.CLERIC, 0, 0);
        this.setIsPassive(true);
    }

//    @EventHandler
//    public void onSpellCast(SpellCastEvent e) {
//
//        if (!hasPassive(e.getCaster(), this.getName())) return;
//
//        Random rand = new Random();
//        int roll = rand.nextInt(100) + 1;
//        if (roll > (PERCENT * 100)) return;
//
//        if (RunicCore.getPartyManager().getPlayerParty(e.getCaster()) == null) return;
//        Set<Player> allies = RunicCore.getPartyManager().getPlayerParty(e.getCaster()).getMembersWithLeader();
//
//        for (Player ally : allies) {
//            if (verifyAlly(e.getCaster(), ally)) {
//                if (!e.getCaster().getWorld().equals(ally.getWorld())) continue;
//                if (e.getCaster().getLocation().distanceSquared(ally.getLocation()) > RADIUS*RADIUS) continue;
//                e.getCaster().getWorld().playSound(e.getCaster().getLocation(), Sound.ENTITY_WITCH_DRINK, 0.25f, 2f);
//                e.getCaster().playSound(e.getCaster().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.25f, 1);
//                ally.getWorld().playSound(ally.getLocation(), Sound.ENTITY_WITCH_DRINK, 0.25f, 2f);
//                ally.getWorld().spawnParticle(Particle.SPELL_WITCH, ally.getEyeLocation(), 3, 0.3F, 0.3F, 0.3F, 0);
//                HealUtil.healPlayer(e.getSpell().getManaCost(), ally, e.getCaster(), false, false, false);
//            }
//        }
//    }

    // todo on heal
}

