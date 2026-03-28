package io.github.icyyoung.tutorialmod.network;

import io.github.icyyoung.tutorialmod.TutorialMod;
import io.github.icyyoung.tutorialmod.network.payload.SortChestPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModMessages {
    private ModMessages() {
    }

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(TutorialMod.MOD_ID);
        registrar.playToServer(SortChestPayload.TYPE, SortChestPayload.STREAM_CODEC, SortChestPayload::handle);
    }
}
