package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.*;
import com.runicrealms.plugin.spellapi.spells.Potion;
import com.runicrealms.plugin.spellapi.spells.archer.*;
import com.runicrealms.plugin.spellapi.spells.artifact.*;
import com.runicrealms.plugin.spellapi.spells.cleric.*;
import com.runicrealms.plugin.spellapi.spells.mage.*;
import com.runicrealms.plugin.spellapi.spells.rogue.*;
import com.runicrealms.plugin.spellapi.spells.warrior.*;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpellManager implements Listener {

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

    /**
     * Adds spell to player, spell cooldown map
     *
     * @param player       to add cooldown to
     * @param spell        to apply cooldown to
     * @param cooldownTime of spell
     */
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

        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin,
                () -> removeCooldown(player, spell), (long) cooldownTime * 20);

    }

    public HashMap<UUID, BukkitTask> getInvulnerableEntities() {
        return invulnerableEntities;
    }

    public HashMap<UUID, BukkitTask> getRootedEntites() {
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

    public HashMap<UUID, BukkitTask> getStunnedEntities() {
        return stunnedEntities;
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

    public boolean isOnCooldown(Player player, String spellName) {
        if (!this.cooldownMap.containsKey(player.getUniqueId()))
            return false;
        ConcurrentHashMap<Spell, Long> playerSpellsOnCooldown = this.cooldownMap.get(player.getUniqueId());
        return playerSpellsOnCooldown.keySet().stream().anyMatch(n -> n.getName().equalsIgnoreCase(spellName));
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent e) {
        if (invulnerableEntities.isEmpty() && silencedEntities.isEmpty() && stunnedEntities.isEmpty()) return;
        if (silencedEntities.containsKey(e.getDamager().getUniqueId())
                || stunnedEntities.containsKey(e.getDamager().getUniqueId())
                || invulnerableEntities.containsKey(e.getVictim().getUniqueId()))
            e.setCancelled(true);
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
    public void onPhysicalDamage(PhysicalDamageEvent e) {
        if (invulnerableEntities.isEmpty() && silencedEntities.isEmpty() && stunnedEntities.isEmpty()) return;
        if (silencedEntities.containsKey(e.getPlayer().getUniqueId())
                || stunnedEntities.containsKey(e.getPlayer().getUniqueId())
                || invulnerableEntities.containsKey(e.getVictim().getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent e) {
        if (silencedEntities.isEmpty()) return;
        if (silencedEntities.containsKey(e.getCaster().getUniqueId())
                || stunnedEntities.containsKey(e.getCaster().getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onSpellDamage(MagicDamageEvent e) {
        if (invulnerableEntities.isEmpty() && silencedEntities.isEmpty() && stunnedEntities.isEmpty()) return;
        if (silencedEntities.containsKey(e.getPlayer().getUniqueId())
                || stunnedEntities.containsKey(e.getPlayer().getUniqueId())
                || invulnerableEntities.containsKey(e.getVictim().getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onSpellHeal(SpellHealEvent e) {
        if (silencedEntities.isEmpty() && stunnedEntities.isEmpty()) return;
        if (silencedEntities.containsKey(e.getPlayer().getUniqueId())
                || stunnedEntities.containsKey(e.getPlayer().getUniqueId()))
            e.setCancelled(true);
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
        this.spellList.add(new Reflect());
        this.spellList.add(new PowerShot());
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
        this.spellList.add(new KillShot());
        this.spellList.add(new BearTrap());
        this.spellList.add(new WingClip());
        this.spellList.add(new Flare());
        this.spellList.add(new Sentry());
        this.spellList.add(new EscapeArtist());
        this.spellList.add(new ArcaneShot());
        this.spellList.add(new Icebrand());
        this.spellList.add(new IceVolley());
        this.spellList.add(new BindingShot());
        this.spellList.add(new SurvivalInstinct());
        this.spellList.add(new Headshot());
        this.spellList.add(new HoningShot());
        this.spellList.add(new Conflagration());
        this.spellList.add(new Shatter());
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
                                removeCooldown(player, spell); // insurance
                            else
                                cdString.add(ChatColor.DARK_RED + spell.getName() + ChatColor.DARK_RED + ": " + ChatColor.YELLOW + getUserCooldown(player, spell) + "s");
                        }

                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.DARK_RED + String.join(ChatColor.YELLOW + " ", cdString)));
                    }
                }
            }
        }.runTaskTimerAsynchronously(this.plugin, 0, 10L);
    }
}
