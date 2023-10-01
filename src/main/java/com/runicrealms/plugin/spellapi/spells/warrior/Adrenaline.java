package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.effect.BleedEffect;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HelixParticleFrame;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * New ultimate spell for berserker
 *
 * @author BoBoBalloon
 */
public class Adrenaline extends Spell implements DurationSpell {
    private final Map<UUID, Pair<Long, Integer>> stacks;
    private double buffDuration;
    private double stackDuration;
    private int maxStacks;
    private double percent;

    public Adrenaline() {
        super("Adrenaline", CharacterClass.WARRIOR);
        this.setDescription("For the next " + this.buffDuration + "s gain Speed II. " +
                "Each of your basic attacks on bleeding enemies grant you a stack of rage. " +
                "Each stack causes you to deal " + (this.percent * 100) + "% more physical⚔ damage. " +
                "This effect can stack up to " + this.maxStacks + " times and lasts " + this.stackDuration + "s. " +
                "If you reach maximum stacks within the " + this.buffDuration + "s, cleanse yourself of all negative effects and reset the duration of the speed bonus.");
        this.stacks = new HashMap<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 1.0f);
        new HelixParticleFrame(1.0F, 30, 10.0F).playParticle(player, Particle.REDSTONE, player.getLocation(), Color.RED);
        this.stacks.put(player.getUniqueId(), Pair.pair(System.currentTimeMillis(), 0));
        this.addStatusEffect(player, RunicStatusEffect.SPEED_II, this.buffDuration, true);
    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number maxStacks = (Number) spellData.getOrDefault("max-stacks", 5);
        this.maxStacks = maxStacks.intValue();
        Number percent = (Number) spellData.getOrDefault("percent", .03);
        this.percent = percent.doubleValue();
        Number stackDuration = (Number) spellData.getOrDefault("stack-duration", 1);
        this.stackDuration = stackDuration.doubleValue();
    }

    @Override
    public double getDuration() {
        return buffDuration;
    }

    @Override
    public void setDuration(double duration) {
        this.buffDuration = duration;
    }

    /**
     * A method used to check the duration of stacks of this ability and to remove any that have expired
     *
     * @param caster the caster
     * @return the amount of stacks the player currently has
     */
    public int getStacks(@NotNull UUID caster) {
        Pair<Long, Integer> data = this.stacks.get(caster);

        if (data == null) {
            return 0;
        }

        long now = System.currentTimeMillis();

        if (now < data.first + (this.buffDuration * 1000)) {
            return data.second;
        }

        if (data.second - 1 <= 0) {
            this.stacks.remove(caster);
            return 0;
        } else {
            this.stacks.put(caster, Pair.pair(data.first, data.second - 1));
            return data.second - 1;
        }
    }

    /**
     * A method used to add a stack to the caster
     *
     * @param caster the caster
     */
    public void addStack(@NotNull UUID caster) {
        Pair<Long, Integer> data = this.stacks.get(caster);

        if (data == null) {
            return;
        }

        long now = System.currentTimeMillis();

        if (now < data.first + (this.buffDuration * 1000)) {
            this.stacks.put(caster, Pair.pair(now, Math.min(data.second + 1, this.maxStacks)));
        } else {
            this.stacks.remove(caster);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        int stacks = this.getStacks(event.getPlayer().getUniqueId());

        if (this.stacks.containsKey(event.getPlayer().getUniqueId())
                && this.hasSpellEffect(event.getVictim().getUniqueId(), BleedEffect.IDENTIFIER)) {
            this.addStack(event.getPlayer().getUniqueId());
            //stacks + 1 since stacks was calculated before the new stack was added
            event.getPlayer().sendMessage(ColorUtil.format("&aYou have " + (stacks + 1) + " stacks of rage!"));
        }

        if (stacks <= 0) {
            return;
        }

        double multiplier = 1 + (this.percent * stacks);
        event.setAmount((int) (event.getAmount() * multiplier));

        if (stacks < this.maxStacks) {
            return;
        }

        event.getPlayer().sendMessage(ColorUtil.format("&aYou are &c&lenraged&r&a!"));
        this.stacks.remove(event.getPlayer().getUniqueId());
        this.addStatusEffect(event.getPlayer(), RunicStatusEffect.SPEED_II, this.buffDuration, false);
    }
}

