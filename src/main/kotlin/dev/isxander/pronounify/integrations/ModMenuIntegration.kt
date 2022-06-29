package dev.isxander.pronounify.integrations

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.isxander.pronounify.config.PronounifyConfig

object ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory{ parent ->
        PronounifyConfig.gui(parent)
    }
}
