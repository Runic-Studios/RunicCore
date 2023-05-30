package com.runicrealms.plugin.spellapi.spelltypes;

import org.bukkit.entity.Player;

public record ShieldPayload(Player player, Player source, Shield shield) {
}
