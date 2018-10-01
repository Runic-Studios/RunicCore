package us.fortherealm.plugin.skills.caster.itemstack;

import com.mysql.jdbc.StringUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.SkillRegistry;
import us.fortherealm.plugin.skills.caster.Caster;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CasterItemStack extends ItemStack implements ICasterItemStack {
	
	private static long nextId;
	private static long currentThousandthId;
	private static Map<Long, CasterItemStack> iHateSpigotMap = new HashMap<>();
	
	private static Map<CasterItemStack, Player> castersOnCooldown = new HashMap<>();
	private static boolean isTaskRunning;
	
	private String name;
	private int cooldown;
	private long lastCast;
	private Type itemType;
	
	private List<Skill> primarySkills = new ArrayList<>();
	private List<Skill> secondarySkills = new ArrayList<>();

	static {
		File casterData = new File("src/main/java/us/fortherealm/plugin/skills/caster/CasterData.yml");
		if(!(casterData.exists()))
			casterData.mkdir();
		YamlConfiguration yamlData = YamlConfiguration.loadConfiguration(casterData);
		if(!(yamlData.isSet("thousandthId")))
			yamlData.set("thousandthId", 0);

		CasterItemStack.nextId = (yamlData.getLong("thousandthId") + 1) * 1000;
		CasterItemStack.currentThousandthId = yamlData.getLong("thousandthId") + 1;

		yamlData.set("thousandthId", currentThousandthId);

		try {
			yamlData.save(casterData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private CasterItemStack(ItemStack item) {
		super(item);

		ItemMeta meta = item.getItemMeta();

		this.name = parseName(meta);
		this.itemType = parseItemType(meta);

		this.primarySkills = parseSkills(meta, "p");
		this.secondarySkills = parseSkills(meta, "s");

		setItemMeta(meta);
	}

	public CasterItemStack(Material itemMaterial, String name, Type itemType, double cooldown,
	                       List<Skill> primarySkills, List<Skill> secondarySkills) {
		super(itemMaterial);
		
		this.name = name;
		this.itemType = itemType;
		
		if(primarySkills != null)
			this.primarySkills.addAll(primarySkills);
		
		if(secondarySkills != null)
			this.secondarySkills.addAll(secondarySkills);

		setItemMeta(generateItemMeta());
	}
	
	public void executeSkill(Skill skill, Player player) {
		if(this.isOnCooldown())
			return;

		this.lastCast = System.currentTimeMillis();
		
		displayCooldown(player);
		
		skill.executeEntireSkill(player);
	}
	
	public void executePrimarySkills(Player player) {
		for(Skill skill : primarySkills) {
			executeSkill(skill, player);
		}
	}
	
	public void executeSecondarySkills(Player player) {
		for(Skill skill : secondarySkills) {
			executeSkill(skill, player);
		}
	}
	
	private ItemMeta generateItemMeta() {
		ItemMeta itemMeta = getItemMeta();
		
		itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);


		// Changing this requires changing the parseName method
		itemMeta.setDisplayName(ChatColor.YELLOW + name);

		List<String> lore = new ArrayList<>();
		
		if(primarySkills.size() != 0) {
			
			
			lore.add(ChatColor.GRAY + "Primary:");
			
			for(Skill skill : primarySkills)
				lore.add(ChatColor.RED + skill.getName());
			
			lore.add("");
		}
		
		if(secondarySkills.size() != 0) {
			
			lore.add(ChatColor.GRAY + "Secondary:");
			
			for (Skill skill : secondarySkills)
				lore.add(ChatColor.DARK_RED + skill.getName());
			
			lore.add("");
			
		}
		
		// Do not change until stated

		// Changing this requires changing the getSignature, getId, and respective contains methods
		// Signature and ID creator
		StringBuilder sb = new StringBuilder();

		for(char c : getCasterSignature().toCharArray()) {
			if (c == '.')
				continue;
			sb.append(ChatColor.COLOR_CHAR + String.valueOf(c));
		}

		sb.append('.');

		for(char c : String.valueOf(nextId).toCharArray()) {
			if (c == '.')
				continue;
			sb.append(ChatColor.COLOR_CHAR + String.valueOf(c));
		}

		// Bug testing
		lore.add(sb.toString().replace(String.valueOf(ChatColor.COLOR_CHAR), ""));

		// Legit
//		lore.add(sb.toString());

		// Caster parser crator
		sb = new StringBuilder();

		// Changing this requires changing the parseSkills method
		sb.append('p');
		for(Skill pSkill : primarySkills)
			for(SkillRegistry registeredSkill : SkillRegistry.values())
				if(registeredSkill.getSkill().equals(pSkill))
					sb.append('.' + registeredSkill.getUniqueId());

		sb.append(".s");
		for(Skill sSkill : secondarySkills)
			for(SkillRegistry registeredSkill : SkillRegistry.values())
				if(registeredSkill.getSkill().equals(sSkill))
					sb.append('.' + registeredSkill.getUniqueId());

		StringBuilder sb2 = new StringBuilder();
		for(char c : sb.toString().toCharArray()) {
			sb2.append(ChatColor.COLOR_CHAR + String.valueOf(c));
		}

		// Bug testing
		lore.add(sb2.toString().replace(String.valueOf(ChatColor.COLOR_CHAR), ""));

		// Legit
		lore.add(sb2.toString());

//		lore.add(sb2.toString());

		// Changing this requires changing the parseItemType method
		// Make sure this is 1 line
		lore.add(ChatColor.YELLOW + itemType.getName());

		itemMeta.setLore(lore);

		// End do not change
		
		return itemMeta;
	}

	private String parseName(ItemMeta meta) {
		return ChatColor.stripColor(meta.getDisplayName());
	}

	private Type parseItemType(ItemMeta meta) {
		List<String> lore = meta.getLore();
		for(Type type : Type.values())
			if(type.getName().equals(lore.get(lore.size() - 1)))
				return type;
		return null;
	}

	private List<Skill> parseSkills(ItemMeta meta, String keyword) {
		List<Skill> primarySkills = new ArrayList<>();
		List<String> lore = meta.getLore();
		String[] skillInfo = ChatColor.stripColor(lore.get(lore.size() - 2)).split(".");
		boolean isOnGoodStuff = false;
		for(String info : skillInfo) {
			if(isOnGoodStuff) {
				if(!(StringUtils.isStrictlyNumeric(info)))
					break;
				for(SkillRegistry registeredSkill : SkillRegistry.values()) {
					if(registeredSkill.getUniqueId() == Integer.valueOf(info))
						primarySkills.add(registeredSkill.getSkill());
				}
			}
			if(info.equalsIgnoreCase(keyword))
				isOnGoodStuff = true;
		}
		return primarySkills;
	}
	
	private void displayCooldown(Player player) {
		synchronized (castersOnCooldown) {
			castersOnCooldown.put(this, player);
		}
		
		if(isTaskRunning)
			return;
		
		isTaskRunning = true;
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				
				Set<CasterItemStack> oldCastersOnCooldown = new HashSet<>(); // Avoids threading issues
				
				synchronized(castersOnCooldown) {
					for(CasterItemStack casterItem : castersOnCooldown.keySet()) {
						if(!casterItem.isOnCooldown()) {
							oldCastersOnCooldown.add(casterItem);
							continue;
						}
						
						castersOnCooldown.get(casterItem).spigot().sendMessage(
								ChatMessageType.ACTION_BAR,
								new net.md_5.bungee.api.chat.TextComponent(
										casterItem.getName() + " is on cooldown for " +
												casterItem.getCurrentCooldown()/1000 + " more seconds"
								)
						);
					}
				}
				
				synchronized (castersOnCooldown) {
					for(CasterItemStack casterItem : oldCastersOnCooldown) {
						castersOnCooldown.remove(casterItem);
					}
					oldCastersOnCooldown.clear();
					
					if(castersOnCooldown.isEmpty()) {
						this.cancel();
						return;
					}
				}
				
			}
		}.runTaskTimer(Main.getInstance(), 0, 10);
		
		isTaskRunning = false;
	}
	
	public final static CasterItemStack getCasterItem(ItemStack item) {
		if(!(containsCasterSignature(item)))
			return null;
		if(iHateSpigotMap.get(getItemId(item)) == null)
			iHateSpigotMap.put(Long.valueOf(getItemId(item)), new CasterItemStack(item));

		return iHateSpigotMap.get(getItemId(item));
	}

	// Skill API will break if this is changed
	public final static String getCasterSignature() {
		return "69Caster69";
	}

	public final static boolean containsCasterSignature(ItemStack item) {
		List<String> lore = item.getItemMeta().getLore();
		if(lore.size() < 3)
			return false;
		String[] lastWords = lore.get(lore.size() - 3).split(".");
		return lastWords[0].replace(String.valueOf(ChatColor.COLOR_CHAR), "").equals(getCasterSignature());
	}
	
	public final static String getItemId(ItemStack item) {
		if(!(containsItemId(item)))
			return null;
		List<String> lore = item.getItemMeta().getLore();
		String[] lastWords = lore.get(lore.size() - 3).split(".");
		return lastWords[1].replace(String.valueOf(ChatColor.COLOR_CHAR), "");
	}
	
	public final static boolean containsItemId(ItemStack item) {
		List<String> lore = item.getItemMeta().getLore();
		if(lore.size() < 3)
			return false;
		String[] lastWords = lore.get(lore.size() - 3).split(".");
		return lastWords.length == 2 || !(containsCasterSignature(item));
	}
	
	private boolean isOnCooldown() {
		return getCurrentCooldown() < cooldown * 1000;
	}
	
	private long getCurrentCooldown() {
		return cooldown*1000 - (System.currentTimeMillis() - lastCast);
	}
	
	public int getTotalCooldown() {
		return cooldown;
	}
	
	public void setCooldown(int cooldown) {
		this.cooldown = cooldown;
	}
	
	public long getLastCast() {
		return lastCast;
	}
	
	public void setLastCast(long lastCast) {
		this.lastCast = lastCast;
	}
	
	public Type getItemType() {
		return itemType;
	}
	
	public String getName() {
		return name;
	}
	
	public enum Type {
		RUNE(0, "Rune"),
		ARTIFACT(1, "Artifact");
		
		private String name;
		private int slot;
		
		Type(int slot, String name) {
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
	
}
