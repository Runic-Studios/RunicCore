package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.classes.SubClass;
import com.runicrealms.plugin.database.MongoData;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;
import com.runicrealms.plugin.spellapi.skilltrees.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SkillTreeData implements SessionData {
    public static final int FIRST_POINT_LEVEL = 10;
    public static final String PATH_LOCATION = "skillTree";
    public static final String SPELLS_LOCATION = "spells";

    private final SkillTreePosition position;
    private final SubClass subClass;
    private final UUID uuid;
    private List<Perk> perks;

    /**
     * Build default skill tree data (if there is no persistent data)
     *
     * @param uuid     of the player
     * @param position of the skill tree (1-3)
     */
    public SkillTreeData(UUID uuid, SkillTreePosition position) {
        this.position = position;
        String className = RunicCore.getCharacterAPI().getPlayerClass(uuid);
        CharacterClass characterClass = CharacterClass.getFromName(className);
        this.subClass = SubClass.determineSubClass(characterClass, position);
        this.uuid = uuid;
        this.perks = getSkillTreeBySubClass(subClass); // load default perks
    }

    /**
     * Build the character's skill tree data from mongo
     *
     * @param uuid      of the player
     * @param position  of the skill tree
     * @param character the character section of their mongo document
     * @param jedis     the jedis resource
     */
    public SkillTreeData(UUID uuid, int slot, SkillTreePosition position, PlayerMongoDataSection character, Jedis jedis) {
        this.uuid = uuid;
        this.position = position;
        SubClass subClass = SubClass.determineSubClass(RunicCore.getCharacterAPI().getPlayerClassValue(uuid), position);
        this.subClass = subClass != null ? subClass : SubClass.determineSubClass(uuid, slot, position, jedis);
        this.perks = getSkillTreeBySubClass(subClass); // load default perks for skill tree in position
        if (!character.has(PATH_LOCATION + "." + position.getValue())) return; // DB not populated
        MongoDataSection perkSection = character.getSection(PATH_LOCATION + "." + position.getValue());
        for (String key : perkSection.getKeys()) {
            Perk perk = getPerk(Integer.parseInt(key));
            if (perk == null) continue;
            perk.setCurrentlyAllocatedPoints(perkSection.get(key, Integer.class));
        }
        writeToJedis(jedis, slot);
    }

    /**
     * Build the character's skill tree data from jedis
     *
     * @param uuid     of the player
     * @param position of the skill tree (1, 2, or 3)
     * @param jedis    the jedis resource
     */
    public SkillTreeData(UUID uuid, int slot, SkillTreePosition position, Jedis jedis) {
        this.uuid = uuid;
        this.position = position;
        this.subClass = SubClass.determineSubClass(uuid, slot, position, jedis);
        this.perks = getSkillTreeBySubClass(subClass); // load default perks
        String key = getJedisKey(uuid, slot, position);
        Map<String, String> perkDataMap = jedis.hgetAll(key); // get all the values for skill tree in position
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
        SkillTreeData first = RunicCore.getSkillTreeAPI().getPlayerSkillTreeMap().get(uuid + ":" + SkillTreePosition.FIRST.getValue());
        SkillTreeData second = RunicCore.getSkillTreeAPI().getPlayerSkillTreeMap().get(uuid + ":" + SkillTreePosition.SECOND.getValue());
        SkillTreeData third = RunicCore.getSkillTreeAPI().getPlayerSkillTreeMap().get(uuid + ":" + SkillTreePosition.THIRD.getValue());
        first.setPerks(first.getSkillTreeBySubClass(first.getSubclass()));
        second.setPerks(second.getSkillTreeBySubClass(second.getSubclass()));
        third.setPerks(third.getSkillTreeBySubClass(third.getSubclass()));
        // --------------------------------------------
        RunicCore.getSkillTreeAPI().getPlayerSpellMap().get(uuid).resetSpells(); // reset assigned spells in-memory
        RunicCore.getSkillTreeAPI().getPassives(uuid).clear(); // reset passives
        RunicCore.getStatAPI().getPlayerStatContainer(player.getUniqueId()).resetValues(); // reset stat values
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Your skill trees have been reset!");
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            first.writeToJedis(jedis, RunicCore.getCharacterAPI().getCharacterSlot(uuid));
            second.writeToJedis(jedis, RunicCore.getCharacterAPI().getCharacterSlot(uuid));
            third.writeToJedis(jedis, RunicCore.getCharacterAPI().getCharacterSlot(uuid));
            PlayerSpellData playerSpellData = RunicCore.getSkillTreeAPI().loadPlayerSpellData(uuid, RunicCore.getCharacterAPI().getCharacterSlot(uuid));
            playerSpellData.writeToJedis(jedis, RunicCore.getCharacterAPI().getCharacterSlot(uuid));
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
    public void addPassivesToMap() {
        if (RunicCore.getSkillTreeAPI().getPassives(uuid) == null) return; // player is offline or removed from memory
        for (Perk perk : perks) {
            if (perk instanceof PerkBaseStat) continue;
            if (RunicCore.getSpellAPI().getSpell(((PerkSpell) perk).getSpellName()) == null) continue;
            if (!RunicCore.getSpellAPI().getSpell(((PerkSpell) perk).getSpellName()).isPassive()) continue;
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
            addPassivesToMap();
        else if (perk instanceof PerkBaseStat)
            RunicCore.getStatAPI().getPlayerStatContainer(player.getUniqueId()).increaseStat(((PerkBaseStat) perk).getStat(), ((PerkBaseStat) perk).getBonusAmount());
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        player.sendMessage(ChatColor.GREEN + "You purchased a new perk!");
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            this.writeToJedis(jedis, slot);
        }
    }

    @Override
    public Map<String, String> getDataMapFromJedis(Jedis jedis, int... slot) {
        String key = getJedisKey(uuid, slot[0], position);
        /*
        Get all the values for skill tree in position. Stored as id-pointsAllocated key-value pair
         */
        return jedis.hgetAll(key);
    }

    @Override
    public List<String> getFields() {
        return null;
    }

    @Override
    public Map<String, String> toMap() {
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
    public void writeToJedis(Jedis jedis, int... slot) {
        String key = getJedisKey(this.uuid, slot[0], this.getPosition());
        jedis.hmset(key, this.toMap());
        jedis.expire(key, RunicCore.getRedisAPI().getExpireTime());
    }

    @Override
    public MongoData writeToMongo(MongoData mongoData, int... slot) {
        try {
            PlayerMongoData playerMongoData = (PlayerMongoData) mongoData;
            PlayerMongoDataSection character = playerMongoData.getCharacter(slot[0]);
            PlayerMongoDataSection skillTrees = (PlayerMongoDataSection) character.getSection(SkillTreeData.PATH_LOCATION + "." + this.position.getValue());
            for (Perk perk : this.perks) {
                if (perk.getCurrentlyAllocatedPoints() == 0) continue;
                skillTrees.set(perk.getPerkID().toString(), perk.getCurrentlyAllocatedPoints());
            }
        } catch (Exception e) {
            RunicCore.getInstance().getLogger().info("[ERROR]: There was a problem saving skill tree data to mongo!");
            e.printStackTrace();
        }
        return mongoData;
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

    /**
     * Returns the appropriate default perk list for the given subclass, to be populated
     * later by persistent data from the DB.
     *
     * @param subClass the subclass of the player (try SubClassUtil)
     * @return A default list of perks (no purchased perks)
     */
    private List<Perk> getSkillTreeBySubClass(SubClass subClass) throws NullPointerException {
        switch (subClass) {
            case MARKSMAN:
                return ArcherTreeUtil.marksmanPerkList();
            case STORMSHOT:
                return ArcherTreeUtil.stormshotPerkList();
            case WARDEN:
                return ArcherTreeUtil.wardenPerkList();
            case BARD:
                return ClericTreeUtil.bardPerkList();
            case SOULREAVER:
                return ClericTreeUtil.soulreaverPerkList();
            case LIGHTBRINGER:
                return ClericTreeUtil.lightbringerPerkList();
            case ARCANIST:
                return MageTreeUtil.arcanistPerkList();
            case CRYOMANCER:
                return MageTreeUtil.cryomancerPerkList();
            case PYROMANCER:
                return MageTreeUtil.pyromancerPerkList();
            case ASSASSIN:
                return RogueTreeUtil.assassinPerkList();
            case CORSAIR:
                return RogueTreeUtil.corsairPerkList();
            case DUELIST:
                return RogueTreeUtil.duelistPerkList();
            case BERSERKER:
                return WarriorTreeUtil.berserkerPerkList();
            case GUARDIAN:
                return WarriorTreeUtil.guardianPerkList();
            case PALADIN:
                return WarriorTreeUtil.paladinPerkList();
        }
        return null;
    }

    public SubClass getSubclass() {
        return subClass;
    }

    public UUID getUuid() {
        return uuid;
    }

}
