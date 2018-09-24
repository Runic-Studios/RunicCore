package us.fortherealm.plugin.skills.caster;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.caster.itemcaster.runes.RuneCasterTester;

import java.util.*;

public class CasterManager {

    private List<Caster> casterList;
    private Main plugin = Main.getInstance();
    private HashMap<UUID, HashMap<Caster, Long>> cooldown;

    public CasterManager() {
        this.casterList = new ArrayList<>();
        this.cooldown = new HashMap<>();

        this.registerCasters();
        this.startCooldownTask();
    }

    public void addCooldown(final Player player, final Caster caster, double cooldownTime) {
        if(this.cooldown.containsKey(player.getUniqueId())) {
            HashMap<Caster, Long> playerSkillsOnCooldown = this.cooldown.get(player.getUniqueId());
            playerSkillsOnCooldown.put(caster, System.currentTimeMillis());
            this.cooldown.put(player.getUniqueId(), playerSkillsOnCooldown);
        } else {
            HashMap<Caster, Long> playerSkillsOnCooldown = new HashMap<>();
            playerSkillsOnCooldown.put(caster, System.currentTimeMillis());
            this.cooldown.put(player.getUniqueId(), playerSkillsOnCooldown);
        }

        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            CasterManager.this.removeCooldown(player, caster);
        }, (long) cooldownTime * 20);

    }

    public boolean isOnCooldown(Player player, Caster caster) {
        if(!this.cooldown.containsKey(player.getUniqueId())){
            return false;
        }
        HashMap<Caster, Long> playerSkillsOnCooldown = this.cooldown.get(player.getUniqueId());
        
        return playerSkillsOnCooldown.containsKey(caster);
    }

    @SuppressWarnings({"unchecked", "IntegerDivisionInFloatingPointContext"})
    public int getUserCooldown(Player player, Caster caster) {
        double cooldownRemaining = 0;

        if(isOnCooldown(player, caster)) {
            HashMap<Caster, Long> cd = this.cooldown.get(player.getUniqueId());
            if(cd.containsKey(caster)) {
                cooldownRemaining = (cd.get(caster) + ((caster.getCooldown() + 1) * 1000)) - System.currentTimeMillis();
            }
        }
        return ((int) (cooldownRemaining / 1000));
    }

    public void removeCooldown(Player player, Caster caster) { // in case we forget to remove a removeCooldown method
        if(!this.cooldown.containsKey(player.getUniqueId())) {
            return;
        }
        HashMap<Caster, Long> playerSkillsOnCooldown =  this.cooldown.get(player.getUniqueId());
        playerSkillsOnCooldown.remove(caster);
        this.cooldown.put(player.getUniqueId(), playerSkillsOnCooldown);
    }

    public Caster getCasterByName(String name) {
        Caster foundCaster = null;
        for(Caster caster : getCasters()) {
            if(caster.getName().equalsIgnoreCase(name)) {
                foundCaster = caster;
                break;
            }
        }
        return (foundCaster != null) ? foundCaster : null;
    }


    private void registerCasters() {
        this.casterList.add(new RuneCasterTester());
    }

    private void startCooldownTask() {
        new BukkitRunnable() {
            @Override
            public void run() {

                for(Player player : Bukkit.getOnlinePlayers()) {
                    if(cooldown.containsKey(player.getUniqueId())) {
                        HashMap<Caster, Long> casters = cooldown.get(player.getUniqueId());
                        List<String> cdString = new ArrayList<>();

                        for(Caster caster : casters.keySet()) {
                            cdString.add(ChatColor.RED + caster.getName() + ChatColor.RED + ": " + ChatColor.YELLOW + getUserCooldown(player, caster) + /*+ ChatColor.RED +*/ "s");
                        }

                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + String.join(ChatColor.YELLOW + " ", cdString)));
                    }
                }
            }
        }.runTaskTimer(this.plugin, 0, 10);
    }
    
    public List<Caster> getCasters() {
        return this.casterList;
    }
    
}
