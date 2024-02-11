package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.api.event.BasicAttackEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.perk.DynamicItemPerkPercentStatPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import com.runicrealms.plugin.spellapi.spells.rogue.Dash;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A class that models the agility item perk
 *
 * @author BoBoBalloon
 */
public class BlackfrostRogueArmorPerk extends ItemPerkHandler {
    private final Map<UUID, Long> lastDash;
    private final double attackSpeedPercent;
    private final long duration;

    public BlackfrostRogueArmorPerk() {
        super("blackfrost-rogue-armor");

        this.lastDash = new HashMap<>();

        this.attackSpeedPercent = ((Number) this.config.get("attack-speed-percent")).doubleValue();
        this.duration = ((Number) this.config.get("duration")).longValue() * 1000; //convert seconds to milliseconds

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("blackfrost-rogue-armor-attack-speed-percent", this, () -> this.attackSpeedPercent));
    }

    @EventHandler(ignoreCancelled = true)
    private void onBasicAttack(BasicAttackEvent event) {
        if (!this.isActive(event.getPlayer())) {
            return;
        }

        Long dash = this.lastDash.get(event.getPlayer().getUniqueId());
        if (dash == null || System.currentTimeMillis() - dash > this.duration) {
            this.lastDash.remove(event.getPlayer().getUniqueId());
            return;
        }

        double ticksToReduce = event.getOriginalCooldownTicks() * this.attackSpeedPercent * this.getCurrentStacks(event.getPlayer());
        event.setCooldownTicks(Math.max(event.getUnroundedCooldownTicks() - ticksToReduce, BasicAttackEvent.MINIMUM_COOLDOWN_TICKS));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    //MONITOR is fine since I am only getting values from the event, not setting any
    private void onSpellCast(SpellCastEvent event) {
        if (!(event.getSpell() instanceof Dash)) {
            return;
        }

        this.lastDash.put(event.getCaster().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.lastDash.remove(event.getPlayer().getUniqueId());
    }
}