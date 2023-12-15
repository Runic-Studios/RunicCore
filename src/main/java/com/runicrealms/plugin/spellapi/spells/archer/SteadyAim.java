package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.RunicBowEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.spellapi.spelltypes.AttributeSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * New marksman ult passive
 *
 * @author BoBoBalloon
 */
public class SteadyAim extends Spell implements AttributeSpell, DurationSpell {
    private static final Stat STAT = Stat.DEXTERITY;
    private static final int MAX_STACKS = 4;
    private final Map<UUID, SpellPayload> stacks;
    private double damage;
    private double damagePerLevel;
    private double duration;

    public SteadyAim() {
        super("Steady Aim", CharacterClass.ARCHER);
        this.setIsPassive(true);
        this.setDescription("For every " + this.duration + "s you do not fire a basic attack, your next basic attack deals (" + this.damage + " +&f " + this.damagePerLevel + "x &e" + STAT.getPrefix() + "&7) more physical⚔ damage.\n" +
                "This caps out at " + MAX_STACKS + " stacks.\n" +
                "Additionally, hitting the last arrow from &aLeaping Shot&7 automatically gives you maximum stacks on this effect.");
        this.stacks = new HashMap<>();

        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), () -> {
            long now = System.currentTimeMillis();

            for (Map.Entry<UUID, SpellPayload> data : this.stacks.entrySet()) {
                SpellPayload payload = data.getValue();

                if (now - payload.getLastTimeShot() < this.duration * 1000) {
                    continue;
                }

                int stacks = payload.getStacks();

                payload.setStacks(stacks + 1);

                if (payload.getStacks() < MAX_STACKS || stacks >= MAX_STACKS) {
                    continue;
                }

                Player player = Bukkit.getPlayer(data.getKey());

                if (player == null) {
                    continue;
                }

                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.NEUTRAL, 1, 1);
                player.sendMessage(ColorUtil.format("&a&lSteady Aim is fully charged"));
            }
        }, 0, (long) (this.duration * 20));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onRunicBow(RunicBowEvent event) {
        if (!this.hasPassive(event.getPlayer().getUniqueId(), this.getName())) {
            return;
        }

        SpellPayload payload = this.stacks.get(event.getPlayer().getUniqueId());

        if (payload != null) {
            payload.setLastTimeShot();
        } else {
            this.stacks.put(event.getPlayer().getUniqueId(), new SpellPayload());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!event.isBasicAttack() || !this.hasPassive(event.getPlayer().getUniqueId(), this.getName())) {
            return;
        }

        SpellPayload payload = this.stacks.get(event.getPlayer().getUniqueId());

        if (payload == null || payload.getStacks() <= 0) {
            return;
        }

        int stat = RunicCore.getStatAPI().getStat(event.getPlayer().getUniqueId(), STAT.getIdentifier());
        int amount = (int) (this.damage + (stat * this.damagePerLevel));

        event.setAmount(event.getAmount() + (amount * payload.getStacks()));
        payload.setStacks(0);
    }

    @EventHandler
    private void onLeapingShotArrowHit(LeapingShot.ArrowHitEvent event) {
        if (!event.isLast() || !this.hasPassive(event.getCaster().getUniqueId(), this.getName())) {
            return;
        }

        SpellPayload payload = this.stacks.get(event.getCaster().getUniqueId());

        if (payload == null) {
            return;
        }

        payload.setStacks(MAX_STACKS);
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.stacks.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onCharacterLoaded(CharacterLoadedEvent event) {
        if (!this.hasPassive(event.getPlayer().getUniqueId(), this.getName())) {
            return;
        }

        this.stacks.put(event.getPlayer().getUniqueId(), new SpellPayload());
    }

    @Override
    public double getBaseValue() {
        return this.damage;
    }

    @Override
    public void setBaseValue(double baseValue) {
        this.damage = baseValue;
    }

    @Override
    public double getMultiplier() {
        return this.damagePerLevel;
    }

    @Override
    public void setMultiplier(double multiplier) {
        this.damagePerLevel = multiplier;
    }

    @Override
    public String getStatName() {
        return STAT.getIdentifier();
    }

    @Override
    public void setStatName(String statName) {

    }

    @Override
    public double getDuration() {
        return this.duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    private static class SpellPayload {
        private long lastTimeShot;
        private int stacks;

        public SpellPayload() {
            this.lastTimeShot = System.currentTimeMillis();
            this.stacks = 0;
        }

        public long getLastTimeShot() {
            return this.lastTimeShot;
        }

        public void setLastTimeShot() {
            this.lastTimeShot = System.currentTimeMillis();
        }

        public int getStacks() {
            return this.stacks;
        }

        public void setStacks(int stacks) {
            this.stacks = Math.max(Math.min(stacks, MAX_STACKS), 0);
        }
    }
}
