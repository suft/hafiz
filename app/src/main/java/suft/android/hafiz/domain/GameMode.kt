package suft.android.hafiz.domain

/** How a game ends: after a fixed round (Regular) or when the lives run out (endless modes). */
enum class GameMode(
    val key: String,
    val lives: Int?,
    val trapOptions: Boolean = false,
    /** True when the mode has no level choice and always plays [Levels.wholeQuran]. */
    val wholeQuranOnly: Boolean = false,
) {
    Regular("regular", lives = null),
    Survival("survival", lives = 1),

    /** Three lives; the wrong options are mutashabihat — ayahs and surahs that resemble the right answer. Always the whole Quran: no level choice. */
    Mutashabihat("mutashabihat", lives = 3, trapOptions = true, wholeQuranOnly = true);

    val endless: Boolean get() = lives != null

    /** Survival's score is its streak, so only games with more lives show the longest streak separately. */
    val showsLongestStreak: Boolean get() = (lives ?: 0) > 1

    /** Mutashabihat mode needs look-alike wrong options, which a style without them ([GameStyle.hasLookAlikes]) cannot give. */
    fun offers(style: GameStyle): Boolean = !trapOptions || style.hasLookAlikes

    /** Regular: after [roundLength] answers. Endless: when [wrong] reaches [lives]. */
    fun isOver(answered: Int, wrong: Int, roundLength: Int): Boolean =
        if (lives == null) answered >= roundLength else wrong >= lives

    companion object {
        fun fromKey(key: String): GameMode = entries.first { it.key == key }
    }
}
