package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.RunicCore;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SpellManager {

    private List<Spell> spellList;
    private RunicCore plugin = RunicCore.getInstance();
    private HashMap<UUID, HashMap<String, Long>> cooldown;

    public SpellManager() {
        this.spellList = new ArrayList<>();
        this.cooldown = new HashMap<>();

        this.registerSpells();
        this.startCooldownTask();
    }

    public List<Spell> getSpells() {
        return this.spellList;
    }

    public void addCooldown(final Player player, final Spell spell, double cooldownTime) {
        if(this.cooldown.containsKey(player.getUniqueId())) {
            HashMap<String, Long> playerSpellsOnCooldown = this.cooldown.get(player.getUniqueId());
            playerSpellsOnCooldown.put(spell.getName(), System.currentTimeMillis());
            this.cooldown.put(player.getUniqueId(), playerSpellsOnCooldown);
        } else {
            HashMap<String, Long> playerSpellsOnCooldown = new HashMap<>();
            playerSpellsOnCooldown.put(spell.getName(), System.currentTimeMillis());
            this.cooldown.put(player.getUniqueId(), playerSpellsOnCooldown);
        }

        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> SpellManager.this.removeCooldown(player, spell), (long) cooldownTime * 20);

    }

    public boolean isOnCooldown(Player player, String spellName) {
        if(!this.cooldown.containsKey(player.getUniqueId())){
            return false;
        }
        HashMap<String, Long> playerSpellsOnCooldown = this.cooldown.get(player.getUniqueId());
        return playerSpellsOnCooldown.containsKey(spellName);
    }

    @SuppressWarnings({"IntegerDivisionInFloatingPointContext"})
    private int getUserCooldown(Player player, String spellName) {
        double cooldownRemaining = 0;

        if(isOnCooldown(player, spellName)) {
            HashMap<String, Long> cd = this.cooldown.get(player.getUniqueId());
            if(cd.containsKey(spellName)) {
                cooldownRemaining = (cd.get(spellName) + ((RunicCore.getSpellManager().getSpellByName(spellName).getCooldown() + 1) * 1000)) - System.currentTimeMillis();
            }
        }
        return ((int) (cooldownRemaining / 1000));
    }

    private void removeCooldown(Player player, Spell spell) { // in case we forget to remove a removeCooldown method
        if(!this.cooldown.containsKey(player.getUniqueId())) {
            return;
        }
        HashMap<String, Long> playerSpellsOnCooldown =  this.cooldown.get(player.getUniqueId());
        playerSpellsOnCooldown.remove(spell.getName());
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
    }

    // starts the repeating task to manage player cooldowns
    private void startCooldownTask() {
        new BukkitRunnable() {
            @Override
            public void run() {

                for(Player player : Bukkit.getOnlinePlayers()) {
                    if(cooldown.containsKey(player.getUniqueId())) {
                        HashMap<String, Long> spells = cooldown.get(player.getUniqueId());
                        List<String> cdString = new ArrayList<>();

                        for(String spellName : spells.keySet()) {
                            cdString.add(ChatColor.RED + spellName + ChatColor.RED + ": " + ChatColor.YELLOW + getUserCooldown(player, spellName) +/*+ ChatColor.RED +*/ "s");
                        }

                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + String.join(ChatColor.YELLOW + " ", cdString)));
                    }
                }
            }
        }.runTaskTimer(this.plugin, 0, 10);
    }
}
