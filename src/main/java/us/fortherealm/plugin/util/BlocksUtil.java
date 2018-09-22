package us.fortherealm.plugin.util;

import java.util.*;

import org.bukkit.Material;

public final class BlocksUtil
{

    public static final Set<Material> interactableBlocks;

    static
    {
        interactableBlocks = EnumSet.noneOf(Material.class);

        interactableBlocks.add(Material.CHEST);
        interactableBlocks.add(Material.IRON_DOOR_BLOCK);
        interactableBlocks.add(Material.SIGN);
        interactableBlocks.add(Material.WALL_SIGN);
        interactableBlocks.add(Material.SIGN_POST);
        interactableBlocks.add(Material.WORKBENCH);
        interactableBlocks.add(Material.STONE_BUTTON);
        interactableBlocks.add(Material.WOOD_BUTTON);
        interactableBlocks.add(Material.LEVER);
        interactableBlocks.add(Material.WOODEN_DOOR);
        interactableBlocks.add(Material.TRAP_DOOR);
        interactableBlocks.add(Material.TRAPPED_CHEST);
        interactableBlocks.add(Material.DIODE);
        interactableBlocks.add(Material.DIODE_BLOCK_ON);
        interactableBlocks.add(Material.DIODE_BLOCK_OFF);
        interactableBlocks.add(Material.DISPENSER);
        interactableBlocks.add(Material.HOPPER);
        interactableBlocks.add(Material.DROPPER);
        interactableBlocks.add(Material.REDSTONE_COMPARATOR);
        interactableBlocks.add(Material.REDSTONE_COMPARATOR_ON);
        interactableBlocks.add(Material.REDSTONE_COMPARATOR_OFF);
        interactableBlocks.add(Material.FURNACE);
        interactableBlocks.add(Material.BURNING_FURNACE);
        interactableBlocks.add(Material.CAULDRON);
        interactableBlocks.add(Material.JUKEBOX);
        interactableBlocks.add(Material.NOTE_BLOCK);
        interactableBlocks.add(Material.STORAGE_MINECART);
        interactableBlocks.add(Material.ENDER_CHEST);
        interactableBlocks.add(Material.FENCE_GATE);
        interactableBlocks.add(Material.ENCHANTMENT_TABLE);
        interactableBlocks.add(Material.BREWING_STAND);
        interactableBlocks.add(Material.ITEM_FRAME);
        interactableBlocks.add(Material.BOAT);
        interactableBlocks.add(Material.MINECART);
        interactableBlocks.add(Material.FLOWER_POT);
        interactableBlocks.add(Material.BEACON);
        interactableBlocks.add(Material.BED_BLOCK);
        interactableBlocks.add(Material.ANVIL);
        interactableBlocks.add(Material.COMMAND);
    }
}
