package dev.isxander.pronounmc.mixins;

import dev.isxander.pronounify.config.PronounifyConfig;
import dev.isxander.pronounify.utils.MessageManager;
import dev.isxander.pronounify.utils.PronounManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(SocialInteractionsPlayerListEntry.class)
public class SocialInteractionsPlayerListEntryMixin {
    @Shadow @Final private UUID uuid;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/client/util/math/MatrixStack;Ljava/lang/String;FFI)I"))
    private int replaceText(TextRenderer textRenderer, MatrixStack matrices, String text, float x, float y, int color) {
        if (PronounifyConfig.INSTANCE.getShowInSocialScreen()) {
            var pronounManager = PronounManager.INSTANCE;
            if (pronounManager.isPronounCached(this.uuid)) {
                Text name = MessageManager.INSTANCE.getTextWithColoredPronoun(Text.of(text), pronounManager.getPronoun(this.uuid), Formatting.GRAY);
                return textRenderer.draw(matrices, name, x, y, color);
            } else if (!pronounManager.isCurrentlyFetching(this.uuid)) {
                // though all players on this screen are bulk cached, it doesn't hurt making sure...
                pronounManager.cachePronoun(this.uuid);
            }
        }

        return textRenderer.draw(matrices, text, x, y, color);
    }
}
