package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Cripple extends Spell {

    private static final int DURATION = 3;
    private static final double PERCENT = 65;
    private static final int RADIUS = 3;
    private final Set<UUID> crippledEntities;

    public Cripple() {
        super("Cripple",
                "You cripple enemies within " + RADIUS + " " +
                        "blocks, disorienting them and " +
                        "reducing their damage by " + (int) PERCENT + "% " +
                        "for " + DURATION + " seconds!",
                ChatColor.WHITE, ClassEnum.ROGUE, 15, 20);
        crippledEntities = new HashSet<>();
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // spell variables, vectors
        pl.swingMainHand();
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_CHICKEN_DEATH, 0.5f, 1.0f);

        for (Entity en : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!(en instanceof LivingEntity)) continue;
            if (!verifyEnemy(pl, en)) continue;
            en.getWorld().spawnParticle(Particle.CLOUD, ((LivingEntity) en).getEyeLocation(), 15, 0.5f, 0.5f, 0.5f, 0);
            crippledEntities.add(en.getUniqueId());
            en.sendMessage(ChatColor.RED + "You have been crippled!");
            Bukkit.getScheduler().scheduleAsyncDelayedTask(RunicCore.getInstance(), () -> {
                crippledEntities.remove(en.getUniqueId());
                en.sendMessage(ChatColor.GREEN + "You are no longer crippled!");
            }, DURATION * 20L);
        }
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {
        if (!crippledEntities.contains(e.getPlayer().getUniqueId())) return;
        double percent = PERCENT / 100;
        double reduced = e.getAmount() * percent;
        e.setAmount((int) (e.getAmount() - reduced));
    }

    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        if (!crippledEntities.contains(e.getPlayer().getUniqueId())) return;
        double percent = PERCENT / 100;
        double reduced = e.getAmount() * percent;
        e.setAmount((int) (e.getAmount() - reduced));
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent e) {
        if (!crippledEntities.contains(e.getDamager().getUniqueId())) return;
        double percent = PERCENT / 100;
        double reduced = e.getAmount() * percent;
        e.setAmount((int) (e.getAmount() - reduced));
    }
}
