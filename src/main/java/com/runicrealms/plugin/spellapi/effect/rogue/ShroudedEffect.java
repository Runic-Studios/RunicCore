package com.runicrealms.plugin.spellapi.effect.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ShroudedEffect implements SpellEffect {
    private final Player caster;
    private final double duration;
    private long startTime;

    /**
     * @param caster   player who is incendiary
     * @param duration (in seconds) before the effect expires
     */
    public ShroudedEffect(Player caster, double duration) {
        this.caster = caster;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void onExpire() {
        // Caster reappears
        for (UUID uuid : RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters()) {
            Player loaded = Bukkit.getPlayer(uuid);
            if (loaded == null) continue;
            loaded.showPlayer(RunicCore.getInstance(), caster);
        }
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.5f);
        caster.getWorld().spawnParticle(Particle.REDSTONE, caster.getEyeLocation(), 15, 0.5f, 0.5f, 0.5f,
                new Particle.DustOptions(Color.BLACK, 1));
        caster.sendMessage(ChatColor.GRAY + "You reappeared!");
    }

    @Override
    public void cancel() {
        startTime = (long) (System.currentTimeMillis() - (duration * 1000)); // Immediately end effect
    }

    public void refresh() {
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public SpellEffectType getEffectType() {
        return SpellEffectType.SHROUDED;
    }

    @Override
    public boolean isBuff() {
        return true;
    }

    @Override
    public Player getCaster() {
        return caster;
    }

    @Override
    public LivingEntity getRecipient() {
        return caster;
    }

    @Override
    public void tick(int globalCounter) {
        if (caster.isDead()) {
            this.cancel();
            return;
        }
        if (globalCounter % 20 == 0) { // Show particle once per second
            executeSpellEffect();
        }
    }

    @Override
    public void executeSpellEffect() {
        caster.playSound(caster.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.2f, 0.2f);
        caster.spawnParticle(
                Particle.SMOKE_NORMAL,
                caster.getEyeLocation(),
                5,
                0.35f,
                0.35f,
                0.35f,
                0
        );
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public void initialize() {
        RunicCore.getSpellEffectAPI().addSpellEffectToManager(this);

        // hide the player, prevent them from disappearing in tab
        for (UUID uuid : RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters()) {
            Player loaded = Bukkit.getPlayer(uuid);
            if (loaded == null) continue;
            loaded.hidePlayer(RunicCore.getInstance(), caster);
        }

        caster.sendMessage(ChatColor.GRAY + "You vanished!");
    }

}
