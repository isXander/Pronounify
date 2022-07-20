package dev.isxander.pronounmc.mixins;

import dev.isxander.pronounify.config.PronounifyConfig;
import dev.isxander.pronounify.utils.MessageManager;
import dev.isxander.pronounify.utils.PronounManager;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.UUID;

@Mixin(SocialInteractionsPlayerListEntry.class)
public class SocialInteractionsPlayerListEntryMixin {

    @Shadow @Final private UUID uuid;

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/client/util/math/MatrixStack;Ljava/lang/String;FFI)I"), index = 1)
    private String replaceText(String text) {
        var pronounManager = PronounManager.INSTANCE;
        if (!PronounifyConfig.INSTANCE.getShowInSocialScreen() || !pronounManager.isPronounCached(this.uuid) || pronounManager.isCurrentlyFetching(this.uuid))
            return text;
        Text name = MessageManager.INSTANCE.getTextWithColoredPronoun(Text.of(text), pronounManager.getPronoun(this.uuid), Formatting.GRAY);
        return name.getString();
    }
}
