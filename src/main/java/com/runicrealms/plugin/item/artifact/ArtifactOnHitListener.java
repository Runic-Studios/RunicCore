package com.runicrealms.plugin.item.artifact;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.item.artifact.event.RunicArtifactOnHitEvent;
import com.runicrealms.plugin.runicitems.item.util.RunicArtifactAbilityTrigger;
import com.runicrealms.plugin.runicitems.util.ArtifactUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class ArtifactOnHitListener implements Listener {

    public ArtifactOnHitListener() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWeaponDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isBasicAttack()) return;
        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
        ArtifactUtil.ArtifactWithTrigger artifactWithTrigger = ArtifactUtil.checkForArtifactTrigger
                (
                        itemStack,
                        RunicArtifactAbilityTrigger.ON_HIT
                );
        if (!artifactWithTrigger.isTrigger()) return;
        Bukkit.getPluginManager().callEvent(new RunicArtifactOnHitEvent
                (
                        event.getPlayer(),
                        artifactWithTrigger.getRunicItemArtifact(),
                        itemStack,
                        RunicArtifactAbilityTrigger.ON_HIT,
                        null,
                        event.getVictim()
                )
        );
    }
}
