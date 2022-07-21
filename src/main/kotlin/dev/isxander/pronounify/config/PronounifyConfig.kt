package dev.isxander.pronounify.config

import dev.isxander.settxi.gui.clothGui
import dev.isxander.settxi.impl.boolean
import dev.isxander.settxi.serialization.SettxiConfigKotlinx
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

object PronounifyConfig : SettxiConfigKotlinx(FabricLoader.getInstance().configDir.resolve("pronounify.json")) {
    private const val visibility = "pronounify.config.category.visibility"

    var showInChat by boolean(false) {
        name = "pronounify.config.showInChat.name"
        category = visibility
        description =  "pronounify.config.showInChat.description"
    }

    var showOnNametag by boolean(true) {
        name = "pronounify.config.showOnNametag.name"
        category = visibility
        description =  "pronounify.config.showOnNametag.description"
    }

    var showInTooltip by boolean(true) {
        name = "pronounify.config.showInTooltip.name"
        category = visibility
        description =  "pronounify.config.showInTooltip.description"
    }

    var showInSocialScreen by boolean(true) {
        name = "pronounify.config.showInSocialScreen.name"
        category = visibility
        description =  "pronounify.config.showInSocialScreen.description"
    }


    init {
        import()
    }

    fun gui(parent: Screen? = null) =
        this.clothGui(Text.translatable("pronounify.config.title"), parent)
}
