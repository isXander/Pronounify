package dev.isxander.pronounmc.mixins;

import dev.isxander.pronounify.config.PronounifyConfig;
import dev.isxander.pronounify.utils.MessageManager;
import dev.isxander.pronounify.utils.PronounManager;
import kotlin.Unit;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;

@Mixin(MessageHandler.class)
public abstract class MessageHandlerMixin {
    @Shadow protected abstract boolean processChatMessageInternal(MessageType.Parameters params, SignedMessage message, Text decorated, @Nullable PlayerListEntry senderEntry, boolean onlyShowSecureChat, Instant receptionTimestamp);

    @Inject(method = "processChatMessageInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/message/MessageTrustStatus;createIndicator(Lnet/minecraft/network/message/SignedMessage;)Lnet/minecraft/client/gui/hud/MessageIndicator;"), cancellable = true)
    private void delayProcessingIfCaching(MessageType.Parameters params, SignedMessage message, Text decorated, PlayerListEntry senderEntry, boolean onlyShowSecureChat, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> cir) {
        var sender = senderEntry.getProfile().getId();
        var pronounManager = PronounManager.INSTANCE;
        if (!pronounManager.isPronounCached(sender) && PronounifyConfig.INSTANCE.getShowInChat()) {
            if (!pronounManager.isCurrentlyFetching(sender)) {
                pronounManager.cachePronoun(sender, (pronouns) -> {
                    processChatMessageInternal(params, message, decorated, senderEntry, onlyShowSecureChat, receptionTimestamp);
                    return Unit.INSTANCE;
                });
            }

            cir.setReturnValue(true);
        }
    }

    @ModifyVariable(method = "processChatMessageInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/message/MessageTrustStatus;createIndicator(Lnet/minecraft/network/message/SignedMessage;)Lnet/minecraft/client/gui/hud/MessageIndicator;"), argsOnly = true)
    private Text decorateTextWithPronouns(Text decorated, MessageType.Parameters params, SignedMessage message, Text dontuse, PlayerListEntry senderEntry) {
        var sender = senderEntry.getProfile().getId();
        var pronounManager = PronounManager.INSTANCE;
        if (pronounManager.isPronounCached(sender) && PronounifyConfig.INSTANCE.getShowInChat()) {
            return MessageManager.INSTANCE.getChatMessageWithPronoun(decorated, pronounManager.getPronoun(sender));
        }
        return decorated;
    }
}
