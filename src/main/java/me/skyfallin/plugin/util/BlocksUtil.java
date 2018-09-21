package me.skyfallin.plugin.util;

import java.util.*;

import org.bukkit.Material;

public final class BlocksUtil
{

    public static final Set<Material> interactableBlocks;
    public static final Set<Material> transparentBlocks;
    public static final HashSet<Byte> transparentIds;

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

        transparentBlocks = EnumSet.noneOf(Material.class);
        transparentBlocks.add(Material.AIR);
        transparentBlocks.add(Material.CARPET);
        transparentBlocks.add(Material.CROPS);
        transparentBlocks.add(Material.DEAD_BUSH);
        transparentBlocks.add(Material.DETECTOR_RAIL);
        transparentBlocks.add(Material.DIODE_BLOCK_OFF);
        transparentBlocks.add(Material.DIODE_BLOCK_ON);
        transparentBlocks.add(Material.DIODE);
        transparentBlocks.add(Material.FENCE_GATE);
        transparentBlocks.add(Material.FLOWER_POT);
        transparentBlocks.add(Material.LADDER);
        transparentBlocks.add(Material.LEVER);
        transparentBlocks.add(Material.LONG_GRASS);
        transparentBlocks.add(Material.NETHER_WARTS);
        transparentBlocks.add(Material.PORTAL);
        transparentBlocks.add(Material.POWERED_RAIL);
        transparentBlocks.add(Material.RAILS);
        transparentBlocks.add(Material.RED_ROSE);
        transparentBlocks.add(Material.REDSTONE_COMPARATOR_OFF);
        transparentBlocks.add(Material.REDSTONE_COMPARATOR_ON);
        transparentBlocks.add(Material.REDSTONE_COMPARATOR);
        transparentBlocks.add(Material.REDSTONE_TORCH_OFF);
        transparentBlocks.add(Material.REDSTONE_TORCH_ON);
        transparentBlocks.add(Material.REDSTONE_WIRE);
        transparentBlocks.add(Material.SAPLING);
        transparentBlocks.add(Material.SIGN_POST);
        transparentBlocks.add(Material.SIGN);
        transparentBlocks.add(Material.SNOW);
        transparentBlocks.add(Material.STATIONARY_LAVA);
        transparentBlocks.add(Material.STATIONARY_WATER);
        transparentBlocks.add(Material.STONE_BUTTON);
        transparentBlocks.add(Material.STONE_PLATE);
        transparentBlocks.add(Material.SUGAR_CANE_BLOCK);
        transparentBlocks.add(Material.TORCH);
        transparentBlocks.add(Material.TRIPWIRE);
        transparentBlocks.add(Material.VINE);
        transparentBlocks.add(Material.WALL_SIGN);
        transparentBlocks.add(Material.WATER_LILY);
        transparentBlocks.add(Material.WATER);
        transparentBlocks.add(Material.WEB);
        transparentBlocks.add(Material.WOOD_BUTTON);
        transparentBlocks.add(Material.WOOD_PLATE);
        transparentBlocks.add(Material.YELLOW_FLOWER);

        transparentIds = new HashSet(42);
        transparentIds.add(Byte.valueOf((byte)Material.AIR.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.CARPET.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.CROPS.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.DEAD_BUSH.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.DETECTOR_RAIL.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.DIODE_BLOCK_OFF.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.DIODE_BLOCK_ON.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.DIODE.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.FENCE_GATE.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.FLOWER_POT.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.LADDER.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.LEVER.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.LONG_GRASS.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.NETHER_WARTS.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.PORTAL.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.POWERED_RAIL.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.RAILS.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.RED_ROSE.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.REDSTONE_COMPARATOR_OFF.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.REDSTONE_COMPARATOR_ON.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.REDSTONE_COMPARATOR.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.REDSTONE_TORCH_OFF.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.REDSTONE_TORCH_ON.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.REDSTONE_WIRE.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.SAPLING.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.SIGN_POST.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.SIGN.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.SNOW.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.STATIONARY_LAVA.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.STATIONARY_WATER.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.STONE_BUTTON.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.STONE_PLATE.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.SUGAR_CANE_BLOCK.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.TORCH.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.TRIPWIRE.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.VINE.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.WALL_SIGN.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.WATER_LILY.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.WATER.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.WEB.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.WOOD_BUTTON.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.WOOD_PLATE.getId()));
        transparentIds.add(Byte.valueOf((byte)Material.YELLOW_FLOWER.getId()));
    }
}
