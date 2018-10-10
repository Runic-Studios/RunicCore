package us.fortherealm.plugin.util;

import org.bukkit.configuration.file.YamlConfiguration;
import us.fortherealm.plugin.Main;

import java.io.File;
import java.io.IOException;

public class UniqueIdAssigner {

    /*

    This util can be used to assign objects with unique IDs when they can't otherwise be individualized.
    The unique IDs are non-volatile, however the id must be stored on the object. This util is ONLY
    responsible for assigning unique IDs. An example of this util's usage can be found in the CasterItemStack
    class. 99.9999% of this utils usage will come from classes extending ItemStack.

    Note: Deleting the yaml file this util is writing to will result in a catastrophic loss of every unique id.

     */

    private static File uniqueIdFile;
    private static YamlConfiguration yamlConfiguration;
    private static Object synchronizer = new Object();

    static {
        UniqueIdAssigner.uniqueIdFile =
                new File(Main.getInstance().getDataFolder() + "/resources/data/UniqueIds.yml");
        if(!(uniqueIdFile.exists()))
            uniqueIdFile.mkdir();
        UniqueIdAssigner.yamlConfiguration = YamlConfiguration.loadConfiguration(uniqueIdFile);
    }

    private long currentStoredId;
    private long nextId;
    private String uniqueIdPath;

    public UniqueIdAssigner(String uniqueIdPath) {
        this.uniqueIdPath = uniqueIdPath;

        initialize();
    }

    private void initialize() {
        synchronized (synchronizer) {
            if(!(yamlConfiguration.isSet(uniqueIdPath)))
                yamlConfiguration.set(uniqueIdPath, 0);
        }
        nextId = yamlConfiguration.getLong(uniqueIdPath) + 100;
        currentStoredId = nextId;
    }

    public long getUniqueId() {
        synchronized (synchronizer) {
            nextId++;
            if((nextId - currentStoredId) >= 100) {
                currentStoredId = nextId;
                yamlConfiguration.set(uniqueIdPath, currentStoredId);
                try {
                    yamlConfiguration.save(uniqueIdFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return nextId;
        }
    }
}
