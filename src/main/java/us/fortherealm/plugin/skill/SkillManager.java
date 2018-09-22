package us.fortherealm.plugin.skill;

import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skill.skilltypes.Skill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.skill.skills.*;

import java.util.*;

public class SkillManager {

    private List<Skill> skillList;
    private Main plugin;
    private HashMap<UUID, HashMap<Skill, Long>> cooldown;

    public SkillManager(Plugin plugin) {
        this.plugin = (Main) plugin;
        this.skillList = new ArrayList<>();
        this.cooldown = new HashMap<>();
    }

    public void registerSkills() {
        this.skillList.add(new Fireball());
        this.skillList.add(new Frostbolt());
        this.skillList.add(new Speed());
        this.skillList.add(new Heal());
        this.skillList.add(new Parry());
        this.skillList.add(new Blink());
        this.skillList.add(new Comet());
        this.skillList.add(new Windstride());
        this.skillList.add(new IceNova());
        this.skillList.add(new Rejuvenate());
        this.skillList.add(new Discharge());
        this.skillList.add(new Enrage());
        this.skillList.add(new Deliverance());
        this.skillList.add(new SmokeBomb());
        this.skillList.add(new Backstab());
        this.skillList.add(new Barrage());
        this.skillList.add(new Grapple());
    }

    public void startCooldownTask() {
        new BukkitRunnable() {
            @Override
            public void run() {

                for(Player player : Bukkit.getOnlinePlayers()) {
                    if(cooldown.containsKey(player.getUniqueId())) {
                        HashMap<Skill, Long> skills = cooldown.get(player.getUniqueId());
                        List<String> cdString = new ArrayList<>();

                        for(Skill skill : skills.keySet()) {
                            cdString.add(ChatColor.RED + skill.getName() + ChatColor.RED + ": " + ChatColor.YELLOW + getUserCooldown(player, skill) +/*+ ChatColor.RED +*/ "s");
                        }

                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + String.join(ChatColor.YELLOW + " ", cdString)));
                    }
                }
            }
        }.runTaskTimer(this.plugin, 0, 10);
    }

    public List<Skill> getSkills() {
        return this.skillList;
    }

    public void addCooldown(final Player player, final Skill skill, double cooldownTime) {
        if(this.cooldown.containsKey(player.getUniqueId())) {
            HashMap<Skill, Long> playerSkillsOnCooldown = this.cooldown.get(player.getUniqueId());
            playerSkillsOnCooldown.put(skill, System.currentTimeMillis());
            this.cooldown.put(player.getUniqueId(), playerSkillsOnCooldown);
        } else {
            HashMap<Skill, Long> playerSkillsOnCooldown = new HashMap<>();
            playerSkillsOnCooldown.put(skill, System.currentTimeMillis());
            this.cooldown.put(player.getUniqueId(), playerSkillsOnCooldown);
        }

        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            SkillManager.this.removeCooldown(player, skill);
        }, (long) cooldownTime * 20);

    }

    public boolean isOnCooldown(Player player, Skill skill) {
        if(!this.cooldown.containsKey(player.getUniqueId())){
            return false;
        }
        HashMap<Skill, Long> playerSkillsOnCooldown = this.cooldown.get(player.getUniqueId());
        if(playerSkillsOnCooldown.containsKey(skill)) {
            return true;
        }
        return false;
    }

    @SuppressWarnings({"unchecked", "IntegerDivisionInFloatingPointContext"})
    public int getUserCooldown(Player player, Skill skill) {
        double cooldownRemaining = 0;

        if(isOnCooldown(player, skill)) {
            HashMap<Skill, Long> cd = this.cooldown.get(player.getUniqueId());
            if(cd.containsKey(skill)) {
                cooldownRemaining = (cd.get(skill) + ((skill.getCooldown() + 1) * 1000)) - System.currentTimeMillis();
            }
        }
        return ((int) (cooldownRemaining / 1000));
    }

    public void removeCooldown(Player player, Skill skill) { // in case we forget to remove a removeCooldown method
        if(!this.cooldown.containsKey(player.getUniqueId())) {
            return;
        }
        HashMap<Skill, Long> playerSkillsOnCooldown =  this.cooldown.get(player.getUniqueId());
        playerSkillsOnCooldown.remove(skill);
        this.cooldown.put(player.getUniqueId(), playerSkillsOnCooldown);
    }

    public Skill getSkillByName(String name) {
        Skill foundSkill = null;
        for(Skill skill : getSkills()) {
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

}
