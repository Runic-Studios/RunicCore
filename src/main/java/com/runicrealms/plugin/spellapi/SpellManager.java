package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spells.archer.*;
import com.runicrealms.plugin.spellapi.spells.cleric.DivineShield;
import com.runicrealms.plugin.spellapi.spells.cleric.*;
import com.runicrealms.plugin.spellapi.spells.mage.*;
import com.runicrealms.plugin.spellapi.spells.rogue.*;
import com.runicrealms.plugin.spellapi.spells.runic.passive.Siphon;
import com.runicrealms.plugin.spellapi.spells.warrior.*;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    private final ConcurrentHashMap<UUID, ConcurrentHashMap<Spell, Long>> cooldown;
    private final HashMap<UUID, BukkitTask> rootedEntities;
    private final HashMap<UUID, BukkitTask> silencedEntities;
    private final HashMap<UUID, BukkitTask> stunnedEntities;
    private final RunicCore plugin = RunicCore.getInstance();

    public SpellManager() {
        this.spellList = new ArrayList<>();
        this.cooldown = new ConcurrentHashMap<>();
        this.rootedEntities = new HashMap<>();
        this.silencedEntities = new HashMap<>();
        this.stunnedEntities = new HashMap<>();
        this.registerSpells();
        this.startCooldownTask();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public List<Spell> getSpells() {
        return this.spellList;
    }

    public HashMap<UUID, BukkitTask> getRootedEntites() {
        return rootedEntities;
    }

    public HashMap<UUID, BukkitTask> getSilencedEntities() {
        return silencedEntities;
    }

    public HashMap<UUID, BukkitTask> getStunnedEntities() {
        return stunnedEntities;
    }

    /**
     * Adds spell to player, spell cooldown map
     * @param player to add cooldown to
     * @param spell to apply cooldown to
     * @param cooldownTime of spell
     */
    public void addCooldown(final Player player, final Spell spell, double cooldownTime) {

        if(this.cooldown.containsKey(player.getUniqueId())) {
            ConcurrentHashMap<Spell, Long> playerSpellsOnCooldown = this.cooldown.get(player.getUniqueId());
            playerSpellsOnCooldown.put(spell, System.currentTimeMillis());
            this.cooldown.put(player.getUniqueId(), playerSpellsOnCooldown);
        } else {
            ConcurrentHashMap<Spell, Long> playerSpellsOnCooldown = new ConcurrentHashMap<>();
            playerSpellsOnCooldown.put(spell, System.currentTimeMillis());
            this.cooldown.put(player.getUniqueId(), playerSpellsOnCooldown);
        }

        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin,
                () -> removeCooldown(player, spell), (long) cooldownTime * 20);

    }

    public boolean isOnCooldown(Player player, String spellName) {
        if(!this.cooldown.containsKey(player.getUniqueId()))
            return false;
        ConcurrentHashMap<Spell, Long> playerSpellsOnCooldown = this.cooldown.get(player.getUniqueId());
        return playerSpellsOnCooldown.keySet().stream().anyMatch(n -> n.getName().equalsIgnoreCase(spellName));
    }

    @SuppressWarnings({"IntegerDivisionInFloatingPointContext"})
    private int getUserCooldown(Player player, Spell spell) {
        double cooldownRemaining = 0;

        if(isOnCooldown(player, spell.getName())) {
            ConcurrentHashMap<Spell, Long> cd = this.cooldown.get(player.getUniqueId());
            if(cd.keySet().stream().anyMatch(n -> n.getName().equalsIgnoreCase(spell.getName()))) {
                cooldownRemaining = (cd.get(spell) + ((spell.getCooldown() + 1) * 1000)) - System.currentTimeMillis();
            }
        }
        return ((int) (cooldownRemaining / 1000));
    }

    private void removeCooldown(Player player, Spell spell) { // in case we forget to remove a removeCooldown method
        if(!this.cooldown.containsKey(player.getUniqueId())) return;
        ConcurrentHashMap<Spell, Long> playerSpellsOnCooldown =  this.cooldown.get(player.getUniqueId());
        playerSpellsOnCooldown.remove(spell);
        this.cooldown.put(player.getUniqueId(), playerSpellsOnCooldown);
    }

    public Spell getSpellByName(String name) {
        Spell foundSpell = null;
        for(Spell spell : getSpells()) {
            if(spell.getName().equalsIgnoreCase(name)) {
                foundSpell = spell;
                break;
            }
        }
        return foundSpell;
    }

    /**
     * Add all spell classes to spell manager
     */
    private void registerSpells() {
        this.spellList.add(new Fireball());
        this.spellList.add(new Frostbolt());
        this.spellList.add(new Sprint());
        this.spellList.add(new Parry());
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
        this.spellList.add(new Siphon());
        this.spellList.add(new IceVolley());
        this.spellList.add(new PowerShot());
        this.spellList.add(new Smite());
        this.spellList.add(new ShadowBomb());
        this.spellList.add(new Cleanse());
        this.spellList.add(new HolyWater());
        this.spellList.add(new Shadowbolt());
        this.spellList.add(new Insanity());
        this.spellList.add(new Hawkeye());
        this.spellList.add(new Manawell());
        this.spellList.add(new Agility());
        this.spellList.add(new Predator());
        this.spellList.add(new Resolve());
        this.spellList.add(new Taunt());
        this.spellList.add(new Leech());
        this.spellList.add(new Frostbite());
        this.spellList.add(new ManaShield());
        this.spellList.add(new SliceAndDice());
        this.spellList.add(new Cripple());
        this.spellList.add(new ArcaneShot());
        this.spellList.add(new SummonSentry());
        this.spellList.add(new Challenger());
        this.spellList.add(new Riposte());
        this.spellList.add(new Kneebreak());
        this.spellList.add(new FireBlast());
        this.spellList.add(new Scald());
        this.spellList.add(new FireAura());
        this.spellList.add(new BlazingSpeed());
        this.spellList.add(new IceBlock());
        this.spellList.add(new ColdTouch());
        this.spellList.add(new IcyAffinity());
        this.spellList.add(new ShadowTouch());
        this.spellList.add(new Shadowmeld());
        this.spellList.add(new TwistOfFate());
        this.spellList.add(new Cleave());
        this.spellList.add(new Whirlwind());
        this.spellList.add(new LastResort());
        this.spellList.add(new Rescue());
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
    }

    // starts the repeating task to manage player cooldowns
    private void startCooldownTask() {
        new BukkitRunnable() {
            @Override
            public void run() {

                for(Player player : Bukkit.getOnlinePlayers()) {
                    if(cooldown.containsKey(player.getUniqueId())) {
                        ConcurrentHashMap<Spell, Long> spells = cooldown.get(player.getUniqueId());
                        List<String> cdString = new ArrayList<>();

                        for(Spell spell : spells.keySet()) {
                            if (getUserCooldown(player, spell) <= 0)
                                removeCooldown(player, spell); // insurance
                            else
                                cdString.add(ChatColor.RED + spell.getName() + ChatColor.RED + ": " + ChatColor.YELLOW + getUserCooldown(player, spell) + "s");
                        }

                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + String.join(ChatColor.YELLOW + " ", cdString)));
                    }
                }
            }
        }.runTaskTimer(this.plugin, 0, 10);
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent e) {
        if (silencedEntities.isEmpty() && stunnedEntities.isEmpty()) return;
        if (silencedEntities.containsKey(e.getDamager().getUniqueId())
                || stunnedEntities.containsKey(e.getDamager().getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {
        if (silencedEntities.isEmpty() && stunnedEntities.isEmpty()) return;
        if (silencedEntities.containsKey(e.getPlayer().getUniqueId())
                || stunnedEntities.containsKey(e.getPlayer().getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onSpellHeal(SpellHealEvent e) {
        if (silencedEntities.isEmpty() && stunnedEntities.isEmpty()) return;
        if (silencedEntities.containsKey(e.getPlayer().getUniqueId())
                || stunnedEntities.containsKey(e.getPlayer().getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        if (silencedEntities.isEmpty() && stunnedEntities.isEmpty()) return;
        if (silencedEntities.containsKey(e.getPlayer().getUniqueId())
                || stunnedEntities.containsKey(e.getPlayer().getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (rootedEntities.isEmpty() && stunnedEntities.isEmpty()) return;
        if (!(rootedEntities.containsKey(e.getPlayer().getUniqueId())
                || stunnedEntities.containsKey(e.getPlayer().getUniqueId()))) return;
        if (e.getTo() == null) return;
        if (!e.getFrom().toVector().equals(e.getTo().toVector())) e.setCancelled(true);
    }
}
