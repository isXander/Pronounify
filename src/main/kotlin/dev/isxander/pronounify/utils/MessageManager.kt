package dev.isxander.pronounify.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*

object MessageManager {
    private val client = MinecraftClient.getInstance()

    fun getChatMessageWithPronoun(message: Text, pronouns: Pronouns): Text {
        if (pronouns == Pronouns.UNSPECIFIED)
            return message

        return Text.empty().append(pronouns.getText().formatted(Formatting.DARK_GRAY)).append(" ").append(message)
    }

    fun getTooltipTextWithPronoun(name: Text, pronouns: Pronouns): Text {
        if (pronouns == Pronouns.UNSPECIFIED)
            return name

        return Text.empty().apply {
            append(name)
            append(Text.empty().apply {
                append(" (")
                append(pronouns.getText())
                append(")")
                formatted(Formatting.DARK_GRAY)
            })
        }
    }

    fun isMessageSentByPlayer(sender: UUID): Boolean {
        if (client.player == null)
            return false

        return client.networkHandler?.playerUuids?.contains(sender) ?: false
    }
}
