package com.runicrealms.plugin.spellapi.spells.runic;

import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Backstab extends Spell {

//    private static final int DAMAGE_AMT = 10;
//    private static final int DURATION = 10;
//    private HashMap<UUID, Long> stabbers = new HashMap<>();

    public Backstab() {
        super("Backstab",
                "For seconds, striking enemies from" +
                        "\nbehind with your weapon deals " +
                        "\nadditional spellʔ damage!",
                ChatColor.WHITE, 0, 0);
        this.setIsPassive(true);
    }

//    @Override
//    public void executeSpell(Player player, SpellItemType type) {
//        UUID uuid = player.getUniqueId();
//        stabbers.put(uuid, System.currentTimeMillis());
//        player.sendMessage(ChatColor.GREEN + "You are now backstabbing!");
//        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 0.5f, 1.0f);
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                stabbers.remove(uuid);
//                player.sendMessage(ChatColor.GRAY + "You are no longer backstabbing.");
//            }
//        }.runTaskLater(plugin, DURATION*20L);
//    }

    @EventHandler
    public void onDamage(WeaponDamageEvent e) {

        Player pl = e.getPlayer();
        UUID uuid = pl.getUniqueId();
        LivingEntity le = (LivingEntity) e.getEntity();

        if (le.hasMetadata("NPC")) return;

        //if (!stabbers.containsKey(uuid)) return;

        // if the dot-product of both entitys' vectors is greater than 0 (positive),
        // then they're facing the same direction and it's a backstab
        if (!(pl.getLocation().getDirection().dot(e.getEntity().getLocation().getDirection()) >= 0.0D)) return;

        // execute skill effects
        //DamageUtil.damageEntitySpell((DAMAGE_AMT), le, pl, false);
        le.getWorld().spawnParticle(Particle.CRIT_MAGIC, le.getEyeLocation(), 25, 0.25, 0.25, 0.25, 0);
        le.getWorld().playSound(le.getLocation(), Sound.ENTITY_WITCH_HURT, 0.5f, 0.8f);
    }
}

