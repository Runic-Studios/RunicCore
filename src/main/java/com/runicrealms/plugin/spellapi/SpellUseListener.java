package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.enums.WeaponEnum;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.listeners.DamageListener;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashSet;
import java.util.UUID;

public class SpellUseListener implements Listener {

    private final HashSet<UUID> casters;
    private static final int SPELL_TIMEOUT = 5;

    public SpellUseListener() {
        casters = new HashSet<>();
    }

    @EventHandler
    public void onWeaponInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        WeaponEnum heldItemType = WeaponEnum.matchType(e.getPlayer().getInventory().getItemInMainHand());
        if (heldItemType == WeaponEnum.NONE) return;
        if (!DamageListener.matchClass(e.getPlayer(), false)) return;
        Player pl = e.getPlayer();
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (!casters.contains(pl.getUniqueId())) {
                casters.add(pl.getUniqueId());
                pl.sendTitle
                        (
                                "", ChatColor.LIGHT_PURPLE + "[R] - " + ChatColor.DARK_GRAY + "[1] [L] [R] [F]", 0, SPELL_TIMEOUT * 20, 0
                        );
                Bukkit.getScheduler().scheduleAsyncDelayedTask(RunicCore.getInstance(), () -> casters.remove(pl.getUniqueId()), SPELL_TIMEOUT * 20);
            } else {
                castSpell(pl, 3);
            }
        } else {
            if (!casters.contains(pl.getUniqueId())) return;
            castSpell(pl, 2);
        }
    }

    @EventHandler
    public void onSpellCast(PlayerItemHeldEvent e) {
        if (!casters.contains(e.getPlayer().getUniqueId())) return;
        if (e.getNewSlot() != 0) return;
        e.setCancelled(true);
        castSpell(e.getPlayer(), 1);
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent e) {
        if (!casters.contains(e.getPlayer().getUniqueId())) return;
        castSpell(e.getPlayer(), 4);
    }

    /**
     * Removes the player from casters set and executes spell logic
     * @param pl who casted the spell
     * @param number which spell to execute (1, 2, 3, 4)
     */
    private void castSpell(Player pl, int number) {
        casters.remove(pl.getUniqueId());
        pl.sendTitle
                (
                        "",
                        ChatColor.LIGHT_PURPLE + "[R] - "
                                + determineSelectedSlot(number), 0, 15, 0
                );
        castSelectedSpell(pl, number);
    }

    /**
     * Determines which UI button to lightup.
     * @param number which of the four spells to execute
     * @return A String corresponding to the new UI to display
     */
    private String determineSelectedSlot(int number) {
        String selectedSpell = "";
        switch (number) {
            case 1:
                selectedSpell = ChatColor.LIGHT_PURPLE + "[1] " + ChatColor.DARK_GRAY + "[L] [R] [F]";
                break;
            case 2:
                selectedSpell = ChatColor.DARK_GRAY + "[1] " + ChatColor.LIGHT_PURPLE + "[L] " + ChatColor.DARK_GRAY + "[R] [F]";
                break;
            case 3:
                selectedSpell = ChatColor.DARK_GRAY + "[1] [L] " + ChatColor.LIGHT_PURPLE + "[R] " + ChatColor.DARK_GRAY + "[F]";
                break;
            case 4:
                selectedSpell = ChatColor.DARK_GRAY + "[1] [L] [R] " + ChatColor.LIGHT_PURPLE + "[F]";
                break;
        }
            return selectedSpell;
    }

    /**
     * Determines which spell to case based on the selected number.
     * @param pl caster of spell
     * @param number which spell number to cast (1, 2, 3, 4)
     */
    private void castSelectedSpell(Player pl, int number) {
        Spell spellToCast = RunicCoreAPI.getPlayerSpell(pl, number);
        if (spellToCast == null) return;
        SpellCastEvent event = new SpellCastEvent(pl, spellToCast);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled() && event.willExecute())
            event.getSpellCasted().execute(pl, SpellItemType.ARTIFACT);
    }
}

