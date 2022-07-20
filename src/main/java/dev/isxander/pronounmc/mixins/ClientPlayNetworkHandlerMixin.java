package dev.isxander.pronounmc.mixins;

import dev.isxander.pronounify.utils.MultithreadingKt;
import dev.isxander.pronounify.utils.PronounManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow @Final private MinecraftClient client;

    @Unique
    private boolean pronounify$waitingForChunkPacket = false;

    @Inject(method = "onGameJoin", at = @At("RETURN"))
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        pronounify$waitingForChunkPacket = true;
    }

    @Inject(method = "onPlayerList", at = @At("HEAD"))
    private void onPlayerList(PlayerListS2CPacket packet, CallbackInfo ci) {
        if (!pronounify$waitingForChunkPacket)
            return;

        pronounify$waitingForChunkPacket = false;

        //MultithreadingKt.scheduleAsync(1000, () -> PronounManager.INSTANCE.bulkCachePronouns(client.getNetworkHandler().getPlayerUuids()));
    }
}
