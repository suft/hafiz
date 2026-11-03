package suft.android.hafiz.domain

import kotlinx.serialization.Serializable

/** UI language. The Quran text itself is always Arabic. */
@Serializable
enum class AppLanguage(val tag: String) {
    Arabic("ar"), English("en"), German("de"), Spanish("es"), French("fr"), Italian("it"), Portuguese("pt");

    /** Arabic-Indic digits in Arabic, ASCII digits in every other language. */
    fun number(value: Int): String = if (this == Arabic) ArabicText.digits(value) else value.toString()
}
