package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class ManaShield extends Spell {

    private static final int DURATION = 5;
    private final List<UUID> shielded = new ArrayList<>();

    public ManaShield() {
        super("Mana Shield",
                "For " + DURATION + " seconds, all damage" +
                        "\nyou receive is taken from" +
                        "\nyour mana instead!",
                ChatColor.WHITE, ClassEnum.MAGE, 15, 0);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // apply effects
        shielded.add(pl.getUniqueId());
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 2.0f);
        Cone.coneEffect(pl, Particle.REDSTONE, DURATION, 0, 20L, Color.TEAL);
        Cone.coneEffect(pl, Particle.CRIT_MAGIC, DURATION, 0, 20L, Color.WHITE);

        new BukkitRunnable() {
            @Override
            public void run() {
                shielded.remove(pl.getUniqueId());
            }
        }.runTaskLater(RunicCore.getInstance(), DURATION*20L);
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent e) {
        if (!shielded.contains(e.getVictim().getUniqueId()))
            return;
        if (!(e.getVictim() instanceof Player))
            return;
        Player victim = (Player) e.getVictim();
        e.setAmount(shieldWithMana(victim, e.getAmount()));
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {
        if (!shielded.contains(e.getEntity().getUniqueId()))
            return;
        if (!(e.getEntity() instanceof Player))
            return;
        Player victim = (Player) e.getEntity();
        e.setAmount(shieldWithMana(victim, e.getAmount()));
    }

    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        if (!shielded.contains(e.getEntity().getUniqueId()))
            return;
        if (!(e.getEntity() instanceof Player))
            return;
        Player victim = (Player) e.getEntity();
        e.setAmount(shieldWithMana(victim, e.getAmount()));
    }

    private int shieldWithMana(Player victim, int eventAmount) {
        int finalAmount = eventAmount;
        if (RunicCore.getRegenManager().getCurrentManaList().get(victim.getUniqueId()) <= finalAmount)
            finalAmount -= RunicCore.getRegenManager().getCurrentManaList().get(victim.getUniqueId());
        else
            finalAmount = 0;
        RunicCore.getRegenManager().subtractMana(victim, eventAmount);
        return finalAmount;
    }
}

