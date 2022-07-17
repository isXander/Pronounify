package dev.isxander.pronounmc.mixins;

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
    private int onDrawName(TextRenderer instance, MatrixStack matrices, String text, float x, float y, int color) {
        var pronounManager = PronounManager.INSTANCE;
        if (!pronounManager.isPronounCached(this.uuid) || pronounManager.isCurrentlyFetching(this.uuid))
            return instance.draw(matrices, text, x, y, color);
        Text name = MessageManager.INSTANCE.getTextWithColoredPronoun(Text.of(text), pronounManager.getPronoun(this.uuid), Formatting.GRAY);
        return instance.draw(matrices, name, x, y, color);
    }
}
