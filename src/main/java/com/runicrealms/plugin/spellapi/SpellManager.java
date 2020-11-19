package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spells.archer.*;
import com.runicrealms.plugin.spellapi.spells.cleric.*;
import com.runicrealms.plugin.spellapi.spells.mage.*;
import com.runicrealms.plugin.spellapi.spells.rogue.*;
import com.runicrealms.plugin.spellapi.spells.runic.active.*;
import com.runicrealms.plugin.spellapi.spells.runic.passive.*;
import com.runicrealms.plugin.spellapi.spells.warrior.*;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SpellManager implements Listener {

    private final List<Spell> spellList;
    private final HashMap<UUID, HashMap<Spell, Long>> cooldown;
    private final HashSet<UUID> silencedEntities;
    private final RunicCore plugin = RunicCore.getInstance();

    public SpellManager() {
        this.spellList = new ArrayList<>();
        this.cooldown = new HashMap<>();
        this.silencedEntities = new HashSet<>();
        this.registerSpells();
        this.startCooldownTask();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public List<Spell> getSpells() {
        return this.spellList;
    }

    public HashSet<UUID> getSilencedEntities() {
        return silencedEntities;
    }

    /**
     *
     * @param player
     * @param spell
     * @param cooldownTime
     */
    public void addCooldown(final Player player, final Spell spell, double cooldownTime) {

        if(this.cooldown.containsKey(player.getUniqueId())) {
            HashMap<Spell, Long> playerSpellsOnCooldown = this.cooldown.get(player.getUniqueId());
            playerSpellsOnCooldown.put(spell, System.currentTimeMillis());
            this.cooldown.put(player.getUniqueId(), playerSpellsOnCooldown);
        } else {
            HashMap<Spell, Long> playerSpellsOnCooldown = new HashMap<>();
            playerSpellsOnCooldown.put(spell, System.currentTimeMillis());
            this.cooldown.put(player.getUniqueId(), playerSpellsOnCooldown);
        }

        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin,
                () -> removeCooldown(player, spell), (long) cooldownTime * 20);

    }

    public boolean isOnCooldown(Player player, String spellName) {
        if(!this.cooldown.containsKey(player.getUniqueId()))
            return false;
        HashMap<Spell, Long> playerSpellsOnCooldown = this.cooldown.get(player.getUniqueId());
        return playerSpellsOnCooldown.keySet().stream().anyMatch(n -> n.getName().equalsIgnoreCase(spellName));
    }

    @SuppressWarnings({"IntegerDivisionInFloatingPointContext"})
    private int getUserCooldown(Player player, Spell spell) {
        double cooldownRemaining = 0;

        if(isOnCooldown(player, spell.getName())) {
            HashMap<Spell, Long> cd = this.cooldown.get(player.getUniqueId());
            if(cd.keySet().stream().anyMatch(n -> n.getName().equalsIgnoreCase(spell.getName()))) {
                cooldownRemaining = (cd.get(spell) + ((spell.getCooldown() + 1) * 1000)) - System.currentTimeMillis();
            }
        }
        return ((int) (cooldownRemaining / 1000));
    }

    private void removeCooldown(Player player, Spell spell) { // in case we forget to remove a removeCooldown method
        if(!this.cooldown.containsKey(player.getUniqueId())) return;
        HashMap<Spell, Long> playerSpellsOnCooldown =  this.cooldown.get(player.getUniqueId());
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


    private void registerSpells() {
        this.spellList.add(new Fireball());
        this.spellList.add(new Frostbolt());
        this.spellList.add(new Sprint());
        this.spellList.add(new Parry());
        this.spellList.add(new Blink());
        this.spellList.add(new Cleave());
        this.spellList.add(new MeteorShower());
        this.spellList.add(new Windstride());
        this.spellList.add(new Rejuvenate());
        this.spellList.add(new Discharge());
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
        this.spellList.add(new BlessedRain());
        this.spellList.add(new Sandstorm());
        this.spellList.add(new Frostbite());
        this.spellList.add(new RottingShot());
        this.spellList.add(new UnholyGround());
        this.spellList.add(new RunicShield());
        this.spellList.add(new ShriekingSkull());
        this.spellList.add(new WoundingShot());
        this.spellList.add(new ThrowAxe());
        this.spellList.add(new ArcaneOrb());
        this.spellList.add(new Lunge());
        this.spellList.add(new Harpoon());
        this.spellList.add(new Warsong());
        this.spellList.add(new Reflect());
        this.spellList.add(new Siphon());
        this.spellList.add(new IceVolley());
        this.spellList.add(new ArrowBomb());
        this.spellList.add(new SearingShot());
        this.spellList.add(new BarkShield());
        this.spellList.add(new Enflame());
        this.spellList.add(new Heal());
        this.spellList.add(new Smite());
        this.spellList.add(new Sandstorm());
        this.spellList.add(new PlagueBomb());
        this.spellList.add(new Bolt());
        this.spellList.add(new Cleanse());
        this.spellList.add(new Eruption());
        this.spellList.add(new HolyWater());
        this.spellList.add(new Shadowbolt());
        this.spellList.add(new Absolution());
        this.spellList.add(new Insanity());
        this.spellList.add(new Marksman());
        this.spellList.add(new Manawell());
        this.spellList.add(new Agility());
        this.spellList.add(new Predator());
        this.spellList.add(new Resolve());
        this.spellList.add(new Taunt());
        this.spellList.add(new RunicMissile());
        this.spellList.add(new Battlecry());
        this.spellList.add(new Leech());
        this.spellList.add(new BloodRitual());
        this.spellList.add(new Ensnare());
        this.spellList.add(new ManaShield());
        this.spellList.add(new Rupture());
        this.spellList.add(new Repent());
        this.spellList.add(new Cripple());
        this.spellList.add(new ArcaneShot());
        this.spellList.add(new SanguineShot());
    }

    // starts the repeating task to manage player cooldowns
    private void startCooldownTask() {
        new BukkitRunnable() {
            @Override
            public void run() {

                for(Player player : Bukkit.getOnlinePlayers()) {
                    if(cooldown.containsKey(player.getUniqueId())) {
                        HashMap<Spell, Long> spells = cooldown.get(player.getUniqueId());
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
        if (silencedEntities.contains(e.getDamager().getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        if (silencedEntities.contains(e.getPlayer().getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {
        if (silencedEntities.contains(e.getPlayer().getUniqueId()))
            e.setCancelled(true);
    }
}
