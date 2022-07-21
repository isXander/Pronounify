package dev.isxander.pronounmc.mixins;

import dev.isxander.pronounify.config.PronounifyConfig;
import dev.isxander.pronounify.utils.PronounManager;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.UUID;

@Mixin(SocialInteractionsPlayerListWidget.class)
public class SocialInteractionsPlayerListWidgetMixin {
    @Inject(method = "update", at = @At("HEAD"))
    private void onUsersUpdate(Collection<UUID> uuids, double scrollAmount, boolean includeOffline, CallbackInfo ci) {
        if (PronounifyConfig.INSTANCE.getShowInSocialScreen())
            PronounManager.INSTANCE.bulkCachePronouns(uuids);
    }
}
