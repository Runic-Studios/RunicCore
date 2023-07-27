package com.runicrealms.plugin.item.artifact;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.item.artifact.event.RunicArtifactOnCastEvent;
import com.runicrealms.plugin.runicitems.item.util.RunicArtifactAbilityTrigger;
import com.runicrealms.plugin.runicitems.util.ArtifactUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class ArtifactOnCastListener implements Listener {

    public ArtifactOnCastListener() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onSpellCast(SpellCastEvent e) {
        if (e.isCancelled()) return;
        ItemStack itemStack = e.getCaster().getInventory().getItemInMainHand();
        ArtifactUtil.ArtifactWithTrigger artifactWithTrigger = ArtifactUtil.checkForArtifactTrigger(itemStack, RunicArtifactAbilityTrigger.ON_CAST);
        if (!artifactWithTrigger.isTrigger()) return;
        Bukkit.getPluginManager().callEvent(new RunicArtifactOnCastEvent
                (
                        e.getCaster(),
                        artifactWithTrigger.getRunicItemArtifact(),
                        itemStack,
                        RunicArtifactAbilityTrigger.ON_CAST,
                        null,
                        e.getSpell()
                )
        );
    }
}
