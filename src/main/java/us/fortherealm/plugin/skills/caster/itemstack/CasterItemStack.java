package us.fortherealm.plugin.skills.caster.itemstack;

import com.mysql.jdbc.StringUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.SkillRegistry;
import us.fortherealm.plugin.cooldown.RealmCooldown;
import us.fortherealm.plugin.util.ItemMetaUtil;
import us.fortherealm.plugin.util.UniqueIdAssigner;

import java.util.*;

public class CasterItemStack extends ItemStack implements ICasterItemStack {

    // The UniqueIdAssigner is how each caster item stack gets a unique id and the activeCastersMap
    // holds all casters currently in use
    private static UniqueIdAssigner uniqueIdAssigner;
    private static Map<Long, CasterItemStack> activeCastersMap = new HashMap<>();
    private static Plugin plugin = Main.getInstance();

    // ----- NEVER CHANGE ----- //
    private final static String ID_SPLITTER = "|";
    private final static String PRIMARY_ID =  "prim";
    private final static String SECONDARY_ID = "sec";
    // ----- NEVER CHANGE ----- //

    // Stores the name and itemType of the CasterItemStack
    private String name;
    private ItemType itemType;

    // The primary and secondary skills storage
    private List<Skill> primarySkills = new ArrayList<>();
    private List<Skill> secondarySkills = new ArrayList<>();

    // The cooldownStorage for primary and secondary skills, separated into artifact and rune
    private final RealmCooldown primaryCD;
    private final RealmCooldown secondaryCD;

    // Assigns the uniqueIdAssigner with a reference for all of the CasterItemStacks to use
    static {
        CasterItemStack.uniqueIdAssigner = new UniqueIdAssigner("casterItemUniqueId");
    }

    // The constructor to create a CasterItemStack
    public CasterItemStack(ItemStack item, String name, ItemType itemType,
                           List<Skill> primarySkills, double primaryCooldown,
                           List<Skill> secondarySkills, double secondaryCooldown) {

        // Sets the physical item
        super(item);

        // Sets the name and itemType
        this.name = name;
        this.itemType = itemType;

        // Sets the primary skills and the primary skills cooldown
        if(primarySkills != null)
            this.primarySkills.addAll(primarySkills);
        this.primaryCD = new RealmCooldown(primaryCooldown);

        // Sets the secondary skills and the secondary skills cooldown
        if(secondarySkills != null)
            this.secondarySkills.addAll(secondarySkills);
        this.secondaryCD = new RealmCooldown(secondaryCooldown);

        // Sets the itemMeta so it can be parsed (for artifacts)
        if (itemType == ItemType.ARTIFACT) {
            if (item.getType() == Material.BOW) {
                setItemMeta(generateItemMeta(ItemType.ARTIFACT, true));
            } else {
                setItemMeta(generateItemMeta(ItemType.ARTIFACT, false));
            }
        }

        // Sets the itemMeta so it can be parsed (for runes)
        if (itemType == ItemType.RUNE) {
            setItemMeta(generateItemMeta(ItemType.RUNE, false));
        }
    }

    // The constructor for itemStacks being parsed into CasterItemStacks
    private CasterItemStack(ItemStack item) {

        // Sets the physical item
        super(item);

        // Get the itemMeta of the item so it can be parsed
        ItemMeta meta = item.getItemMeta();

        // Sets the name and itemType
        this.name = parseName(meta);
        this.itemType = parseItemType(meta);

        // Sets the primary skills and primary skills cooldown
        this.primarySkills = parseSkills(meta, PRIMARY_ID);
        // System.out.println(parseCooldown(meta, PRIMARY_ID));
        this.primaryCD = new RealmCooldown(parseCooldown(meta, PRIMARY_ID));

        // Sets the secondary skills and the secondary skills cooldown
        this.secondarySkills = parseSkills(meta, SECONDARY_ID);
        this.secondaryCD = new RealmCooldown(parseCooldown(meta, SECONDARY_ID));
    }

    // Executes the specified skill with the player as the user
    @Override
    public void executeSkill(Skill skill, Player player) {
        skill.executeEntireSkill(player);
    }

    // Executes all of the primary skills if they are not on cooldown
    public void executePrimarySkills(Player player) {
        for (Skill skill : primarySkills) {

            if (primaryCD.isOnCooldown()) {
                return;
            }

            executeSkill(skill, player);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "You cast " + ChatColor.WHITE + skill.getName() + ChatColor.GREEN + "!"));
            primaryCD.setCooldown(skill.getCooldown());
            primaryCD.setLastCast(System.currentTimeMillis());


            if (cooldown1.containsKey(player.getUniqueId())) {
                HashMap<Skill, RealmCooldown> playerSkillsOnCooldown = cooldown1.get(player.getUniqueId());
                playerSkillsOnCooldown.put(skill, primaryCD);

                cooldown1.put(player.getUniqueId(), playerSkillsOnCooldown);
            } else {
                HashMap<Skill, RealmCooldown> playerSkillsOnCooldown = new HashMap<>();
                playerSkillsOnCooldown.put(skill, primaryCD);
                cooldown1.put(player.getUniqueId(), playerSkillsOnCooldown);
            }
        }
    }

    //private List<String> cdString = new ArrayList<>();

    // Executes all of the secondary skills if they are not on cooldown
    private ArrayList<String> cdString = new ArrayList<>();

    // create an array of skills on cooldown
    public static HashMap<UUID, HashMap<Skill, RealmCooldown>> cooldown1 = new HashMap<>();

    public void executeSecondarySkills(Player player) {

        for (Skill skill : secondarySkills) {

            if (secondaryCD.isOnCooldown()) {
                return;
            }

            executeSkill(skill, player);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "You cast " + ChatColor.WHITE + skill.getName() + ChatColor.GREEN + "!"));
            secondaryCD.setCooldown(skill.getCooldown());
            secondaryCD.setLastCast(System.currentTimeMillis());


            if (cooldown1.containsKey(player.getUniqueId())) {
                HashMap<Skill, RealmCooldown> playerSkillsOnCooldown = cooldown1.get(player.getUniqueId());
                playerSkillsOnCooldown.put(skill, secondaryCD);

                cooldown1.put(player.getUniqueId(), playerSkillsOnCooldown);
            } else {
                HashMap<Skill, RealmCooldown> playerSkillsOnCooldown = new HashMap<>();
                playerSkillsOnCooldown.put(skill, secondaryCD);
                cooldown1.put(player.getUniqueId(), playerSkillsOnCooldown);
            }
        }
    }


    // Generates the item meta
    private ItemMeta generateItemMeta(ItemType type, boolean isBow) {
        // Gets the current Item Meta
        ItemMeta itemMeta = getItemMeta();

        // Add item flags
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        itemMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        // Sets the name of the item
        // Note: This is used to parse the item name, so
        // changing this requires changing the parseName method
        itemMeta.setDisplayName(ChatColor.YELLOW + name);

        // Generates a new list of strings to become the lore
        List<String> lore = new ArrayList<>();

        // Sets the lore visible to the player
        if (type == ItemType.ARTIFACT) {
            if (isBow) {
                lore = generateArtifactLore(lore, true);
            } else {
                lore = generateArtifactLore(lore, false);
            }
        } else if (type == ItemType.RUNE) {
            lore = generateRuneLore(lore);
        }

        // Sets the invisible lore used to parse the stats
        lore = generateInvisibleLore(lore);

        // The parsing methods expect one line after the
        // invisible lore which will can be filled by the item type
        // Note: This is used to parse the item type, so
        // changing this requires changing the parseType method
        lore.add(ChatColor.YELLOW + itemType.getName());

        // Sets the lore
        itemMeta.setLore(lore);

        // Returns the generated ItemMeta
        return itemMeta;
    }

    // set visible Artifact lore
    protected List<String> generateArtifactLore(List<String> lore, boolean isBow) {
        if(primarySkills.size() != 0) {

            // adds lore for each skill
            for(Skill skill : primarySkills) {
                if (isBow) {
                    lore.add(ChatColor.GRAY + "Left-click: " + ChatColor.GREEN + skill.getName());
                } else {
                    lore.add(ChatColor.GRAY + "Sneak + Left-click: " + ChatColor.GREEN + skill.getName());
                }
            }

        }

        if(secondarySkills.size() != 0) {

            // adds lore for each skill
            for (Skill skill : secondarySkills) {
                if (isBow) {
                    lore.add(ChatColor.GRAY + "Sneak + Right-click: " + ChatColor.GREEN + skill.getName());
                } else {
                    lore.add(ChatColor.GRAY + "Right-click: " + ChatColor.GREEN + skill.getName());
                }
            }
        }
        return lore;
    }

    // sets the visible rune lore
    protected List<String> generateRuneLore(List<String> lore) {
        if(primarySkills.size() != 0) {

            // adds lore for each skill
            for(Skill skill : primarySkills)
                lore.add(ChatColor.GRAY + "Left-click: " + ChatColor.GREEN + skill.getName());
        }

        if(secondarySkills.size() != 0) {

            // adds lore for each skill
            for (Skill skill : secondarySkills)
                lore.add(ChatColor.GRAY + "Right-click: " + ChatColor.GREEN + skill.getName());
        }
        return lore;
    }

    // Sets the invisible lore which will be used to parse item stats
    private List<String> generateInvisibleLore(List<String> lore) {

        // Signature and ID creator
        String sigID = getCasterSignature() + ID_SPLITTER + uniqueIdAssigner.getUniqueId();

        // Bug testing - a bit ugly
        //lore.add(ItemMetaUtil.revealHiddenLore(sigID));

        // Legit do not remove
        lore.add(ItemMetaUtil.hideLore(sigID));

        // Skills and cooldown creator
        StringBuilder sb = new StringBuilder();
        sb.append(PRIMARY_ID + ID_SPLITTER + primaryCD.getCooldown());
        for(Skill pSkill : primarySkills)


            for(SkillRegistry registeredSkill : SkillRegistry.values()) {
                if (registeredSkill.getSkill().getClass().equals(pSkill.getClass()))
                    sb.append(ID_SPLITTER + registeredSkill.getUniqueId());
            }

        sb.append(ID_SPLITTER + SECONDARY_ID + ID_SPLITTER + secondaryCD.getCooldown());
        for(Skill sSkill : secondarySkills) {
            for (SkillRegistry registeredSkill : SkillRegistry.values()) {
                if (registeredSkill.getSkill().getClass().equals(sSkill.getClass()))
                    sb.append(ID_SPLITTER + registeredSkill.getUniqueId());
            }
        }

        // Bug testing
        //lore.add(sb.toString());

        // Legit
        lore.add(ItemMetaUtil.hideLore(sb.toString()));

        return lore;
    }

    // Parses the caster's name of the item from the meta's display name
    private String parseName(ItemMeta meta) {
        return ChatColor.stripColor(meta.getDisplayName());
    }

    // Parses the itemType from the last value in the lore
    private ItemType parseItemType(ItemMeta meta) {
        List<String> lore = meta.getLore();
        for(ItemType type : ItemType.values())
            if(type.getName().equals(ChatColor.stripColor(lore.get(lore.size() - 1))))
                return type;
        return null;
    }

    // Parses skills given the item meta and the keyword that indicates where
    // parsing should begin
    private List<Skill> parseSkills(ItemMeta meta, String keyword) {
        // Creates the list of skills the item contains
        List<Skill> skills = new ArrayList<>();

        // Gets the lore from the item meta
        List<String> lore = meta.getLore();

        // Gets the skill info which is located on the second to last line
        // of the item meta's lore. Also reveals the hidden text and splits
        // the skill info at the location of every ID_SPLITTER
        String[] skillInfo = ItemMetaUtil.revealHiddenLore(lore.get(lore.size() - 2))
                .split("\\" + ID_SPLITTER);

        // Gets the index to begin parsing skills
        int startIndex = getSkillStartIndex(skillInfo, keyword);

        // Returns no skills if the start was not found
        if(startIndex == -1)
            return skills;

        // Iterates from the startIndex to the end of the skill info
        for(int c = startIndex + 1 /* Skips the first index, because the cooldown is located here */;
            c < skillInfo.length;
            c++) {

            // Breaks the loop if the value is not numeric (meaning all the info from the keyword has been parsed)
            if(!(StringUtils.isStrictlyNumeric(skillInfo[c])))
                break;

            // Checks with registered skills to see what the current number corresponds to
            for(SkillRegistry registeredSkill : SkillRegistry.values())
                if(registeredSkill.getUniqueId() == Integer.valueOf(skillInfo[c]))
                    skills.add(registeredSkill.getSkill());
        }

        // Returns all of the skills
        return skills;
    }

    private double parseCooldown(ItemMeta meta, String keyword) {
        // Gets the lore from the item meta
        List<String> lore = meta.getLore();

        // Gets the skill info which is located on the second to last line
        // of the item meta's lore. Also reveals the hidden text and splits
        // the skill info at the location of every ID_SPLITTER
        String[] skillInfo = ItemMetaUtil.revealHiddenLore(lore.get(lore.size() - 2))
                .split("\\" + ID_SPLITTER);

        // Returns the double value of the skillInfo at the cooldown's location
        return Double.valueOf(skillInfo[getSkillStartIndex(skillInfo, keyword)]);
    }

    // Gets the location to start parsing given a keyword
    // Note: Skills begin parsing at the cooldown then the skill ids
    // Returns -1 if the start does not exist
    private int getSkillStartIndex(String[] skillInfo, String keyword) {
        boolean isFound = false;
        int startIndex = 1;
        for(String info : skillInfo) {
            if (info.equalsIgnoreCase(keyword)) {
                isFound = true;
                break;
            }
            startIndex++;
        }

        return (isFound) ? startIndex : -1;
    }

    // The static method used to convert ItemStacks to CasterItemStacks
    public final static CasterItemStack getCasterItem(ItemStack item) {

        // Returns null if the item is not a caster item stack
        if(!(containsCasterSignature(item))) {
            return null;
        }

        // Checks if the item has already been parsed. If not, it parses
        // the item and stores it at the item's unique ID location.
        if(activeCastersMap.get(Long.valueOf(getItemId(item))) == null) {
            activeCastersMap.put(Long.valueOf(getItemId(item)), new CasterItemStack(item));
        }

        // Returns the CasterItemStack stored at the location of  the items unique ID
        return activeCastersMap.get(Long.valueOf(getItemId(item)));
    }

    // All casters contain this breathtaking signature
    public final static String getCasterSignature() {
        return "69";
    }

    // Determines if an item contains the Caster Signature
    public final static boolean containsCasterSignature(ItemStack item) {

        // False if the item is null
        if(item == null)
            return false;

        // Gets the lore
        List<String> lore = item.getItemMeta().getLore();

        // False if the lore is null
        if(lore == null)
            return false;

        // False if the lore is not atleast three lines long
        if(lore.size() < 3) {
            return false;
        }

        // Gets the third to last line in the lore which should contain the signature and ID.
        // Also reveals the text and splits it at splitter locations.
        String[] sigId = ItemMetaUtil.revealHiddenLore(lore.get(lore.size() - 3))
                .split("\\" + ID_SPLITTER);

        // Returns false if the length of sigID is not 2 (signature and id)
        if(sigId.length != 2)
            return false;

        // Returns if sig equals the caster signature
        return sigId[0].equals(getCasterSignature());
    }

    // Get the item's unique ID
    public final static String getItemId(ItemStack item) {

        // Returns null if the item does not have an id
        if(!(containsItemId(item)))
            return null;

        // Gets the lore from the item
        List<String> lore = item.getItemMeta().getLore();

        // Gets the third to last line in the lore which should contain the signature and ID.
        // Also reveals the text and splits it at splitter locations.
        String[] sigId = ItemMetaUtil.revealHiddenLore(lore.get(lore.size() - 3))
                .split("\\" + ID_SPLITTER);

        // Returns the id
        return sigId[1];
    }

    // Determines if the given item contains an item Id
    public final static boolean containsItemId(ItemStack item) {

        // Returns false if the item doesn't contain a signature
        if(!(containsCasterSignature(item)))
            return false;

        // Gets the item's lore from the item
        List<String> lore = item.getItemMeta().getLore();

        // Gets the third to last line in the lore which should contain the signature and ID.
        // Also reveals the text and splits it at splitter locations.
        String[] sigId = ItemMetaUtil.revealHiddenLore(lore.get(lore.size() - 3))
                .split("\\" + ID_SPLITTER);

        // Returns true if the length is 2
        return sigId.length == 2;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public String getName() {
        return name;
    }

    public enum ItemType {
        RUNE(0, "Rune"),
        ARTIFACT(1, "Artifact");

        private String name;
        private int slot;

        ItemType(int slot, String name) {
            this.slot = slot;
            this.name = name;
        }

        public int getSlot() {
            return slot;
        }

        public String getName() {
            return name;
        }
    }

    public static void startCooldownTask() {
        new BukkitRunnable() {
            @Override
            public void run() {

                if (cooldown1 == null) { return; }
                for(Player player : Bukkit.getOnlinePlayers()) {
                    if (cooldown1.containsKey(player.getUniqueId())) {
                        HashMap<Skill, RealmCooldown> skills = cooldown1.get(player.getUniqueId());
                        List<String> cdString = new ArrayList<>();

                        if (skills != null) {
                            for (Skill skill : skills.keySet()) {

                                if (skill == null) { continue; }

                                double cooldownRemaining = (int) ((skills.get(skill).getLastCast() + ((skill.getCooldown() + 1) * 1000)) - System.currentTimeMillis());

                                if (cooldownRemaining > 0) {

                                    cdString.add(ChatColor.RED + skill.getName() + ChatColor.RED + ": " + ChatColor.YELLOW + (int) (cooldownRemaining / 1000) + "s");
                                } else {
                                    RealmCooldown cooldown = skills.get(skill);
                                    cooldown.setCooldown(0);
                                    cooldown1.get(player.getUniqueId()).remove(skill);
                                }
                            }

                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + String.join(ChatColor.YELLOW + " ", cdString)));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 10);
    }
}

