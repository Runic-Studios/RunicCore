package com.runicrealms.plugin.spellapi.spelltypes;

import com.runicrealms.plugin.classes.ClassEnum;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface ISpell {

    void addStatusEffect(Entity entity, EffectEnum effectEnum, double duration);

    void execute(Player player, SpellItemType type); // casts the spell

    String getName(); // returns the spell name

    ChatColor getColor();

    String getDescription(); // returns the spell description

    ClassEnum getReqClass();

    double getCooldown();

    int getManaCost();

    boolean hasPassive(UUID uuid, String passive);

    boolean isInvulnerable(Entity entity);

    boolean isOnCooldown(Player player);

    boolean isRooted(Entity entity);

    boolean isSilenced(Entity entity);

    boolean isStunned(Entity entity);

    int percentMissingHealth(Entity entity, double percent);

    boolean verifyAlly(Player caster, Entity recipient); // checks for valid healing, shielding targets

    boolean verifyEnemy(Player caster, Entity victim); // check tons of things, like if target entity is NPC, party member, and outlaw checks
}

