package dev.isxander.pronounmc.mixins;

import dev.isxander.pronounify.config.PronounifyConfig;
import dev.isxander.pronounify.utils.MessageManager;
import dev.isxander.pronounify.utils.PronounManager;
import kotlin.Unit;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.network.message.MessageSender;
import net.minecraft.network.message.MessageType;
import net.minecraft.text.Text;
import net.minecraft.util.registry.BuiltinRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow public abstract void onChatMessage(MessageType type, Text message, MessageSender sender);

    @ModifyVariable(method = "onChatMessage", at = @At("HEAD"), argsOnly = true)
    private Text modifyText(Text message, MessageType type, Text dontuse, MessageSender sender) {
        var messageManager = MessageManager.INSTANCE;
        var pronounManager = PronounManager.INSTANCE;

        if (type != null && type.chat().isPresent() && PronounifyConfig.INSTANCE.getShowInChat() && messageManager.isMessageSentByPlayer(sender.uuid())) {
            if (pronounManager.isPronounCached(sender.uuid())) {
                return messageManager.getChatMessageWithPronoun(message, pronounManager.getPronoun(sender.uuid()));
            } else if (!pronounManager.isCurrentlyFetching(sender.uuid())) {
                pronounManager.cachePronoun(sender.uuid(), (pronouns) -> {
                    onChatMessage(null, messageManager.getChatMessageWithPronoun(message, pronouns), sender);
                    return Unit.INSTANCE;
                });
            } else {
                pronounManager.listenToPronounGet(sender.uuid(), (pronouns) -> {
                    onChatMessage(null, messageManager.getChatMessageWithPronoun(message, pronouns), sender);
                    return Unit.INSTANCE;
                });
            }
        }

        return message;
    }

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    private void onAddChatMessage(MessageType type, Text message, MessageSender sender, CallbackInfo ci) {
        var messageManager = MessageManager.INSTANCE;
        var pronounManager = PronounManager.INSTANCE;

        if (!PronounifyConfig.INSTANCE.getShowInChat() || !messageManager.isMessageSentByPlayer(sender.uuid()) || type == null)
            return;

        if (!pronounManager.isPronounCached(sender.uuid()))
            ci.cancel();
    }

    @ModifyVariable(method = "onChatMessage", at = @At("HEAD"), argsOnly = true)
    private MessageType modifyType(MessageType type) {
        if (type == null)
            return BuiltinRegistries.MESSAGE_TYPE.get(MessageType.CHAT);
        return type;
    }
}
