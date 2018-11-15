package us.fortherealm.plugin.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

//import java.util.Random;

public class PartyHelpGUI implements InventoryProvider {

    public static final SmartInventory INVENTORY = SmartInventory.builder()
            .id("partyHelp")
            .provider(new PartyHelpGUI())
            .size(1, 9)
            .title(ChatColor.BLUE + "Available Party Commands")
            .build();

    //private final Random random = new Random();

    @Override
    public void init(Player player, InventoryContents contents) {

        //contents.fillBorders(ClickableItem.empty(new ItemStack(Material.STONE)));

        contents.set(0, 1, ClickableItem.of(new ItemStack(Material.CARROT),
                e -> player.sendMessage(ChatColor.GOLD + "You clicked on a potato.")));

        contents.set(0, 7, ClickableItem.of(new ItemStack(Material.BARRIER),
                e -> player.closeInventory()));
    }

    // used for animated inventories, not necessary here
    @Override
    public void update(Player player, InventoryContents contents) {

//        int state = contents.property("state", 0);
//        contents.setProperty("state", state + 1);
//
//        if(state % 5 != 0)
//            return;
//
//        short durability = (short) random.nextInt(15);
//
//        ItemStack glass = new ItemStack(Material.STONE, 1, durability);
//        contents.fillBorders(ClickableItem.empty(glass));
    }
}
