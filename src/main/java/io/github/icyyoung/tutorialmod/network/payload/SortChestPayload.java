package io.github.icyyoung.tutorialmod.network.payload;

import io.github.icyyoung.tutorialmod.TutorialMod;
import io.github.icyyoung.tutorialmod.util.ChestSortHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SortChestPayload(int menuId) implements CustomPacketPayload {
    public static final Type<SortChestPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "sort_chest"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SortChestPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, SortChestPayload::menuId, SortChestPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SortChestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (!(player.containerMenu instanceof ChestMenu chestMenu)) {
                return;
            }
            if (player.containerMenu.containerId != payload.menuId()) {
                return;
            }
            if (!chestMenu.stillValid(player)) {
                return;
            }

            ChestSortHelper.sortChest(chestMenu);
        });
    }
}
