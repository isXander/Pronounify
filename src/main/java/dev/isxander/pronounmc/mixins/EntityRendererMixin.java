package dev.isxander.pronounmc.mixins;

import dev.isxander.pronounify.config.PronounifyConfig;
import dev.isxander.pronounify.utils.MessageManager;
import dev.isxander.pronounify.utils.PronounManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {
    @ModifyVariable(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;getTextRenderer()Lnet/minecraft/client/font/TextRenderer;"), argsOnly = true)
    private Text modifyNametag(Text text, T entity) {
        if (PronounifyConfig.INSTANCE.getShowOnNametag() && entity instanceof PlayerEntity player) {
            var uuid = player.getGameProfile().getId();
            var pronounManager = PronounManager.INSTANCE;
            var messageManager = MessageManager.INSTANCE;

            if (pronounManager.isPronounCached(uuid)) {
                return messageManager.getTooltipTextWithPronoun(text, pronounManager.getPronoun(uuid));
            } else if (!pronounManager.isCurrentlyFetching(uuid)) {
                pronounManager.cachePronoun(uuid);
            }
        }

        return text;
    }
}
