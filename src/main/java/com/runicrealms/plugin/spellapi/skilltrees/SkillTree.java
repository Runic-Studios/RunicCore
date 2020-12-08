package com.runicrealms.plugin.spellapi.skilltrees;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.spellapi.skilltrees.util.MageTreeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SkillTree {

    private Player player;
    private final List<Perk> perks;
    private static final String PATH_LOCATION = "skillTree";

    public SkillTree() {
        perks = new ArrayList<>();
    }

    public SkillTree(Player player) {
        this.player = player;
        perks = MageTreeUtil.pyromancerPerkList(); // todo: determine 3 sub-trees to show based on class, then from there
        // get sub-class, load default
        // get allocatin data from db, populate
    }

    public Player getPlayer() {
        return player;
    }

    public List<Perk> getPerks() {
        return perks;
    }

    /**
     *
     * @param async
     */
    public void save(boolean async) {
        if (async)
            Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> saveTreeData());
        else
            saveTreeData();
    }

    /**
     *
     */
    private void saveTreeData() {
        int slot = RunicCoreAPI.getPlayerCache(player).getCharacterSlot();
        PlayerMongoData mongoData = new PlayerMongoData(player.getUniqueId().toString());
        MongoDataSection treeDataSection = mongoData.getCharacter(slot);
        for (Perk perk : perks) {
            if (perk.getCurrentlyAllocatedPoints() == 0) continue;
            treeDataSection.set(PATH_LOCATION + "." + perk.getPerkID(), perk.getCurrentlyAllocatedPoints());
        }
        treeDataSection.save();
    }
}
