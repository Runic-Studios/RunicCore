package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.spellapi.spells.archer.*;
import com.runicrealms.plugin.spellapi.spells.cleric.*;
import com.runicrealms.plugin.spellapi.spells.mage.*;
import com.runicrealms.plugin.spellapi.spells.rogue.*;
import com.runicrealms.plugin.spellapi.spells.runic.*;
import com.runicrealms.plugin.spellapi.spells.warrior.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SpellManager {

    private List<Spell> spellList;
    private RunicCore plugin = RunicCore.getInstance();
    private HashMap<UUID, HashMap<Spell, Long>> cooldown;

    public SpellManager() {
        this.spellList = new ArrayList<>();
        this.cooldown = new HashMap<>();

        this.registerSkills();
        this.startCooldownTask();
    }

    public List<Spell> getSkills() {
        return this.spellList;
    }

    public void addCooldown(final Player player, final Spell skill, double cooldownTime) {
        if(this.cooldown.containsKey(player.getUniqueId())) {
            HashMap<Spell, Long> playerSkillsOnCooldown = this.cooldown.get(player.getUniqueId());
            playerSkillsOnCooldown.put(skill, System.currentTimeMillis());
            this.cooldown.put(player.getUniqueId(), playerSkillsOnCooldown);
        } else {
            HashMap<Spell, Long> playerSkillsOnCooldown = new HashMap<>();
            playerSkillsOnCooldown.put(skill, System.currentTimeMillis());
            this.cooldown.put(player.getUniqueId(), playerSkillsOnCooldown);
        }

        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> SpellManager.this.removeCooldown(player, skill), (long) cooldownTime * 20);

    }

    public boolean isOnCooldown(Player player, Spell skill) {
        if(!this.cooldown.containsKey(player.getUniqueId())){
            return false;
        }
        HashMap<Spell, Long> playerSkillsOnCooldown = this.cooldown.get(player.getUniqueId());
        return playerSkillsOnCooldown.containsKey(skill);
    }

    @SuppressWarnings({"unchecked", "IntegerDivisionInFloatingPointContext"})
    public int getUserCooldown(Player player, Spell skill) {
        double cooldownRemaining = 0;

        if(isOnCooldown(player, skill)) {
            HashMap<Spell, Long> cd = this.cooldown.get(player.getUniqueId());
            if(cd.containsKey(skill)) {
                cooldownRemaining = (cd.get(skill) + ((skill.getCooldown() + 1) * 1000)) - System.currentTimeMillis();
            }
        }
        return ((int) (cooldownRemaining / 1000));
    }

    public void removeCooldown(Player player, Spell skill) { // in case we forget to remove a removeCooldown method
        if(!this.cooldown.containsKey(player.getUniqueId())) {
            return;
        }
        HashMap<Spell, Long> playerSkillsOnCooldown =  this.cooldown.get(player.getUniqueId());
        playerSkillsOnCooldown.remove(skill);
        this.cooldown.put(player.getUniqueId(), playerSkillsOnCooldown);
    }

    public Spell getSkillByName(String name) {
        Spell foundSkill = null;
        for(Spell skill : getSkills()) {
            if(skill.getName().equalsIgnoreCase(name)) {
                foundSkill = skill;
                break;
            }
        }
        if(foundSkill != null)
            return foundSkill;
        else
            return null;
    }


    private void registerSkills() {
        this.spellList.add(new Fireball());
        this.spellList.add(new Frostbolt());
        this.spellList.add(new Sprint());
        this.spellList.add(new Parry());
        this.spellList.add(new Blink());
        this.spellList.add(new Comet());
        this.spellList.add(new Windstride());
        this.spellList.add(new ArcaneSpike());
        this.spellList.add(new Rejuvenate());
        this.spellList.add(new Discharge());
        this.spellList.add(new Enrage());
        this.spellList.add(new Judgment());
        this.spellList.add(new SmokeBomb());
        this.spellList.add(new Backstab());
        this.spellList.add(new Barrage());
        this.spellList.add(new Grapple());
        this.spellList.add(new Charge());
        this.spellList.add(new Cloak());
        this.spellList.add(new HolyNova());
        this.spellList.add(new Blizzard());
        this.spellList.add(new BlessedRain());
    }

    // starts the repeating task to manage player cooldowns
    private void startCooldownTask() {
        new BukkitRunnable() {
            @Override
            public void run() {

                for(Player player : Bukkit.getOnlinePlayers()) {
                    if(cooldown.containsKey(player.getUniqueId())) {
                        HashMap<Spell, Long> skills = cooldown.get(player.getUniqueId());
                        List<String> cdString = new ArrayList<>();

                        for(Spell skill : skills.keySet()) {
                            cdString.add(ChatColor.RED + skill.getName() + ChatColor.RED + ": " + ChatColor.YELLOW + getUserCooldown(player, skill) +/*+ ChatColor.RED +*/ "s");
                        }

                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + String.join(ChatColor.YELLOW + " ", cdString)));
                    }
                }
            }
        }.runTaskTimer(this.plugin, 0, 10);
    }
}
