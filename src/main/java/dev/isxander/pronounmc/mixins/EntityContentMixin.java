package dev.isxander.pronounmc.mixins;

import dev.isxander.pronounify.config.PronounifyConfig;
import dev.isxander.pronounify.utils.MessageManager;
import dev.isxander.pronounify.utils.PronounManager;
import kotlin.Unit;
import net.minecraft.entity.EntityType;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;

@Mixin(HoverEvent.EntityContent.class)
public class EntityContentMixin {
    @Shadow @Final public EntityType<?> entityType;

    @Shadow @Final public UUID uuid;

    @Shadow @Final @Mutable
    @Nullable public Text name;

    @Shadow private @Nullable List<Text> tooltip;

    @Inject(method = "asTooltip", at = @At("HEAD"))
    private void modifyTooltip(CallbackInfoReturnable<List<Text>> cir) {
        if (!entityType.equals(EntityType.PLAYER))
            return;

        if (PronounifyConfig.INSTANCE.getShowInTooltip()) {
            var pronounManager = PronounManager.INSTANCE;
            if (pronounManager.isPronounCached(uuid)) {
                var pronouns = pronounManager.getPronoun(uuid);

                name = MessageManager.INSTANCE.getTextWithColoredPronoun(name, pronouns, Formatting.DARK_GRAY);
            } else if (!pronounManager.isCurrentlyFetching(uuid)) {
                pronounManager.cachePronoun(uuid, (pronouns) -> {
                    tooltip = null;
                    return Unit.INSTANCE;
                });
            }
        }
    }
}
