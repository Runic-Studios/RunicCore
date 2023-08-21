package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.SpellAPI;
import com.runicrealms.plugin.api.event.SpellShieldEvent;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.model.SpellData;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.spellapi.spells.Combat;
import com.runicrealms.plugin.spellapi.spells.Potion;
import com.runicrealms.plugin.spellapi.spells.archer.Ambush;
import com.runicrealms.plugin.spellapi.spells.archer.Barrage;
import com.runicrealms.plugin.spellapi.spells.archer.Fade;
import com.runicrealms.plugin.spellapi.spells.archer.GiftsOfTheGrove;
import com.runicrealms.plugin.spellapi.spells.archer.Jolt;
import com.runicrealms.plugin.spellapi.spells.archer.LeapingShot;
import com.runicrealms.plugin.spellapi.spells.archer.Overcharge;
import com.runicrealms.plugin.spellapi.spells.archer.PiercingArrow;
import com.runicrealms.plugin.spellapi.spells.archer.RainFire;
import com.runicrealms.plugin.spellapi.spells.archer.RefreshingVolley;
import com.runicrealms.plugin.spellapi.spells.archer.Remedy;
import com.runicrealms.plugin.spellapi.spells.archer.SacredGrove;
import com.runicrealms.plugin.spellapi.spells.archer.SnareTrap;
import com.runicrealms.plugin.spellapi.spells.archer.Stormborn;
import com.runicrealms.plugin.spellapi.spells.archer.Sunder;
import com.runicrealms.plugin.spellapi.spells.archer.Surge;
import com.runicrealms.plugin.spellapi.spells.archer.ThunderArrow;
import com.runicrealms.plugin.spellapi.spells.cleric.Accelerando;
import com.runicrealms.plugin.spellapi.spells.cleric.AstralBlessing;
import com.runicrealms.plugin.spellapi.spells.cleric.Consecration;
import com.runicrealms.plugin.spellapi.spells.cleric.CosmicPrism;
import com.runicrealms.plugin.spellapi.spells.cleric.Diminuendo;
import com.runicrealms.plugin.spellapi.spells.cleric.Discord;
import com.runicrealms.plugin.spellapi.spells.cleric.DivineShield;
import com.runicrealms.plugin.spellapi.spells.cleric.Encore;
import com.runicrealms.plugin.spellapi.spells.cleric.Lightwell;
import com.runicrealms.plugin.spellapi.spells.cleric.Nightfall;
import com.runicrealms.plugin.spellapi.spells.cleric.Purify;
import com.runicrealms.plugin.spellapi.spells.cleric.RadiantFire;
import com.runicrealms.plugin.spellapi.spells.cleric.RadiantNova;
import com.runicrealms.plugin.spellapi.spells.cleric.RayOfLight;
import com.runicrealms.plugin.spellapi.spells.cleric.Rejuvenate;
import com.runicrealms.plugin.spellapi.spells.cleric.SacredSpring;
import com.runicrealms.plugin.spellapi.spells.cleric.Sear;
import com.runicrealms.plugin.spellapi.spells.cleric.Starlight;
import com.runicrealms.plugin.spellapi.spells.cleric.TwilightResurgence;
import com.runicrealms.plugin.spellapi.spells.cleric.Warsong;
import com.runicrealms.plugin.spellapi.spells.mage.ArcaneSlash;
import com.runicrealms.plugin.spellapi.spells.mage.Blink;
import com.runicrealms.plugin.spellapi.spells.mage.Blizzard;
import com.runicrealms.plugin.spellapi.spells.mage.CinderedTouch;
import com.runicrealms.plugin.spellapi.spells.mage.DragonsBreath;
import com.runicrealms.plugin.spellapi.spells.mage.Erupt;
import com.runicrealms.plugin.spellapi.spells.mage.Fireball;
import com.runicrealms.plugin.spellapi.spells.mage.Frostbite;
import com.runicrealms.plugin.spellapi.spells.mage.Inferno;
import com.runicrealms.plugin.spellapi.spells.mage.Manashield;
import com.runicrealms.plugin.spellapi.spells.mage.Meteor;
import com.runicrealms.plugin.spellapi.spells.mage.Riftwalk;
import com.runicrealms.plugin.spellapi.spells.mage.Shatter;
import com.runicrealms.plugin.spellapi.spells.mage.SnapFreeze;
import com.runicrealms.plugin.spellapi.spells.mage.SpectralBlade;
import com.runicrealms.plugin.spellapi.spells.mage.WintersGrasp;
import com.runicrealms.plugin.spellapi.spells.rogue.Agility;
import com.runicrealms.plugin.spellapi.spells.rogue.Backstab;
import com.runicrealms.plugin.spellapi.spells.rogue.CallOfTheDeep;
import com.runicrealms.plugin.spellapi.spells.rogue.Castigate;
import com.runicrealms.plugin.spellapi.spells.rogue.Challenger;
import com.runicrealms.plugin.spellapi.spells.rogue.Cocoon;
import com.runicrealms.plugin.spellapi.spells.rogue.Cripple;
import com.runicrealms.plugin.spellapi.spells.rogue.Dash;
import com.runicrealms.plugin.spellapi.spells.rogue.Flay;
import com.runicrealms.plugin.spellapi.spells.rogue.FromTheShadows;
import com.runicrealms.plugin.spellapi.spells.rogue.Harpoon;
import com.runicrealms.plugin.spellapi.spells.rogue.Hereticize;
import com.runicrealms.plugin.spellapi.spells.rogue.Kneebreak;
import com.runicrealms.plugin.spellapi.spells.rogue.Lunge;
import com.runicrealms.plugin.spellapi.spells.rogue.Scurvy;
import com.runicrealms.plugin.spellapi.spells.rogue.SilverBolt;
import com.runicrealms.plugin.spellapi.spells.rogue.TwinFangs;
import com.runicrealms.plugin.spellapi.spells.rogue.Unseen;
import com.runicrealms.plugin.spellapi.spells.rogue.WardingGlyph;
import com.runicrealms.plugin.spellapi.spells.warrior.Adrenaline;
import com.runicrealms.plugin.spellapi.spells.warrior.AxeToss;
import com.runicrealms.plugin.spellapi.spells.warrior.BlessedBlade;
import com.runicrealms.plugin.spellapi.spells.warrior.Bloodbath;
import com.runicrealms.plugin.spellapi.spells.warrior.Cleave;
import com.runicrealms.plugin.spellapi.spells.warrior.Consecrate;
import com.runicrealms.plugin.spellapi.spells.warrior.Damnation;
import com.runicrealms.plugin.spellapi.spells.warrior.Devour;
import com.runicrealms.plugin.spellapi.spells.warrior.Judgment;
import com.runicrealms.plugin.spellapi.spells.warrior.Ruination;
import com.runicrealms.plugin.spellapi.spells.warrior.Rupture;
import com.runicrealms.plugin.spellapi.spells.warrior.Salvation;
import com.runicrealms.plugin.spellapi.spells.warrior.Slam;
import com.runicrealms.plugin.spellapi.spells.warrior.Smite;
import com.runicrealms.plugin.spellapi.spells.warrior.SoulReaper;
import com.runicrealms.plugin.spellapi.spells.warrior.Taunt;
import com.runicrealms.plugin.spellapi.spells.warrior.UmbralGrasp;
import com.runicrealms.plugin.spellapi.spelltypes.ShieldPayload;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpellManager implements Listener, SpellAPI {
    private final List<Spell> spellList;
    private final ConcurrentHashMap<UUID, ConcurrentHashMap<Spell, Long>> cooldownMap;
    private final HashMap<UUID, ShieldPayload> shieldedPlayers;
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
    public void addCooldown(@NotNull Player player, @NotNull Spell spell, double cooldownTime) {
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
    public Spell getPlayerSpell(@NotNull Player player, int number) {
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
    public HashMap<UUID, ShieldPayload> getShieldedPlayers() {
        return this.shieldedPlayers;
    }

    @Override
    public Spell getSpell(@NotNull String name) {
        return this.getSpellByName(name);
    }

    @Override
    public ConcurrentHashMap.KeySetView<Spell, Long> getSpellsOnCooldown(@NotNull UUID uuid) {
        if (cooldownMap.containsKey(uuid)) {
            return cooldownMap.get(uuid).keySet();
        } else {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void healPlayer(@NotNull Player caster, @NotNull Player recipient, double amount, @Nullable Spell spell) {
        // Call our custom heal event for interaction with buffs/de buffs
        SpellHealEvent event = new SpellHealEvent((int) amount, recipient, caster, spell);
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
    public boolean isCasting(@NotNull Player player) {
        return SpellUseListener.getCasters().containsKey(player.getUniqueId());
    }

    @Override
    public boolean isOnCooldown(@NotNull Player player, @NotNull String spellName) {
        if (!this.cooldownMap.containsKey(player.getUniqueId()))
            return false;
        ConcurrentHashMap<Spell, Long> playerSpellsOnCooldown = this.cooldownMap.get(player.getUniqueId());
        return playerSpellsOnCooldown.keySet().stream().anyMatch(n -> n.getName().equalsIgnoreCase(spellName));
    }

    @Override
    public boolean isShielded(@NotNull UUID uuid) {
        return this.shieldedPlayers.containsKey(uuid);
    }

    @Override
    public void reduceCooldown(@NotNull Player player, @NotNull Spell spell, double duration) {
        if (!this.cooldownMap.containsKey(player.getUniqueId())) return;
        ConcurrentHashMap<Spell, Long> playerSpellsOnCooldown = this.cooldownMap.get(player.getUniqueId());
        if (!playerSpellsOnCooldown.containsKey(spell)) return;
        long durationToReduce = (long) duration * 1000;
        playerSpellsOnCooldown.put(spell, playerSpellsOnCooldown.get(spell) - durationToReduce);
        this.cooldownMap.put(player.getUniqueId(), playerSpellsOnCooldown);
    }

    @Override
    public void increaseCooldown(@NotNull Player player, @NotNull Spell spell, double duration) {
        // Ensure duration isn't negative
        if (duration < 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        if (!this.cooldownMap.containsKey(player.getUniqueId())) return;
        ConcurrentHashMap<Spell, Long> playerSpellsOnCooldown = this.cooldownMap.get(player.getUniqueId());
        if (!playerSpellsOnCooldown.containsKey(spell)) return;
        long durationToAdd = (long) duration * 1000;
        playerSpellsOnCooldown.put(spell, playerSpellsOnCooldown.get(spell) + durationToAdd);
        this.cooldownMap.put(player.getUniqueId(), playerSpellsOnCooldown);
    }

    @Override
    public void increaseCooldown(@NotNull Player player, @NotNull String spell, double duration) {
        // Ensure duration isn't negative
        if (duration < 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        if (!this.cooldownMap.containsKey(player.getUniqueId())) return;
        ConcurrentHashMap<Spell, Long> playerSpellsOnCooldown = this.cooldownMap.get(player.getUniqueId());
        Optional<Spell> spellOptional = playerSpellsOnCooldown.keySet().stream().filter(key -> key.getName().equalsIgnoreCase(spell)).findAny();
        if (spellOptional.isEmpty()) return;
        long durationToAdd = (long) (duration * 1000);
        playerSpellsOnCooldown.put(spellOptional.get(), playerSpellsOnCooldown.get(spellOptional.get()) + durationToAdd);
        this.cooldownMap.put(player.getUniqueId(), playerSpellsOnCooldown);
    }

    @Override
    public void reduceCooldown(@NotNull Player player, @NotNull String spell, double duration) {
        if (!this.cooldownMap.containsKey(player.getUniqueId())) return;
        ConcurrentHashMap<Spell, Long> playerSpellsOnCooldown = this.cooldownMap.get(player.getUniqueId());
        Optional<Spell> spellOptional = playerSpellsOnCooldown.keySet().stream().filter(key -> key.getName().equalsIgnoreCase(spell)).findAny();
        if (spellOptional.isEmpty()) return;
        long durationToReduce = (long) (duration * 1000);
        playerSpellsOnCooldown.put(spellOptional.get(), playerSpellsOnCooldown.get(spellOptional.get()) - durationToReduce);
        this.cooldownMap.put(player.getUniqueId(), playerSpellsOnCooldown);
    }

    @Override
    public void setCooldown(@NotNull Player player, @NotNull Spell spell, double duration) {
        if (!this.cooldownMap.containsKey(player.getUniqueId())) return;
        ConcurrentHashMap<Spell, Long> playerSpellsOnCooldown = this.cooldownMap.get(player.getUniqueId());
        if (!playerSpellsOnCooldown.containsKey(spell)) return;
        long durationToSet = (long) duration * 1000;
        playerSpellsOnCooldown.put(spell, durationToSet);
        this.cooldownMap.put(player.getUniqueId(), playerSpellsOnCooldown);
    }

    @Override
    public void setCooldown(@NotNull Player player, @NotNull String spell, double duration) {
        if (!this.cooldownMap.containsKey(player.getUniqueId())) return;
        ConcurrentHashMap<Spell, Long> playerSpellsOnCooldown = this.cooldownMap.get(player.getUniqueId());
        Optional<Spell> spellOptional = playerSpellsOnCooldown.keySet().stream().filter(key -> key.getName().equalsIgnoreCase(spell)).findAny();
        if (spellOptional.isEmpty()) return;
        long durationToSet = (long) (duration * 1000);
        playerSpellsOnCooldown.put(spellOptional.get(), durationToSet);
        this.cooldownMap.put(player.getUniqueId(), playerSpellsOnCooldown);
    }

    @Override
    public void shieldPlayer(@NotNull Player caster, @NotNull Player recipient, double amount, @Nullable Spell spell) {
        // Call our custom shield event for interaction with buffs/de buffs
        Bukkit.getPluginManager().callEvent(new SpellShieldEvent((int) amount, recipient, caster, spell));
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

    @Override
    public double getUserCooldown(@NotNull Player player, @NotNull Spell spell) {
        double cooldownRemaining = 0;

        if (isOnCooldown(player, spell.getName())) {
            ConcurrentHashMap<Spell, Long> cd = this.cooldownMap.get(player.getUniqueId());
            if (cd.keySet().stream().anyMatch(n -> n.getName().equalsIgnoreCase(spell.getName()))) {
                cooldownRemaining = (cd.get(spell) + (spell.getCooldown() * 1000)) - System.currentTimeMillis();
            }
        }

        return cooldownRemaining / 1000;
    }

    /**
     * Add all spell classes to spell manager
     */
    private void registerSpells() {
        this.spellList.add(new Fireball());
        this.spellList.add(new Blink());
        this.spellList.add(new Meteor());
        this.spellList.add(new Accelerando());
        this.spellList.add(new Rejuvenate());
        this.spellList.add(new Judgment());
        this.spellList.add(new Backstab());
        this.spellList.add(new Barrage());
        this.spellList.add(new Slam());
        this.spellList.add(new Unseen());
        this.spellList.add(new RadiantNova());
        this.spellList.add(new Blizzard());
        this.spellList.add(new AxeToss());
        this.spellList.add(new Lunge());
        this.spellList.add(new Harpoon());
        this.spellList.add(new Warsong());
        this.spellList.add(new ThunderArrow());
        this.spellList.add(new Sear());
        this.spellList.add(new SacredSpring());
        this.spellList.add(new RadiantFire());
        this.spellList.add(new Agility());
        this.spellList.add(new Taunt());
        this.spellList.add(new Frostbite());
        this.spellList.add(new Cripple());
        this.spellList.add(new Challenger());
        this.spellList.add(new Kneebreak());
        this.spellList.add(new DragonsBreath());
        this.spellList.add(new Riftwalk());
        this.spellList.add(new SpectralBlade());
        this.spellList.add(new Manashield());
        this.spellList.add(new Rupture());
        this.spellList.add(new DivineShield());
        this.spellList.add(new Encore());
        this.spellList.add(new Discord());
        this.spellList.add(new RayOfLight());
        this.spellList.add(new Lightwell());
        this.spellList.add(new Purify());
        this.spellList.add(new Consecration());
        this.spellList.add(new Inferno());
        this.spellList.add(new Remedy());
        this.spellList.add(new Ambush());
        this.spellList.add(new SnareTrap());
        this.spellList.add(new SacredGrove());
        this.spellList.add(new GiftsOfTheGrove());
        this.spellList.add(new RefreshingVolley());
        this.spellList.add(new Fade());
        this.spellList.add(new Stormborn());
        this.spellList.add(new Surge());
        this.spellList.add(new SnapFreeze());
        this.spellList.add(new WintersGrasp());
        this.spellList.add(new Diminuendo());
        this.spellList.add(new ArcaneSlash());
        this.spellList.add(new TwinFangs());
        this.spellList.add(new Cocoon());
        this.spellList.add(new FromTheShadows());
        this.spellList.add(new Scurvy());
        this.spellList.add(new CallOfTheDeep());
        this.spellList.add(new Dash());
        this.spellList.add(new Adrenaline());
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
        this.spellList.add(new Flay());
        this.spellList.add(new WardingGlyph());
        this.spellList.add(new Hereticize());
        this.spellList.add(new Starlight());
        this.spellList.add(new AstralBlessing());
        this.spellList.add(new CosmicPrism());
        this.spellList.add(new Nightfall());
        this.spellList.add(new TwilightResurgence());
        this.spellList.add(new Devour());
        this.spellList.add(new SoulReaper());
        this.spellList.add(new UmbralGrasp());
        this.spellList.add(new Damnation());
        this.spellList.add(new Ruination());
        this.spellList.add(new Frostbite());
        this.spellList.add(new Shatter());
        this.spellList.add(new Jolt());
        this.spellList.add(new Overcharge());
        this.spellList.add(new Cleave());
        this.spellList.add(new Bloodbath());
        this.spellList.add(new CinderedTouch());
        this.spellList.add(new Erupt());

        /*
        Items
         */
        this.spellList.add(new Potion());
        this.spellList.add(new Combat());
        /*
        Artifacts
         */
        //this.spellList.add(new DrainLife());
        //this.spellList.add(new AdrenalineRush());
        //this.spellList.add(new LifeInfusion());
        //this.spellList.add(new BloodyShot());
        //this.spellList.add(new Bloodlust());
        //this.spellList.add(new Electrocute());
        //this.spellList.add(new LightningArrow());
        //this.spellList.add(new Maelstrom());
        //this.spellList.add(new Thundershock());
        //this.spellList.add(new ThunderousRift());
        //this.spellList.add(new Cannonfire());
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
                            if (getUserCooldown(player, spell) <= 0) {
                                removeCooldown(player, spell);
                            } else {
                                double cooldown = getUserCooldown(player, spell);
                                String formattedCooldown = String.format("%.1f", cooldown);
                                cdString.add(ChatColor.RED + String.valueOf(ChatColor.BOLD) + spell.getName() + ChatColor.RED + ChatColor.BOLD + ": " + ChatColor.YELLOW + formattedCooldown + "s");
                            }
                        }

                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + String.join(ChatColor.YELLOW + " ", cdString)));
                    }
                }
            }
        }.runTaskTimerAsynchronously(this.plugin, 0, 5L); // every 0.25s
    }

}
