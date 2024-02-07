package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.effect.rogue.ShroudedEffect;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Optional;
import java.util.UUID;

public class Unseen extends Spell implements DurationSpell {
    private double duration;

    public Unseen() {
        super("Unseen", CharacterClass.ROGUE);
        this.setDescription("For " + duration + "s, you vanish completely, " +
                "gaining &9shrouded&7! Dealing or taking damage from players " +
                "ends the effect early. " +
                "\n\n&2&lEFFECT &9Shrouded" +
                "\n&9Shrouded &7causes you to appear invisible to " +
                "players. During this time, you are " +
                "immune to damage from monsters!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        // Poof!
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.5f);
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getEyeLocation(), 15, 0.5f, 0.5f, 0.5f,
                new Particle.DustOptions(Color.BLACK, 1));
        ShroudedEffect shroudedEffect = new ShroudedEffect(player, this.duration);
        shroudedEffect.initialize();
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    /**
     * Player is immune to mob attacks
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMobDamage(MobDamageEvent event) {
        if (!(event.getVictim() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();
        Optional<SpellEffect> spellEffectOpt = this.getSpellEffect(uuid, uuid, SpellEffectType.SHROUDED);
        if (spellEffectOpt.isEmpty()) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        UUID victimId = event.getVictim().getUniqueId();
        Optional<SpellEffect> spellEffectOptP = this.getSpellEffect(uuid, uuid, SpellEffectType.SHROUDED);
        Optional<SpellEffect> spellEffectOptV = this.getSpellEffect(victimId, victimId, SpellEffectType.SHROUDED);
        if (spellEffectOptP.isPresent()) {
            ShroudedEffect shroudedEffect = (ShroudedEffect) spellEffectOptP.get();
            shroudedEffect.cancel();
        }
        if (spellEffectOptV.isPresent()) {
            ShroudedEffect shroudedEffect = (ShroudedEffect) spellEffectOptV.get();
            shroudedEffect.cancel();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMagicDamage(MagicDamageEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        UUID victimId = event.getVictim().getUniqueId();
        Optional<SpellEffect> spellEffectOptP = this.getSpellEffect(uuid, uuid, SpellEffectType.SHROUDED);
        Optional<SpellEffect> spellEffectOptV = this.getSpellEffect(victimId, victimId, SpellEffectType.SHROUDED);
        if (spellEffectOptP.isPresent()) {
            ShroudedEffect shroudedEffect = (ShroudedEffect) spellEffectOptP.get();
            shroudedEffect.cancel();
        }
        if (spellEffectOptV.isPresent()) {
            ShroudedEffect shroudedEffect = (ShroudedEffect) spellEffectOptV.get();
            shroudedEffect.cancel();
        }
    }
}
