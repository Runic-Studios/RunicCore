package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

public class Conflagration extends Spell {
    private static final int DAMAGE_CAP = 500;
    private static final int DURATION = 4;
    private static final int PERIOD = 2;
    private static final double PERCENT = .02;

    public Conflagration() {
        super("Conflagration",
                "Your spells roar with magical fire! " +
                        "Enemies hit by your spells take " + (int) (PERCENT * 100) + "% of their " +
                        "max health as magic damage over " + DURATION + "s! " +
                        "Capped at " + DAMAGE_CAP + " total damage against monsters.",
                ChatColor.WHITE, CharacterClass.MAGE, 0, 0);
        this.setIsPassive(true);
    }

    private void conflagration(Player player, LivingEntity victim) {
        boolean isCapped = !(victim instanceof Player);
        new BukkitRunnable() {
            int count = 1;
            int totalDamage = 0;

            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                } else {
                    count += PERIOD;
                    player.getWorld().spawnParticle(Particle.REDSTONE, victim.getEyeLocation(), 15, 0.25f, 0, 0.25f, new Particle.DustOptions(Color.ORANGE, 1));
                    int damage = (int) ((PERCENT * victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) / PERIOD);
                    if (isCapped && (damage + totalDamage) > DAMAGE_CAP)
                        damage = DAMAGE_CAP - totalDamage;
                    totalDamage += damage;
                    DamageUtil.damageEntitySpell(damage, victim, player);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, PERIOD * 20L);
    }

    @EventHandler
    public void onMagicDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (event.getSpell() == null) return;
        conflagration(event.getPlayer(), event.getVictim());
    }
}

