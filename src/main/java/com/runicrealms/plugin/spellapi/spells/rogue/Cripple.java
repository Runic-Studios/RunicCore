package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
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

public class Cripple extends Spell implements PhysicalDamageSpell {
    private static final int DURATION = 3;
    private static final double PERCENT = 40;
    private static final int RADIUS = 2;
    private final Set<UUID> crippledEntities;
    private double damage;
    private double damagePerLevel;

    public Cripple() {
        super("Cripple", CharacterClass.ROGUE);
        crippledEntities = new HashSet<>();
        this.setDescription("You cripple enemies within " + RADIUS + " " +
                "blocks, dealing (" + damage + " + &f" + damagePerLevel + "x&7 lvl) physical⚔ " +
                "damage to them reducing their damage by " + (int) PERCENT + "% " +
                "for " + DURATION + " seconds!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CHICKEN_DEATH, 0.5f, 1.0f);

        for (Entity entity : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!(entity instanceof LivingEntity)) continue;
            if (!isValidEnemy(player, entity)) continue;
            entity.getWorld().spawnParticle(Particle.CLOUD, ((LivingEntity) entity).getEyeLocation(), 15, 0.5f, 0.5f, 0.5f, 0);
            DamageUtil.damageEntityPhysical(damage, (LivingEntity) entity, player, false, false, this);
            crippledEntities.add(entity.getUniqueId());
            entity.sendMessage(ChatColor.RED + "You have been crippled!");
            Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> {
                crippledEntities.remove(entity.getUniqueId());
                entity.sendMessage(ChatColor.GREEN + "You are no longer crippled!");
            }, DURATION * 20L);
        }
    }

    @Override
    public double getPhysicalDamage() {
        return damage;
    }

    @Override
    public void setPhysicalDamage(double physicalDamage) {
        this.damage = physicalDamage;
    }

    @Override
    public double getPhysicalDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setPhysicalDamagePerLevel(double physicalDamagePerLevel) {
        this.damagePerLevel = physicalDamagePerLevel;
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent event) {
        if (!crippledEntities.contains(event.getEntity().getUniqueId())) return;
        double percent = PERCENT / 100;
        double reduced = event.getAmount() * percent;
        event.setAmount((int) (event.getAmount() - reduced));
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!crippledEntities.contains(event.getPlayer().getUniqueId())) return;
        double percent = PERCENT / 100;
        double reduced = event.getAmount() * percent;
        event.setAmount((int) (event.getAmount() - reduced));
    }

    @EventHandler
    public void onSpellDamage(MagicDamageEvent event) {
        if (!crippledEntities.contains(event.getPlayer().getUniqueId())) return;
        double percent = PERCENT / 100;
        double reduced = event.getAmount() * percent;
        event.setAmount((int) (event.getAmount() - reduced));
    }
}
