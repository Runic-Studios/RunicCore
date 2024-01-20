package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.perk.DynamicItemPerkPercentStatPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A class that models the ravenous item perk
 *
 * @author BoBoBalloon
 */
public class RavenousPerk extends ItemPerkHandler implements Listener {
    private final Map<UUID, Integer> stacks;
    private final double healthCutoff;
    private final double healthPercentRestored;

    public RavenousPerk() {
        super("ravenous");

        this.stacks = new HashMap<>();

        this.healthCutoff = ((Number) this.config.get("health-percent-threshold")).doubleValue();
        this.healthPercentRestored = ((Number) this.config.get("health-percent-per-stack")).doubleValue();

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("ravenous-health-restored", this, () -> this.healthPercentRestored));  //This is used in the configured lore
    }

    @Override
    public void onChange(Player player, int stacks) {
        if (stacks > 0) {
            this.stacks.put(player.getUniqueId(), stacks);
        } else {
            this.stacks.remove(player.getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!event.isBasicAttack()) {
            return;
        }

        Integer stacks = this.stacks.get(event.getPlayer().getUniqueId());
        if (stacks == null) {
            return;
        }

        double maxHealth = event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        if (event.getPlayer().getHealth() >= maxHealth * this.healthCutoff) {
            return;
        }

        double heal = this.healthPercentRestored * stacks * maxHealth;
        RunicCore.getSpellAPI().healPlayer(event.getPlayer(), event.getPlayer(), heal);
    }


    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.stacks.remove(event.getPlayer().getUniqueId());
    }
}
