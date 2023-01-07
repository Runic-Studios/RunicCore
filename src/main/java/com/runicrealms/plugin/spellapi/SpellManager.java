package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.SpellAPI;
import com.runicrealms.plugin.events.*;
import com.runicrealms.plugin.model.PlayerSpellData;
import com.runicrealms.plugin.spellapi.spells.Consumable;
import com.runicrealms.plugin.spellapi.spells.Potion;
import com.runicrealms.plugin.spellapi.spells.archer.*;
import com.runicrealms.plugin.spellapi.spells.artifact.*;
import com.runicrealms.plugin.spellapi.spells.cleric.*;
import com.runicrealms.plugin.spellapi.spells.mage.*;
import com.runicrealms.plugin.spellapi.spells.rogue.*;
import com.runicrealms.plugin.spellapi.spells.warrior.*;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpellManager implements Listener, SpellAPI {

    private final List<Spell> spellList;
    private final ConcurrentHashMap<UUID, ConcurrentHashMap<Spell, Long>> cooldownMap;
    private final HashMap<UUID, BukkitTask> invulnerableEntities;
    private final HashMap<UUID, BukkitTask> rootedEntities;
    private final HashMap<UUID, BukkitTask> silencedEntities;
    private final HashMap<UUID, BukkitTask> stunnedEntities;
    private final RunicCore plugin = RunicCore.getInstance();

    public SpellManager() {
        this.spellList = new ArrayList<>();
        this.cooldownMap = new ConcurrentHashMap<>();
        this.invulnerableEntities = new HashMap<>();
        this.rootedEntities = new HashMap<>();
        this.silencedEntities = new HashMap<>();
        this.stunnedEntities = new HashMap<>();
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
    public void addStatusEffect(Entity entity, RunicStatusEffect runicStatusEffect, double durationInSecs) {
        if (runicStatusEffect == RunicStatusEffect.SILENCE) {
            entity.sendMessage(ChatColor.RED + "You have been " + ChatColor.DARK_RED + ChatColor.BOLD + "silenced!");
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_CHICKEN_DEATH, 0.5f, 1.0f);
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    silencedEntities.remove(entity.getUniqueId());
                }
            }.runTaskLaterAsynchronously(plugin, (long) (durationInSecs * 20L));
            silencedEntities.put(entity.getUniqueId(), task);
        } else if (runicStatusEffect == RunicStatusEffect.STUN) {
            entity.sendMessage(ChatColor.RED + "You have been " + ChatColor.DARK_RED + ChatColor.BOLD + "stunned!");
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    stunnedEntities.remove(entity.getUniqueId());
                }
            }.runTaskLaterAsynchronously(plugin, (long) (durationInSecs * 20L));
            stunnedEntities.put(entity.getUniqueId(), task);
            if (!(entity instanceof Player)) { // since there's no entity move event, we do it the old-fashioned way for mobs
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (durationInSecs * 20), 3));
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (int) (durationInSecs * 20), 127));
            }
        } else if (runicStatusEffect == RunicStatusEffect.ROOT) {
            entity.sendMessage(ChatColor.RED + "You have been " + ChatColor.DARK_RED + ChatColor.BOLD + "rooted!");
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 1.0f);
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    rootedEntities.remove(entity.getUniqueId());
                }
            }.runTaskLaterAsynchronously(plugin, (long) (durationInSecs * 20L));
            rootedEntities.put(entity.getUniqueId(), task);
            if (!(entity instanceof Player)) { // since there's no entity move event, we do it the old-fashioned way for mobs
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (durationInSecs * 20), 3));
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (int) (durationInSecs * 20), 127));
            }
        } else if (runicStatusEffect == RunicStatusEffect.INVULNERABILITY) {
            entity.sendMessage(ChatColor.GREEN + "You are now " + ChatColor.DARK_GREEN + ChatColor.BOLD + "invulnerable!");
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 0.1f);
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    invulnerableEntities.remove(entity.getUniqueId());
                }
            }.runTaskLaterAsynchronously(plugin, (long) (durationInSecs * 20L));
            invulnerableEntities.put(entity.getUniqueId(), task);
        }
    }

    @Override
    public Spell getPlayerSpell(Player player, int number) {
        Spell spellToCast = null;
        UUID uuid = player.getUniqueId();
        try {
            PlayerSpellData playerSpellData = RunicCore.getSkillTreeAPI().getPlayerSpellMap().get(uuid);
            switch (number) {
                case 1:
                    spellToCast = this.getSpellByName(playerSpellData.getSpellHotbarOne());
                    if (playerSpellData.getSpellHotbarOne().equals("")) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                        player.sendMessage(ChatColor.RED + "You have no spell set in this slot!");
                    }
                    break;
                case 2:
                    spellToCast = this.getSpellByName(playerSpellData.getSpellLeftClick());
                    if (playerSpellData.getSpellLeftClick().equals("")) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                        player.sendMessage(ChatColor.RED + "You have no spell set in this slot!");
                    }
                    break;
                case 3:
                    spellToCast = this.getSpellByName(playerSpellData.getSpellRightClick());
                    if (playerSpellData.getSpellRightClick().equals("")) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                        player.sendMessage(ChatColor.RED + "You have no spell set in this slot!");
                    }
                    break;
                case 4:
                    spellToCast = this.getSpellByName(playerSpellData.getSpellSwapHands());
                    if (playerSpellData.getSpellSwapHands().equals("")) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                        player.sendMessage(ChatColor.RED + "You have no spell set in this slot!");
                    }
                    break;
            }
        } catch (NullPointerException e) {
            // haha sky is lazy
        }
        return spellToCast;
    }

    @Override
    public Spell getSpell(String name) {
        return this.getSpellByName(name);
    }

    @Override
    public boolean isCasting(Player player) {
        return SpellUseListener.getCasters().containsKey(player.getUniqueId());
    }

    @Override
    public boolean isInvulnerable(Entity entity) {
        return invulnerableEntities.containsKey(entity.getUniqueId());
    }

    @Override
    public boolean isOnCooldown(Player player, String spellName) {
        if (!this.cooldownMap.containsKey(player.getUniqueId()))
            return false;
        ConcurrentHashMap<Spell, Long> playerSpellsOnCooldown = this.cooldownMap.get(player.getUniqueId());
        return playerSpellsOnCooldown.keySet().stream().anyMatch(n -> n.getName().equalsIgnoreCase(spellName));
    }

    @Override
    public boolean isRooted(Entity entity) {
        return rootedEntities.containsKey(entity.getUniqueId());
    }

    @Override
    public boolean isSilenced(Entity entity) {
        return silencedEntities.containsKey(entity.getUniqueId());
    }

    @Override
    public boolean isStunned(Entity entity) {
        return stunnedEntities.containsKey(entity.getUniqueId());
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
        if (!spellOptional.isPresent()) return;
        long durationToReduce = (long) (duration * 1000);
        playerSpellsOnCooldown.put(spellOptional.get(), playerSpellsOnCooldown.get(spellOptional.get()) - durationToReduce);
        this.cooldownMap.put(player.getUniqueId(), playerSpellsOnCooldown);
    }

    @Override
    public boolean removeStatusEffect(UUID uuid, RunicStatusEffect statusEffect) {
        if (statusEffect == RunicStatusEffect.INVULNERABILITY) {
            if (!invulnerableEntities.containsKey(uuid)) return false;
            invulnerableEntities.get(uuid).cancel(); // cancel the async removal task
            invulnerableEntities.remove(uuid);
            return true;
        } else if (statusEffect == RunicStatusEffect.ROOT) {
            if (!rootedEntities.containsKey(uuid)) return false;
            rootedEntities.get(uuid).cancel(); // cancel the async removal task
            rootedEntities.remove(uuid);
            return true;
        } else if (statusEffect == RunicStatusEffect.SILENCE) {
            if (!silencedEntities.containsKey(uuid)) return false;
            silencedEntities.get(uuid).cancel(); // cancel the async removal task
            silencedEntities.remove(uuid);
            return true;
        } else if (statusEffect == RunicStatusEffect.STUN) {
            if (!stunnedEntities.containsKey(uuid)) return false;
            stunnedEntities.get(uuid).cancel(); // cancel the async removal task
            stunnedEntities.remove(uuid);
            return true;
        }
        return false;
    }

    public HashMap<UUID, BukkitTask> getInvulnerableEntities() {
        return invulnerableEntities;
    }

    public HashMap<UUID, BukkitTask> getRootedEntities() {
        return rootedEntities;
    }

    public HashMap<UUID, BukkitTask> getSilencedEntities() {
        return silencedEntities;
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

    @EventHandler
    public void onMobDamage(MobDamageEvent event) {
        if (invulnerableEntities.isEmpty() && silencedEntities.isEmpty() && stunnedEntities.isEmpty()) return;
        if (silencedEntities.containsKey(event.getDamager().getUniqueId())
                || stunnedEntities.containsKey(event.getDamager().getUniqueId())
                || invulnerableEntities.containsKey(event.getVictim().getUniqueId()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (rootedEntities.isEmpty() && stunnedEntities.isEmpty()) return;
        if (!(rootedEntities.containsKey(event.getPlayer().getUniqueId())
                || stunnedEntities.containsKey(event.getPlayer().getUniqueId()))) return;
        if (event.getTo() == null) return;
        Location to = event.getFrom();
        to.setPitch(event.getTo().getPitch());
        to.setYaw(event.getTo().getYaw());
        event.setTo(to);
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (invulnerableEntities.isEmpty() && silencedEntities.isEmpty() && stunnedEntities.isEmpty()) return;
        if (silencedEntities.containsKey(event.getPlayer().getUniqueId())
                || stunnedEntities.containsKey(event.getPlayer().getUniqueId())
                || invulnerableEntities.containsKey(event.getVictim().getUniqueId()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent event) {
        if (silencedEntities.isEmpty() && stunnedEntities.isEmpty()) return;
        if (silencedEntities.containsKey(event.getCaster().getUniqueId())
                || stunnedEntities.containsKey(event.getCaster().getUniqueId()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onSpellDamage(MagicDamageEvent event) {
        if (invulnerableEntities.isEmpty() && silencedEntities.isEmpty() && stunnedEntities.isEmpty()) return;
        if (silencedEntities.containsKey(event.getPlayer().getUniqueId())
                || stunnedEntities.containsKey(event.getPlayer().getUniqueId())
                || invulnerableEntities.containsKey(event.getVictim().getUniqueId()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onSpellHeal(SpellHealEvent event) {
        if (silencedEntities.isEmpty() && stunnedEntities.isEmpty()) return;
        if (silencedEntities.containsKey(event.getPlayer().getUniqueId())
                || stunnedEntities.containsKey(event.getPlayer().getUniqueId()))
            event.setCancelled(true);
    }

    /**
     * Add all spell classes to spell manager
     */
    private void registerSpells() {
        this.spellList.add(new Fireball());
        this.spellList.add(new Frostbolt());
        this.spellList.add(new Sprint());
        this.spellList.add(new Disengage());
        this.spellList.add(new Blink());
        this.spellList.add(new MeteorShower());
        this.spellList.add(new Windstride());
        this.spellList.add(new Rejuvenate());
        this.spellList.add(new Enrage());
        this.spellList.add(new Judgment());
        this.spellList.add(new SmokeBomb());
        this.spellList.add(new Backstab());
        this.spellList.add(new Barrage());
        this.spellList.add(new Grapple());
        this.spellList.add(new Slam());
        this.spellList.add(new Cloak());
        this.spellList.add(new HolyNova());
        this.spellList.add(new Blizzard());
        this.spellList.add(new Frostbite());
        this.spellList.add(new ThrowAxe());
        this.spellList.add(new ArcaneOrb());
        this.spellList.add(new Lunge());
        this.spellList.add(new Harpoon());
        this.spellList.add(new Spellsong());
        this.spellList.add(new Ironhide());
        this.spellList.add(new RunicArrow());
        this.spellList.add(new Smite());
        this.spellList.add(new ArcaneBomb());
        this.spellList.add(new CureScurvy());
        this.spellList.add(new HolyWater());
        this.spellList.add(new Seasick());
        this.spellList.add(new Hawkeye());
        this.spellList.add(new Manawell());
        this.spellList.add(new SeaLegs());
        this.spellList.add(new Predator());
        this.spellList.add(new Resolve());
        this.spellList.add(new Taunt());
        this.spellList.add(new Leech());
        this.spellList.add(new Frostbite());
        this.spellList.add(new SliceAndDice());
        this.spellList.add(new Cripple());
        this.spellList.add(new Challenger());
        this.spellList.add(new Riposte());
        this.spellList.add(new Kneebreak());
        this.spellList.add(new FireBlast());
        this.spellList.add(new Scald());
        this.spellList.add(new FireAura());
        this.spellList.add(new IceBlock());
        this.spellList.add(new ColdTouch());
        this.spellList.add(new IcyAffinity());
        this.spellList.add(new Foresight());
        this.spellList.add(new Cleave());
        this.spellList.add(new Whirlwind());
        this.spellList.add(new LastResort());
        this.spellList.add(new Bolster());
        this.spellList.add(new Condemn());
        this.spellList.add(new Subdue());
        this.spellList.add(new Rebuke());
        this.spellList.add(new Rift());
        this.spellList.add(new SoulLink());
        this.spellList.add(new DivineShield());
        this.spellList.add(new Improvisation());
        this.spellList.add(new Discord());
        this.spellList.add(new RayOfLight());
        this.spellList.add(new Dissonance());
        this.spellList.add(new Absolution());
        this.spellList.add(new Purify());
        this.spellList.add(new RighteousBlade());
        this.spellList.add(new Consecration());
        this.spellList.add(new WingClip());
        this.spellList.add(new Sentry());
        this.spellList.add(new EscapeArtist());
        this.spellList.add(new NetShot());
        this.spellList.add(new SurvivalInstinct());
        this.spellList.add(new Headshot());
        this.spellList.add(new HoningShot());
        this.spellList.add(new Conflagration());
        this.spellList.add(new Shatter());
        this.spellList.add(new TippedArrows());
        this.spellList.add(new Remedy());
        this.spellList.add(new Ambush());
        this.spellList.add(new NetTrap());
        this.spellList.add(new WildGrowth());
        this.spellList.add(new GiftsOfTheWoad());
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
        this.spellList.add(new Consumable());
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
                                cdString.add(ChatColor.DARK_RED + spell.getName() + ChatColor.DARK_RED + ": " + ChatColor.YELLOW + getUserCooldown(player, spell) + "s");
                        }

                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.DARK_RED + String.join(ChatColor.YELLOW + " ", cdString)));
                    }
                }
            }
        }.runTaskTimerAsynchronously(this.plugin, 0, 5L); // every 0.25s
    }
}
