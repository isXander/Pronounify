package dev.isxander.pronounify.utils

import net.minecraft.text.Text

enum class Pronouns(val id: String) {
    UNSPECIFIED("unspecified"),
    HE_HIM("hh"),
    HE_IT("hi"),
    HE_SHE("hs"),
    HE_THEY("ht"),
    IT_HIM("ih"),
    IT_ITS("ii"),
    IT_SHE("is"),
    IT_THEY("it"),
    SHE_HE("shh"),
    SHE_HER("sh"),
    SHE_IT("si"),
    SHE_THEY("st"),
    THEY_HE("th"),
    THEY_IT("ti"),
    THEY_SHE("ts"),
    THEY_THEM("tt"),
    ANY("any"),
    OTHER("other"),
    ASK("ask"),
    AVOID("avoid");

    fun getText() = Text.translatable("pronounify.pronoun.$id")

    companion object {
        fun fromId(id: String) = values().find { it.id == id }
    }
}
