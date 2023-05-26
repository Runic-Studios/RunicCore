package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.SpellAPI;
import com.runicrealms.plugin.api.event.SpellShieldEvent;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.model.SpellData;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.spellapi.spells.Potion;
import com.runicrealms.plugin.spellapi.spells.archer.*;
import com.runicrealms.plugin.spellapi.spells.artifact.*;
import com.runicrealms.plugin.spellapi.spells.cleric.*;
import com.runicrealms.plugin.spellapi.spells.mage.*;
import com.runicrealms.plugin.spellapi.spells.rogue.*;
import com.runicrealms.plugin.spellapi.spells.warrior.*;
import com.runicrealms.plugin.spellapi.spelltypes.Shield;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.HologramUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpellManager implements Listener, SpellAPI {
    private final List<Spell> spellList;
    private final ConcurrentHashMap<UUID, ConcurrentHashMap<Spell, Long>> cooldownMap;
    private final HashMap<UUID, Shield> shieldedPlayers;
    private final RunicCore plugin = RunicCore.getInstance();

    public SpellManager() {
        this.spellList = new ArrayList<>();
        this.cooldownMap = new ConcurrentHashMap<>();
        this.shieldedPlayers = new HashMap<>();
        this.registerSpells();
        this.startCooldownTask();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void addCooldown(final Player player, final Spell spell, double cooldownTime) {
        if (this.cooldownMap.containsKey(player.getUniqueId())) {
            ConcurrentHashMap<Spell, Long> playerSpellsOnCooldown = this.cooldownMap.get(player.getUniqueId());
            playerSpellsOnCooldown.put(spell, System.currentTimeMillis());
            this.cooldownMap.put(player.getUniqueId(), playerSpellsOnCooldown);
        } else {
            ConcurrentHashMap<Spell, Long> playerSpellsOnCooldown = new ConcurrentHashMap<>();
            playerSpellsOnCooldown.put(spell, System.currentTimeMillis());
            this.cooldownMap.put(player.getUniqueId(), playerSpellsOnCooldown);
        }
    }

    @Override
    public Spell getPlayerSpell(Player player, int number) {
        Spell spellToCast = null;
        UUID uuid = player.getUniqueId();
        try {
            int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(uuid);
            SpellData playerSpellData = RunicCore.getSkillTreeAPI().getPlayerSpellData(uuid, slot);
            switch (number) {
                case 1 -> {
                    spellToCast = this.getSpellByName(playerSpellData.getSpellHotbarOne());
                    if (playerSpellData.getSpellHotbarOne().equals("")) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                        player.sendMessage(ChatColor.RED + "You have no spell set in this slot!");
                    }
                }
                case 2 -> {
                    spellToCast = this.getSpellByName(playerSpellData.getSpellLeftClick());
                    if (playerSpellData.getSpellLeftClick().equals("")) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                        player.sendMessage(ChatColor.RED + "You have no spell set in this slot!");
                    }
                }
                case 3 -> {
                    spellToCast = this.getSpellByName(playerSpellData.getSpellRightClick());
                    if (playerSpellData.getSpellRightClick().equals("")) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                        player.sendMessage(ChatColor.RED + "You have no spell set in this slot!");
                    }
                }
                case 4 -> {
                    spellToCast = this.getSpellByName(playerSpellData.getSpellSwapHands());
                    if (playerSpellData.getSpellSwapHands().equals("")) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                        player.sendMessage(ChatColor.RED + "You have no spell set in this slot!");
                    }
                }
            }
        } catch (NullPointerException e) {
            // haha sky is lazy
        }
        return spellToCast;
    }

    @Override
    public HashMap<UUID, Shield> getShieldedPlayers() {
        return this.shieldedPlayers;
    }

    @Override
    public Spell getSpell(String name) {
        return this.getSpellByName(name);
    }

    @Override
    public ConcurrentHashMap.KeySetView<Spell, Long> getSpellsOnCooldown(UUID uuid) {
        if (cooldownMap.containsKey(uuid)) {
            return cooldownMap.get(uuid).keySet();
        } else {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void healPlayer(Player caster, Player recipient, double amount, Spell... spell) {
        // Call our custom heal event for interaction with buffs/de buffs
        SpellHealEvent event = spell.length > 0
                ? new SpellHealEvent((int) amount, recipient, caster, spell)
                : new SpellHealEvent((int) amount, recipient, caster);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        amount = event.getAmount();

        double newHP = recipient.getHealth() + amount;
        double difference = recipient.getMaxHealth() - recipient.getHealth();

        // If they are missing less than healAmt
        if (newHP > recipient.getMaxHealth()) {

            recipient.setHealth(recipient.getMaxHealth());

            if (difference == (int) difference) {
                if (difference <= 0) {
                    return;
                }
            }

            ChatColor chatColor = event.isCritical() ? ChatColor.GOLD : ChatColor.GREEN;
            HologramUtil.createCombatHologram(Arrays.asList(caster, recipient), recipient.getEyeLocation(), chatColor + "+" + (int) difference + " ❤✦");

        } else {

            recipient.setHealth(newHP);
            ChatColor chatColor = event.isCritical() ? ChatColor.GOLD : ChatColor.GREEN;
            HologramUtil.createCombatHologram(Arrays.asList(caster, recipient), recipient.getEyeLocation(), chatColor + "+" + (int) amount + " ❤✦");
        }
        recipient.playSound(recipient.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.25f, 0.5f);
        recipient.getWorld().spawnParticle(Particle.HEART, recipient.getEyeLocation(), 3, 0.35F, 0.35F, 0.35F, 0);

        // Call a new health regen event
        Bukkit.getPluginManager().callEvent(new EntityRegainHealthEvent(recipient, amount, EntityRegainHealthEvent.RegainReason.CUSTOM));
    }

    @Override
    public boolean isCasting(Player player) {
        return SpellUseListener.getCasters().containsKey(player.getUniqueId());
    }

    @Override
    public boolean isOnCooldown(Player player, String spellName) {
        if (!this.cooldownMap.containsKey(player.getUniqueId()))
            return false;
        ConcurrentHashMap<Spell, Long> playerSpellsOnCooldown = this.cooldownMap.get(player.getUniqueId());
        return playerSpellsOnCooldown.keySet().stream().anyMatch(n -> n.getName().equalsIgnoreCase(spellName));
    }

    @Override
    public boolean isShielded(UUID uuid) {
        return this.shieldedPlayers.containsKey(uuid);
    }

    @Override
    public void reduceCooldown(Player player, Spell spell, double duration) {
        if (!this.cooldownMap.containsKey(player.getUniqueId())) return;
        ConcurrentHashMap<Spell, Long> playerSpellsOnCooldown = this.cooldownMap.get(player.getUniqueId());
        if (!playerSpellsOnCooldown.containsKey(spell)) return;
        long durationToReduce = (long) duration * 1000;
        playerSpellsOnCooldown.put(spell, playerSpellsOnCooldown.get(spell) - durationToReduce);
        this.cooldownMap.put(player.getUniqueId(), playerSpellsOnCooldown);
    }

    @Override
    public void reduceCooldown(Player player, String spell, double duration) {
        if (!this.cooldownMap.containsKey(player.getUniqueId())) return;
        ConcurrentHashMap<Spell, Long> playerSpellsOnCooldown = this.cooldownMap.get(player.getUniqueId());
        Optional<Spell> spellOptional = playerSpellsOnCooldown.keySet().stream().filter(key -> key.getName().equalsIgnoreCase(spell)).findAny();
        if (spellOptional.isEmpty()) return;
        long durationToReduce = (long) (duration * 1000);
        playerSpellsOnCooldown.put(spellOptional.get(), playerSpellsOnCooldown.get(spellOptional.get()) - durationToReduce);
        this.cooldownMap.put(player.getUniqueId(), playerSpellsOnCooldown);
    }

    @Override
    public void shieldPlayer(Player caster, Player recipient, double amount, Spell... spell) {
        // Call our custom shield event for interaction with buffs/de buffs
        SpellShieldEvent event = spell.length > 0
                ? new SpellShieldEvent((int) amount, recipient, caster, spell)
                : new SpellShieldEvent((int) amount, recipient, caster);
        Bukkit.getPluginManager().callEvent(event);
    }

    public Spell getSpellByName(String name) {
        Spell foundSpell = null;
        for (Spell spell : getSpells()) {
            if (spell.getName().equalsIgnoreCase(name)) {
                foundSpell = spell;
                break;
            }
        }
        return foundSpell;
    }

    public List<Spell> getSpells() {
        return this.spellList;
    }

    private int getUserCooldown(Player player, Spell spell) {
        double cooldownRemaining = 0;

        if (isOnCooldown(player, spell.getName())) {
            ConcurrentHashMap<Spell, Long> cd = this.cooldownMap.get(player.getUniqueId());
            if (cd.keySet().stream().anyMatch(n -> n.getName().equalsIgnoreCase(spell.getName()))) {
                cooldownRemaining = (cd.get(spell) + ((spell.getCooldown() + 1) * 1000)) - System.currentTimeMillis();
            }
        }
        return ((int) (cooldownRemaining / 1000));
    }

    /**
     * Add all spell classes to spell manager
     */
    private void registerSpells() {
        this.spellList.add(new Fireball());
        this.spellList.add(new Frostbolt());
        this.spellList.add(new Sprint());
        this.spellList.add(new Blink());
        this.spellList.add(new MeteorShower());
        this.spellList.add(new Accelerando());
        this.spellList.add(new Rejuvenate());
        this.spellList.add(new Judgment());
        this.spellList.add(new Backstab());
        this.spellList.add(new Barrage());
        this.spellList.add(new Slam());
        this.spellList.add(new Unseen());
        this.spellList.add(new RadiantNova());
        this.spellList.add(new Blizzard());
        this.spellList.add(new Frostbite());
        this.spellList.add(new AxeToss());
        this.spellList.add(new Lunge());
        this.spellList.add(new Harpoon());
        this.spellList.add(new Warsong());
        this.spellList.add(new Ironhide());
        this.spellList.add(new ThunderArrow());
        this.spellList.add(new Sear());
        this.spellList.add(new SacredSpring());
        this.spellList.add(new RadiantFire());
        this.spellList.add(new Agility());
        this.spellList.add(new Resolve());
        this.spellList.add(new Taunt());
        this.spellList.add(new Frostbite());
        this.spellList.add(new Cripple());
        this.spellList.add(new Challenger());
        this.spellList.add(new Kneebreak());
        this.spellList.add(new FireBlast());
        this.spellList.add(new Scald());
        this.spellList.add(new DragonsBreath());
        this.spellList.add(new ColdTouch());
        this.spellList.add(new Riftwalk());
        this.spellList.add(new SpectralBlade());
        this.spellList.add(new Manashield());
        this.spellList.add(new Cleave());
        this.spellList.add(new Whirlwind());
        this.spellList.add(new LastResort());
        this.spellList.add(new Bolster());
        this.spellList.add(new Rift());
        this.spellList.add(new DivineShield());
        this.spellList.add(new Encore());
        this.spellList.add(new Discord());
        this.spellList.add(new RayOfLight());
        this.spellList.add(new Lightwell());
        this.spellList.add(new Purify());
        this.spellList.add(new RighteousBlade());
        this.spellList.add(new Consecration());
        this.spellList.add(new Conflagration());
        this.spellList.add(new Remedy());
        this.spellList.add(new Ambush());
        this.spellList.add(new NetTrap());
        this.spellList.add(new SacredGrove());
        this.spellList.add(new GiftsOfTheGrove());
        this.spellList.add(new RefreshingVolley());
        this.spellList.add(new Fade());
        this.spellList.add(new Stormborn());
        this.spellList.add(new Charged());
        this.spellList.add(new Surge());
        this.spellList.add(new SnapFreeze());
        this.spellList.add(new WintersGrasp());
        this.spellList.add(new Despair());
        this.spellList.add(new TouchOfDeath());
        this.spellList.add(new Diminuendo());
        DefiledFont defiledFont = new DefiledFont();
        this.spellList.add(defiledFont);
        this.spellList.add(new CorruptedWaters(defiledFont));
        this.spellList.add(new UmbralGrasp());
        this.spellList.add(new Ruination());
        this.spellList.add(new ArcaneSlash());
        this.spellList.add(new TwinFangs());
        this.spellList.add(new Cocoon());
        this.spellList.add(new FromTheShadows());
        this.spellList.add(new Scurvy());
        this.spellList.add(new CallOfTheDeep());
        this.spellList.add(new Adrenaline());
        this.spellList.add(new Unstoppable());
        this.spellList.add(new Smite());
        this.spellList.add(new Consecrate());
        this.spellList.add(new Salvation());
        this.spellList.add(new BlessedBlade());
        this.spellList.add(new RainFire());
        this.spellList.add(new PiercingArrow());
        this.spellList.add(new Sunder());
        this.spellList.add(new LeapingShot());
        this.spellList.add(new SilverBolt());
        this.spellList.add(new Castigate());
        this.spellList.add(new Condemn());
        this.spellList.add(new WardingGlyph());
        this.spellList.add(new Hereticize());
        /*
        Items
         */
        this.spellList.add(new Potion());
        /*
        Artifacts
         */
        this.spellList.add(new DrainLife());
        this.spellList.add(new AdrenalineRush());
        this.spellList.add(new LifeInfusion());
        this.spellList.add(new BloodyShot());
        this.spellList.add(new Bloodlust());
        this.spellList.add(new Electrocute());
        this.spellList.add(new LightningArrow());
        this.spellList.add(new Maelstrom());
        this.spellList.add(new Thundershock());
        this.spellList.add(new ThunderousRift());
        this.spellList.add(new Cannonfire());
//        this.spellList.add(new ScorchedBlade()); sunken artifact passives
//        this.spellList.add(new BlessingOfFire());
//        this.spellList.add(new FlamingShield());
//        this.spellList.add(new BlazingRings());
//        this.spellList.add(new FirePulse());
    }

    private void removeCooldown(Player player, Spell spell) { // in case we forget to remove a removeCooldown method
        if (!this.cooldownMap.containsKey(player.getUniqueId())) return;
        ConcurrentHashMap<Spell, Long> playerSpellsOnCooldown = this.cooldownMap.get(player.getUniqueId());
        playerSpellsOnCooldown.remove(spell);
        this.cooldownMap.put(player.getUniqueId(), playerSpellsOnCooldown);
    }

    /**
     * Starts the repeating task to manage player cooldowns
     * Uses the action bar to display cooldowns
     */
    private void startCooldownTask() {
        new BukkitRunnable() {
            @Override
            public void run() {

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (cooldownMap.containsKey(player.getUniqueId())) {
                        ConcurrentHashMap<Spell, Long> spells = cooldownMap.get(player.getUniqueId());
                        if (spells.size() == 0) continue; // no active cooldowns
                        List<String> cdString = new ArrayList<>();

                        for (Spell spell : spells.keySet()) {
                            if (getUserCooldown(player, spell) <= 0)
                                removeCooldown(player, spell);
                            else
                                cdString.add(ChatColor.RED + "" + ChatColor.BOLD + spell.getName() + ChatColor.RED + ChatColor.BOLD + ": " + ChatColor.YELLOW + getUserCooldown(player, spell) + "s");
                        }

                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + String.join(ChatColor.YELLOW + " ", cdString)));
                    }
                }
            }
        }.runTaskTimerAsynchronously(this.plugin, 0, 5L); // every 0.25s
    }
}
