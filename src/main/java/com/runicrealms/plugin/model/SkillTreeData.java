package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.SubClass;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.rdb.model.SessionDataRedis;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;
import com.runicrealms.plugin.spellapi.skilltrees.util.ArcherTreeUtil;
import com.runicrealms.plugin.spellapi.skilltrees.util.ClericTreeUtil;
import com.runicrealms.plugin.spellapi.skilltrees.util.MageTreeUtil;
import com.runicrealms.plugin.spellapi.skilltrees.util.RogueTreeUtil;
import com.runicrealms.plugin.spellapi.skilltrees.util.WarriorTreeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SkillTreeData implements SessionDataRedis {
    public static final int FIRST_POINT_LEVEL = 10;
    public static final String PATH_LOCATION = "skillTree";
    public static final String SPELLS_LOCATION = "spells";
    private SkillTreePosition position;
    private List<Perk> perks = new ArrayList<>();

    @SuppressWarnings("unused")
    public SkillTreeData() {
        // Default constructor for Spring
    }

    /**
     * Build default skill tree data (if there is no persistent data)
     *
     * @param uuid     of the player
     * @param position of the skill tree (1-3)
     */
    public SkillTreeData(UUID uuid, SkillTreePosition position, String playerClass) {
        this.position = position;
        CharacterClass characterClass = CharacterClass.getFromName(playerClass);
        SubClass subClass = SubClass.determineSubClass(characterClass, position);
        this.perks = getSkillTreeBySubClass(subClass); // load default perks
    }

    /**
     * Build the character's skill tree data from jedis
     *
     * @param uuid     of the player
     * @param position of the skill tree (1, 2, or 3)
     * @param jedis    the jedis resource
     */
    public SkillTreeData(UUID uuid, int slot, SkillTreePosition position, Jedis jedis) {
        this.position = position;
        SubClass subClass = SubClass.determineSubClass(uuid, slot, position, jedis);
        this.perks = getSkillTreeBySubClass(subClass); // load default perks
        String key = getJedisKey(uuid, slot, position);
        String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
        Map<String, String> perkDataMap = jedis.hgetAll(database + ":" + key); // get all the values for skill tree in position
        for (String perkId : perkDataMap.keySet()) { // update stored perk data for this object
            Perk perk = getPerk(Integer.parseInt(perkId));
            if (perk == null) continue;
            perk.setCurrentlyAllocatedPoints(Integer.parseInt(perkDataMap.get(perkId)));
        }
    }

    /**
     * Calculates the available skill points of the player.
     * First point is given at level 10, so first 9 levels are ignored.
     * For level 10 onward, we award 1 point per level. Then,
     * subtract 1 point for each purchased perk across all skill trees
     *
     * @param uuid of the player
     * @param slot of the character
     * @return available skill points to spend
     */
    public static int getAvailablePoints(UUID uuid, int slot) {
        Player player = Bukkit.getPlayer(uuid);
        int spentPoints = RunicCore.getSkillTreeAPI().getSpentPoints(uuid, slot);
        if (player != null)
            return Math.max(0, player.getLevel() - (FIRST_POINT_LEVEL - 1) - spentPoints);
        else
            return 0;
    }

    /**
     * Resets the skill trees for given player. ALL THREE skill trees will be wiped from memory / DB,
     * and spent points will be reset to 0 in DB and memory.
     *
     * @param player to reset tree for
     */
    public static void resetSkillTrees(Player player) {
        UUID uuid = player.getUniqueId();
        /*
        Wipe the memoized perk data
         */
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(uuid);
        SkillTreeData first = RunicCore.getSkillTreeAPI().loadSkillTreeData(uuid, slot, SkillTreePosition.FIRST);
        SkillTreeData second = RunicCore.getSkillTreeAPI().loadSkillTreeData(uuid, slot, SkillTreePosition.SECOND);
        SkillTreeData third = RunicCore.getSkillTreeAPI().loadSkillTreeData(uuid, slot, SkillTreePosition.THIRD);
//        SkillTreeData second = RunicCore.getSkillTreeAPI().getPlayerSkillTreeMap().get(uuid + ":" + SkillTreePosition.SECOND.getValue());
//        SkillTreeData third = RunicCore.getSkillTreeAPI().getPlayerSkillTreeMap().get(uuid + ":" + SkillTreePosition.THIRD.getValue());
        // todo: refactor the skill tree architecture
        first.setPerks(first.getSkillTreeBySubClass(first.getSubClass(uuid)));
        second.setPerks(second.getSkillTreeBySubClass(second.getSubClass(uuid)));
        third.setPerks(third.getSkillTreeBySubClass(third.getSubClass(uuid)));
        // --------------------------------------------
        RunicCore.getSkillTreeAPI().getPlayerSpellData(uuid, slot).resetSpells(RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(uuid)); // reset assigned spells in-memory
        RunicCore.getSkillTreeAPI().getPassives(uuid).clear(); // reset passives
        RunicCore.getStatAPI().getPlayerStatContainer(player.getUniqueId()).resetValues(); // reset stat values
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Your skill trees have been reset!");
        try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
            first.writeToJedis(uuid, jedis, RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(uuid));
            second.writeToJedis(uuid, jedis, RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(uuid));
            third.writeToJedis(uuid, jedis, RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(uuid));
            SpellData playerSpellData = RunicCore.getSkillTreeAPI().loadSpellDataFromMemory(uuid, RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(uuid), RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(uuid));
            playerSpellData.writeToJedis(uuid, jedis, RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(uuid));
        }
    }

    /**
     * Skill Trees are nested in redis, so here's a handy method to get the key
     *
     * @param uuid              of the player
     * @param slot              of the character
     * @param skillTreePosition 1, 2 or 3
     * @return a string representing the location in jedis
     */
    public static String getJedisKey(UUID uuid, int slot, SkillTreePosition skillTreePosition) {
        return uuid + ":character:" + slot + ":" + PATH_LOCATION + ":" + skillTreePosition.getValue();
    }

    /**
     * Loops through currently purchased perks to store passives in memory
     */
    public void addPassivesToMap(UUID uuid) {
        if (RunicCore.getSkillTreeAPI().getPassives(uuid) == null)
            return; // player is offline or removed from memory
        for (Perk perk : perks) {
            if (perk instanceof PerkBaseStat) continue;
            if (RunicCore.getSpellAPI().getSpell(((PerkSpell) perk).getSpellName()) == null)
                continue;
            if (!RunicCore.getSpellAPI().getSpell(((PerkSpell) perk).getSpellName()).isPassive())
                continue;
            if (perk.getCurrentlyAllocatedPoints() >= perk.getCost())
                RunicCore.getSkillTreeAPI().getPassives(uuid).add(((PerkSpell) perk).getSpellName());
        }
    }

    /**
     * Attempts to purchase perk selected from inv click event.
     * Will fail for a variety of reasons, or succeeds, updates currently allocated points, and
     *
     * @param player   purchasing the perk
     * @param slot     of the character
     * @param previous the previous perk in the perk array (to ensure perks purchased in-sequence)
     * @param perk     the perk attempting to be purchased
     */
    public void attemptToPurchasePerk(Player player, int slot, Perk previous, Perk perk) {
        int getAvailablePoints = getAvailablePoints(player.getUniqueId(), slot);
        if (perk.getCurrentlyAllocatedPoints() >= perk.getMaxAllocatedPoints()) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "You have already purchased this perk!");
            return;
        }
        if (previous != null && previous.getCurrentlyAllocatedPoints() < previous.getMaxAllocatedPoints()) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "You must purchase all previous perks first!");
            return;
        }
        if (getAvailablePoints <= 0 || getAvailablePoints < perk.getCost()) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "You don't have enough skill points to purchase this!");
            return;
        }
        perk.setCurrentlyAllocatedPoints(perk.getCurrentlyAllocatedPoints() + 1);
        if (perk instanceof PerkSpell && (RunicCore.getSpellAPI().getSpell((((PerkSpell) perk).getSpellName())).isPassive()))
            addPassivesToMap(player.getUniqueId());
        else if (perk instanceof PerkBaseStat)
            RunicCore.getStatAPI().getPlayerStatContainer(player.getUniqueId()).increaseStat(((PerkBaseStat) perk).getStat(), ((PerkBaseStat) perk).getBonusAmount());
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        player.sendMessage(ChatColor.GREEN + "You purchased a new perk!");
        try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
            this.writeToJedis(player.getUniqueId(), jedis, slot);
        }
    }

    @Override
    public Map<String, String> getDataMapFromJedis(UUID uuid, Jedis jedis, int... slot) {
        String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
        String key = getJedisKey(uuid, slot[0], position);
        /*
        Get all the values for skill tree in position. Stored as id-pointsAllocated key-value pair
         */
        return jedis.hgetAll(database + ":" + key);
    }

    @Override
    public List<String> getFields() {
        return null;
    }

    @Override
    public Map<String, String> toMap(UUID uuid, int... slot) {
        Map<String, String> skillTreeDataMap = new HashMap<>();
        for (Perk perk : this.perks) {
            if (perk == null) continue;
            skillTreeDataMap.put(String.valueOf(perk.getPerkID()), String.valueOf(perk.getCurrentlyAllocatedPoints()));
        }
        return skillTreeDataMap;
    }

    /**
     * Adds the object into session storage in redis
     *
     * @param jedis the jedis resource from core
     * @param slot  of the character
     */
    @Override
    public void writeToJedis(UUID uuid, Jedis jedis, int... slot) {
        // Inform the server that this player should be saved to mongo on next task (jedis data is refreshed)
        String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
        jedis.sadd(database + ":" + "markedForSave:core", uuid.toString());
        // Ensure the system knows that there is data in redis
        jedis.sadd(database + ":" + uuid + ":skillTreeData", String.valueOf(slot[0]));
        jedis.expire(database + ":" + uuid + ":skillTreeData", RunicDatabase.getAPI().getRedisAPI().getExpireTime());
        String key = getJedisKey(uuid, slot[0], this.getPosition());
        jedis.hmset(database + ":" + key, this.toMap(uuid));
        jedis.expire(database + ":" + key, RunicDatabase.getAPI().getRedisAPI().getExpireTime());
    }

    /**
     * Returns specified perk by ID.
     *
     * @param perkID ID of perk
     * @return the Perk object
     */
    private Perk getPerk(int perkID) {
        for (Perk perk : this.perks) {
            if (perk.getPerkID() == perkID)
                return perk;
        }
        return null;
    }

    public List<Perk> getPerks() {
        return perks;
    }

    public void setPerks(List<Perk> perks) {
        this.perks = perks;
    }

    public SkillTreePosition getPosition() {
        return position;
    }

    public void setPosition(SkillTreePosition position) {
        this.position = position;
    }

    /**
     * Returns the appropriate default perk list for the given subclass, to be populated
     * later by persistent data from the DB.
     *
     * @param subClass the subclass of the player (try SubClassUtil)
     * @return A default list of perks (no purchased perks)
     */
    private List<Perk> getSkillTreeBySubClass(SubClass subClass) throws NullPointerException {
        return switch (subClass) {
            case MARKSMAN -> ArcherTreeUtil.marksmanPerkList();
            case STORMSHOT -> ArcherTreeUtil.stormshotPerkList();
            case WARDEN -> ArcherTreeUtil.wardenPerkList();
            case BARD -> ClericTreeUtil.bardPerkList();
            case LIGHTBRINGER -> ClericTreeUtil.lightbringerPerkList();
            case SOULREAVER -> ClericTreeUtil.soulreaverPerkList();
            case CRYOMANCER -> MageTreeUtil.cryomancerPerkList();
            case PYROMANCER -> MageTreeUtil.pyromancerPerkList();
            case SPELLSWORD -> MageTreeUtil.spellswordPerkList();
            case CORSAIR -> RogueTreeUtil.corsairPerkList();
            case WITCH_HUNTER -> RogueTreeUtil.witchHunterPerkList();
            case NIGHTCRAWLER -> RogueTreeUtil.nightcrawlerPerkList();
            case BERSERKER -> WarriorTreeUtil.berserkerPerkList();
            case EARTHSHAKER -> WarriorTreeUtil.earthshakerPerkList();
            case PALADIN -> WarriorTreeUtil.paladinPerkList();
        };
    }

    public SubClass getSubClass(UUID uuid) {
        String className = RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(uuid);
        CharacterClass characterClass = CharacterClass.getFromName(className);
        return SubClass.determineSubClass(characterClass, position);
    }

}
