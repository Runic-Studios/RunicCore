package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.ISpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * New bard ultimate passive
 *
 * @author BoBoBalloon
 */
public class Tempo extends Spell implements RadiusSpell, DurationSpell {
    private static final Map<UUID, Set<Pair<Influenced, Double>>> EXTENSIONS = new HashMap<>();
    private final Set<UUID> specialAttacks;
    private int restore;
    private double radius;
    private double duration;

    public Tempo() {
        super("Tempo", CharacterClass.CLERIC);
        this.setIsPassive(true);
        this.setDescription("Whenever you cast a spell, your next basic attack restores " +
                this.restore + " mana to yourself and allies within " + this.radius + " blocks. " +
                "Additionally, this enhanced basic attack increases the duration of your active " +
                "&aAccelerando&7, &aBattlecry&7 and &aGrand Symphony&7 by " + this.duration + "s each.");
        this.specialAttacks = new HashSet<>();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onSpellCast(SpellCastEvent event) {
        if (!this.hasPassive(event.getCaster().getUniqueId(), this.getName()) || event.getSpell().isPassive() || !event.getSpell().isDisplayingCastMessage()) {
            return;
        }

        this.specialAttacks.add(event.getCaster().getUniqueId());

        RunicCore.getRegenManager().addMana(event.getCaster(), this.restore);

        for (Entity entity : event.getCaster().getNearbyEntities(this.radius, this.radius, this.radius)) {
            if (!(entity instanceof Player ally) || !this.isValidAlly(event.getCaster(), ally)) {
                continue;
            }

            RunicCore.getRegenManager().addMana(ally, this.restore);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!this.specialAttacks.remove(event.getPlayer().getUniqueId())) {
            return;
        }

        Spell one = RunicCore.getSpellAPI().getSpell("Battlecry");
        Spell two = RunicCore.getSpellAPI().getSpell("Accelerando");
        Spell three = RunicCore.getSpellAPI().getSpell("Grand Symphony");

        if (!(one instanceof Battlecry battlecry) || !(two instanceof Accelerando accelerando) || !(three instanceof GrandSymphony grandSymphony)) {
            RunicCore.getInstance().getLogger().warning("One of the dependant spells on the Tempo spell is missing!");
            return;
        }

        battlecry.increaseExtraDuration(event.getPlayer(), this.duration);
        accelerando.increaseExtraDuration(event.getPlayer(), this.duration);
        grandSymphony.increaseExtraDuration(event.getPlayer(), this.duration);
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        EXTENSIONS.remove(event.getPlayer().getUniqueId());
        this.specialAttacks.remove(event.getPlayer().getUniqueId());
    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number restore = (Number) spellData.getOrDefault("restore", 10);
        this.restore = restore.intValue();
    }

    @Override
    public double getRadius() {
        return this.radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public double getDuration() {
        return this.duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    /**
     * An interface used to determine if a spell is handled by {@link Tempo}
     */
    public interface Influenced extends ISpell, DurationSpell {
        default double getDuration(@NotNull Player player) {
            return this.getDuration() + this.getExtraSeconds(player);
        }

        default double getExtraSeconds(@NotNull Player player) {
            if (!this.hasPassive(player.getUniqueId(), "Tempo")) {
                return 0;
            }

            Set<Pair<Influenced, Double>> data = EXTENSIONS.get(player.getUniqueId());

            if (data == null) {
                return 0;
            }

            Optional<Pair<Influenced, Double>> extension = data.stream().filter(pair -> this.getName().equals(pair.first.getName())).findAny();

            return extension.isPresent() ? extension.get().second : 0;
        }

        default void increaseExtraDuration(@NotNull Player player, double seconds) {
            if (!this.hasPassive(player.getUniqueId(), "Tempo")) {
                return;
            }

            if (!EXTENSIONS.containsKey(player.getUniqueId())) {
                EXTENSIONS.put(player.getUniqueId(), new HashSet<>());
            }

            Set<Pair<Influenced, Double>> data = EXTENSIONS.get(player.getUniqueId());

            Optional<Pair<Influenced, Double>> extension = data.stream().filter(pair -> this.getName().equals(pair.first.getName())).findAny();

            if (extension.isPresent()) {
                data.remove(extension.get());
                data.add(Pair.pair(this, Math.max(Math.min(extension.get().second + seconds, this.getMaxExtraDuration()), 0)));
            } else {
                data.add(Pair.pair(this, Math.max(Math.min(seconds, this.getMaxExtraDuration()), 0)));
            }
        }

        default void removeExtraDuration(@NotNull Player player) {
            if (!this.hasPassive(player.getUniqueId(), "Tempo")) {
                return;
            }

            Set<Pair<Influenced, Double>> data = EXTENSIONS.get(player.getUniqueId());

            if (data == null) {
                return;
            }

            data.removeIf(pair -> this.getName().equals(pair.first.getName()));
        }

        default double getMaxExtraDuration() {
            return Double.MAX_VALUE;
        }
    }
}
