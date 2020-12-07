package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.RunicCore;
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
        if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        WeaponEnum heldItemType = WeaponEnum.matchType(e.getPlayer().getInventory().getItemInMainHand());
        if (heldItemType == WeaponEnum.NONE) return;
        if (!DamageListener.matchClass(e.getPlayer(), false)) return;
        Player pl = e.getPlayer();
        if (!casters.contains(pl.getUniqueId())) {
            casters.add(pl.getUniqueId());
            pl.sendTitle
                    (
                            "", ChatColor.LIGHT_PURPLE + "[R] - " + ChatColor.DARK_GRAY + "[1] [2] [3] [4]", 0, 100, 0
                    );
        }
        Bukkit.getScheduler().scheduleAsyncDelayedTask(RunicCore.getInstance(), () -> casters.remove(pl.getUniqueId()), SPELL_TIMEOUT);
    }

    @EventHandler
    public void onSpellCast(PlayerItemHeldEvent e) {
        if (!casters.contains(e.getPlayer().getUniqueId())) return;
        switch (e.getNewSlot()) {
            case 0:

            case 1:
            case 2:
            case 3:
                e.setCancelled(true);
                casters.remove(e.getPlayer().getUniqueId());
                e.getPlayer().sendTitle
                        (
                                "",
                                ChatColor.LIGHT_PURPLE + "[R] - "
                                        + determineSelectedSlot(e.getNewSlot()), 0, 15, 0
                        );
                castSelectedSpell(e.getPlayer(), e.getNewSlot());
                break;
        }
    }

    /**
     * Determines which UI button to lightup.
     * @param slot from the held item event
     * @return A String corresponding to the new UI to display
     */
    private String determineSelectedSlot(int slot) {
        String selectedSpell = "";
        switch (slot) {
            case 0:
                selectedSpell = ChatColor.LIGHT_PURPLE + "[1] " + ChatColor.DARK_GRAY + "[2] [3] [4]";
                break;
            case 1:
                selectedSpell = ChatColor.DARK_GRAY + "[1] " + ChatColor.LIGHT_PURPLE + "[2] " + ChatColor.DARK_GRAY + "[3] [4]";
                break;
            case 2:
                selectedSpell = ChatColor.DARK_GRAY + "[1] [2] " + ChatColor.LIGHT_PURPLE + "[3] " + ChatColor.DARK_GRAY + "[4]";
                break;
            case 3:
                selectedSpell = ChatColor.DARK_GRAY + "[1] [2] [3] " + ChatColor.LIGHT_PURPLE + "[4]";
                break;
        }
            return selectedSpell;
    }

    /**
     * Determines which spell to case based on the selected slot.
     * @param pl caster of spell
     * @param slot slot of held item event
     */
    private void castSelectedSpell(Player pl, int slot) {
        Spell spellToCast = null;
        switch (slot) {
            case 0:
                spellToCast = RunicCore.getSpellManager().getSpellByName("Sprint");
                break;
            case 1:
                spellToCast = RunicCore.getSpellManager().getSpellByName("Smoke Bomb");
                break;
            case 2:
                spellToCast = RunicCore.getSpellManager().getSpellByName("Cloak");
                break;
            case 3:
                spellToCast = RunicCore.getSpellManager().getSpellByName("Rupture");
                break;
        }
        if (spellToCast == null) return;
        SpellCastEvent event = new SpellCastEvent(pl, spellToCast);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled() && event.willExecute())
            event.getSpellCasted().execute(pl, SpellItemType.ARTIFACT);
    }
}

