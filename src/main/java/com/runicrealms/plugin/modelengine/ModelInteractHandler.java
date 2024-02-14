package com.runicrealms.plugin.modelengine;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.event.ModelInteractEvent;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import org.bukkit.Bukkit;

public class ModelInteractHandler {

    public ModelInteractHandler() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(RunicCore.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.USE_ENTITY) {
                    PacketContainer packet = event.getPacket();
                    WrappedEnumEntityUseAction useAction = packet.getEnumEntityUseActions().readSafely(0);
                    EnumWrappers.EntityUseAction action = useAction.getAction();

                    if (action == EnumWrappers.EntityUseAction.INTERACT_AT
                            || action == EnumWrappers.EntityUseAction.ATTACK) {
                        if (action == EnumWrappers.EntityUseAction.INTERACT_AT && useAction.getHand() == EnumWrappers.Hand.OFF_HAND)
                            return;

                        // Grab ID of clientside entity
                        int entityID = packet.getIntegers().read(0);

                        // Find matching active model
                        final ActiveModel activeModel = ModelEngineAPI.getInteractionTracker().getModelRelay(entityID);
                        if (activeModel == null) return;
                        if (activeModel.getModeledEntity() == null) return;
                        if (activeModel.getModeledEntity().getBase() == null) return;

                        ModelInteractEvent.InteractType interactType = action == EnumWrappers.EntityUseAction.INTERACT_AT
                                ? ModelInteractEvent.InteractType.RIGHT_CLICK
                                : ModelInteractEvent.InteractType.LEFT_CLICK;

                        // Find corresponding serverside entity
                        Bukkit.getPluginManager().callEvent(new ModelInteractEvent(event.getPlayer(), interactType, activeModel));
                    }
                }
            }
        });
    }

}
