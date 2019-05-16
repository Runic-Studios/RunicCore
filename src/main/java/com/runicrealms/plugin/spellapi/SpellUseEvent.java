package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class SpellUseEvent implements Listener {

    @EventHandler
    public void onSkill(PlayerInteractEvent e) {

        Player pl = e.getPlayer();
        int slot = pl.getInventory().getHeldItemSlot();

        // only listen for artifacts/runes
        if (slot != 0 && slot != 1) return;

        if (pl.getGameMode() == GameMode.CREATIVE) return;
        if (pl.getInventory().getItemInMainHand().getType() == Material.AIR) return;

        ItemStack heldItem = pl.getInventory().getItemInMainHand();

        // identify artifact or rune
        SpellItemType skillItemType = SpellItemType.NONE;
        for (SpellItemType type : SpellItemType.values()) {
            if (type.getSlot() == slot) {
                skillItemType = type;
                break;
            }
        }

        // determine which spell slot to search, based on the type of item (bows are flipped left, right)
        String spellSlot = determineSpellSlot(e, pl, heldItem, slot);
        if (spellSlot.equals("")) return;

        // determine skill to cast
        Spell skillCasted = null;
        for (Spell skill : RunicCore.getSkillManager().getSkills()) {
            if (skill.isFound(pl.getInventory().getItemInMainHand(), spellSlot)) {
                skillCasted = skill;
                break;
            }
        }

        // execute the skill
        if (skillCasted != null) {
            if (skillItemType == SpellItemType.NONE) {
                pl.sendMessage(ChatColor.RED + "ERROR: Something went wrong.");
                pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5F, 1);
                return;
            }
            skillCasted.execute(pl, skillItemType);
        }
    }

    private String determineSpellSlot(PlayerInteractEvent e, Player pl, ItemStack item, int itemSlot) {
        String spellSlot = "";
        // if its the rune, its always just left, right
        if (itemSlot == 1) {
            if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                spellSlot = "primarySpell";
            } else if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                spellSlot = "secondarySpell";
            }
        // now if we've got an artifact, things are gonna be a bit more complicated cuz of sneaking and bows.
        } else {
            if (item.getType() == Material.BOW) {
                if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    spellSlot = "primarySpell";
                } else if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (pl.isSneaking()) {
                        spellSlot = "secondarySpell";
                    }
                }
            } else {
                if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    if (pl.isSneaking()) {
                        spellSlot = "primarySpell";
                    }
                } else if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    spellSlot = "secondarySpell";
                }
            }
        }
        return spellSlot;
    }
}

