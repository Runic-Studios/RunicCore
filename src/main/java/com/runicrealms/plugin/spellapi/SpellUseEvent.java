package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.ActionBarUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class SpellUseEvent implements Listener {

    @EventHandler
    public void onSpell(PlayerInteractEvent e) {

        Player pl = e.getPlayer();
        int slot = pl.getInventory().getHeldItemSlot();
        if (e.getHand() != EquipmentSlot.HAND) return;

        if (pl.getGameMode() == GameMode.CREATIVE) return;
        Material mat = pl.getInventory().getItemInMainHand().getType();
        if (pl.getInventory().getItemInMainHand().getType() == Material.AIR) return;

        // ignore spell scrolls
        if (mat != Material.POPPED_CHORUS_FRUIT
                && mat != Material.BOW
                && mat != Material.WOODEN_SHOVEL
                && mat != Material.WOODEN_HOE
                && mat != Material.WOODEN_SWORD
                && mat != Material.WOODEN_AXE) return;

        ItemStack heldItem = pl.getInventory().getItemInMainHand();

        // identify artifact or rune
        SpellItemType spellItemType = null;
        for (SpellItemType type : SpellItemType.values()) {
            if (type.getSlot() == slot) {
                spellItemType = type;
                break;
            }
        }

        // determine which spell slot to search, based on the type of item (bows are flipped left, right)
        String spellSlot = determineSpellSlot(e, heldItem);
        if (spellSlot.equals("")) return;

        // determine spell to cast
        Spell spellToCast = null;
        for (Spell spell : RunicCore.getSpellManager().getSpells()) {
            if (spell.isFound(pl.getInventory().getItemInMainHand(), spellSlot)) {
                spellToCast = spell;
                break;
            }
        }

        // execute the spell
        if (spellToCast == null) return;

        // verify player level
        if (RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getClassLevel() < AttributeUtil.getCustomDouble(heldItem, "required.level")) {
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5F, 1);
            ActionBarUtil.sendTimedMessage(pl, "&cYour level is too low to cast this!", 3);
            return;
        }

        // call spell event
        SpellCastEvent event = new SpellCastEvent(pl, spellToCast);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled() && event.willExecute()) {
            event.getSpellCasted().execute(pl, spellItemType);
        }
    }

    private String determineSpellSlot(PlayerInteractEvent e, ItemStack item) {
        String spellSlot = "";
        if (item.getType() == Material.BOW) {
            if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                spellSlot = !e.getPlayer().isSneaking() ? "secondarySpell" : "sneakSpell";
            }
        } else {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                spellSlot = !e.getPlayer().isSneaking() ? "secondarySpell" : "sneakSpell";
            }
        }
        return spellSlot;
    }
}

