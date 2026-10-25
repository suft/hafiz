package suft.android.hafiz.domain

import kotlinx.serialization.Serializable

/** UI language. The Quran text itself is always Arabic. */
@Serializable
enum class AppLanguage {
    Arabic, English;

    /** Arabic-Indic digits in Arabic, ASCII digits in English. */
    fun number(value: Int): String = if (this == Arabic) ArabicText.digits(value) else value.toString()
}
