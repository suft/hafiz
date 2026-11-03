package suft.android.hafiz.ui.i18n

import suft.android.hafiz.domain.AppLanguage
import suft.android.hafiz.domain.GameMode
import suft.android.hafiz.domain.GameStyle
import suft.android.hafiz.domain.MushafEdition

object SpanishStrings : Strings {
    override val language = AppLanguage.Spanish

    override fun percent(value: Int) = "$value\u00A0%"

    override val appName = "Hafiz"
    override val back = "Atrás"
    override val loadingQuran = "Cargando el Corán…"
    override val tagline = "Pon a prueba tu memorización del Sagrado Corán"
    override fun styleTitle(style: GameStyle) = when (style) {
        GameStyle.GuessSurah -> "Adivina la sura"
        GameStyle.NextAyah -> "Siguiente aleya"
        GameStyle.AyahPosition -> "¿Dónde está la aleya?"
    }

    override fun styleSubtitle(style: GameStyle) = when (style) {
        GameStyle.GuessSurah -> "Lee la aleya y elige su sura"
        GameStyle.NextAyah -> "Elige la aleya que sigue"
        GameStyle.AyahPosition -> "Lee la aleya y elige dónde empieza en el mushaf de Medina"
    }

    override fun gameModeTitle(mode: GameMode) = when (mode) {
        GameMode.Regular -> "Normal"
        GameMode.Survival -> "Supervivencia"
        GameMode.Mutashabihat -> "Mutashabihat"
    }

    override fun gameModeSubtitle(mode: GameMode) = when (mode) {
        GameMode.Regular -> "Rondas de duración fija; domina todos los niveles"
        GameMode.Survival -> "Preguntas sin fin; un error termina la partida"
        GameMode.Mutashabihat -> "Preguntas de todo el Corán con trampas de aleyas parecidas (mutashabihat); tres errores terminan la partida"
    }

    override val chooseStyle = "Elige un tipo de pregunta"
    override val best = "mejor"
    override fun questionNumber(n: Int) = "Pregunta $n"
    override fun livesLeft(n: Int) = "Vidas restantes: $n"
    override val gameOver = "Fin de la partida"
    override fun survivalResult(score: Int) = "Aciertos seguidos: $score"
    override fun livesResult(score: Int, answered: Int) = "$score aciertos de ${questionsCount(answered)}"
    override fun longestStreak(n: Int) = "Racha más larga: $n"
    override fun bestScore(n: Int) = "Mejor puntuación: $n"
    override fun gamesPlayed(n: Int) = "Partidas jugadas: $n"
    override fun averageScore(n: Int) = "Puntuación media: $n"
    override val noGamesYet = "Aún no hay partidas"
    override fun shareRunText(score: Int, levelName: String, modeTitle: String, styleTitle: String) =
        "He logrado $score en el modo $modeTitle, nivel «$levelName», del quiz del Corán «$appName – $styleTitle»"

    override val mastered = "dominado"
    override val statistics = "Estadísticas"
    override val statisticsHint = "Mira qué suras necesitan más repaso"
    override val play = "Jugar"
    override val playHint = "Elige un modo de juego, un tipo de pregunta y un nivel"
    override val gameModes = "Modos de juego"
    override val howToPlay = "Cómo jugar"
    override val howToPlayHint = "Conoce los tipos de pregunta, los modos de juego, los niveles y los ajustes"
    override val guideStylesHeading = "Tipos de pregunta"
    override val guideLevelsHeading = "Niveles y dominio"
    override val guideLevels =
        "Cada tipo de pregunta tiene niveles que abarcan distintas partes del Corán, desde las suras cortas hasta el Corán completo. " +
            "El modo Normal guarda tu mejor ronda en cada nivel y Supervivencia tu mejor partida en cada nivel. " +
            "Mutashabihat no tiene niveles: sus preguntas proceden de todo el Corán."
    override fun guideMastery(styleTitle: String, questions: Int, mastery: Int) =
        "$styleTitle: una ronda normal tiene ${questionsCount(questions)}; una puntuación de ${number(mastery)} o más domina el nivel."
    override val guideStatsHeading = "Estadísticas y repaso"
    override val guideStats = "Las estadísticas muestran tu precisión y las suras que necesitan repaso; desde ahí puedes iniciar una ronda de repaso de una sola sura."
    override val guideSettings
        get() = "El engranaje de la parte superior del menú principal cambia el idioma de la app y el mushaf que usa " +
            "${styleTitle(GameStyle.AyahPosition)}: la ${mushafEditionName(MushafEdition.V1)}, la predeterminada, o la " +
            "${mushafEditionName(MushafEdition.V2)}. Algunas aleyas empiezan en un lugar distinto en cada edición; " +
            "las puntuaciones se comparten entre ambas."
    override fun statisticsSummary(percent: Int, total: Int) =
        "Precisión ${percent(percent)} en ${questionsCount(total)}"

    override val languageLabel = "Idioma"
    override val settings = "Ajustes"
    override val mushafEditionLabel get() = "Mushaf para «${styleTitle(GameStyle.AyahPosition)}»"

    override fun mushafEditionName(edition: MushafEdition) = when (edition) {
        MushafEdition.V1 -> "Edición de ${number(edition.hijriYear)} H (v1)"
        MushafEdition.V2 -> "Edición de ${number(edition.hijriYear)} H (v2)"
    }
    override val credits = "Créditos"
    override val creditsQuranText = "Texto del Corán"
    override val creditsRecitations = "Recitaciones"
    override val creditsMutashabihat = "Mutashabihat"
    override val creditsMushafLayout = "Posiciones de las aleyas en el mushaf"
    override val creditsTanzil = "Tanzil Project"
    override val creditsAswaatulQurraa = "Aswaatul Qurraa"
    override val creditsDeveloper = "Desarrollador"
    override val developerName = "Sufien Tout"
    override val creditsInspiredBy = "Inspirado en"
    override val privacyPolicy = "Política de privacidad"
    override val creditsSourcesHeading = "Fuentes"
    override val creditsPeopleHeading = "Personas"
    override val creditsLegalHeading = "Legal"
    override val creditsTanzilLicence = "CC BY 3.0, sin modificar"
    override val creditsMushafEditions get() = "Ediciones de ${number(MushafEdition.V1.hijriYear)} H y ${number(MushafEdition.V2.hijriYear)} H"
    override val creditsIcons = "Iconos"
    override val creditsIconsLicence = "Licencia MIT"
    override val opensInBrowser = "Se abre en el navegador"
    override fun versionLabel(version: String) = "Versión $version"
    override fun totalPoints(points: Int) = "Puntos totales: $points"
    override fun masteredCount(mastered: Int, total: Int) = "$mastered de $total dominados"
    override val start = "Empezar"
    override fun questionProgress(current: Int, total: Int) = "Pregunta $current de $total"
    override val next = "Siguiente"
    override fun points(score: Int) = "Puntos: $score"
    override val whichSurah = "¿De qué sura es esta aleya?"
    override val whatIsNextAyah = "¿Cuál es la siguiente aleya?"
    override val whereIsAyah = "¿Dónde empieza esta aleya en el mushaf?"
    override fun optionMarker(index: Int) = listOf("A", "B", "C", "D")[index]
    override val pause = "Pausa"
    override val listen = "Escuchar"
    override val chooseReciter = "Elegir recitador"
    override val autoPlay = "Auto"
    override val correctAnswer = "Respuesta correcta"
    override val wrongAnswer = "Respuesta incorrecta"
    override val resultTitle = "Resultado"
    override val perfectScore = "¡Masha'Allah! Puntuación perfecta"
    override val levelMastered = "¡Bien hecho! Has dominado este nivel"
    override val keepReviewing = "Bien, sigue repasando para dominarlo"
    override val needsReview = "Este nivel necesita más repaso"
    override val surahReviewMastered = "¡Bien hecho! Conoces bien esta sura"
    override val surahNeedsReview = "Esta sura necesita más repaso"
    override fun newRecord(previous: Int) = "¡Nuevo récord! (anterior: $previous)"
    override val shareResult = "Compartir resultado"
    override val retry = "Reintentar"
    override val mainMenu = "Menú principal"
    override fun shareScoreText(score: Int, total: Int, levelName: String, styleTitle: String) =
        "He logrado $score de $total en el nivel «$levelName» del quiz del Corán «$appName – $styleTitle»"

    override val playbackFailed = "No se pudo cargar la recitación. Comprueba tu conexión a internet."
    override val playbackFailedAutoPlayOff =
        "No se pudo cargar la recitación, así que se desactivó la reproducción automática. Comprueba tu conexión a internet."
    override val clearStatistics = "Borrar estadísticas"
    override val clearStatisticsQuestion = "¿Borrar las estadísticas?"
    override val clearStatisticsBody =
        "Se eliminan el historial de respuestas de cada sura, la precisión por tipo de pregunta, las rachas de días y de aciertos, " +
            "el historial de rondas, y las partidas jugadas, las medias y las rachas más largas de Supervivencia y Mutashabihat. " +
            "Se conserva tu mejor puntuación en cada nivel."
    override val clear = "Borrar"
    override val cancel = "Cancelar"
    override val statisticsEmpty = "Responde algunas preguntas para ver aquí las suras que necesitan repaso."
    override val noMistakes = "¡Masha'Allah! Ningún error hasta ahora."
    override val surahsToReview = "Suras que necesitan repaso"
    override val accuracy = "Precisión"
    override fun correctAnswers(n: Int) = "Respuestas correctas: $n"
    override fun wrongAnswers(n: Int) = "Respuestas incorrectas: $n"
    override fun rankedSurah(rank: Int, name: String) = "$rank. Sura $name"
    override fun surahCounts(correct: Int, wrong: Int) = "$correct correctas · $wrong incorrectas"
    override val tabOverview = "Resumen"
    override val tabSurahs = "Suras"
    override val tabActivity = "Actividad"
    override val noAnswersYet = "Aún no hay respuestas"
    override val styleAccuracySinceUpdate = "La precisión por tipo cuenta las respuestas dadas desde esta actualización; las anteriores solo figuran en la precisión global."
    override val surahMap = "Mapa de suras"
    override fun surahsPractised(n: Int, total: Int) = "$n de $total suras practicadas"
    override val tierNotPractised = "Sin practicar"
    override val tierWeak = "Necesita repaso"
    override val tierFair = "Bien"
    override val tierStrong = "Sólida"
    override val strongestSurahs = "Suras más sólidas"
    override val practice = "Practicar"
    override val notPractisedYet = "Aún sin practicar"
    override val currentStreak = "Racha de días actual"
    override val bestStreak = "Mejor racha de días"
    override val questionsToday = "Preguntas de hoy"
    override val roundsCompleted = "Rondas completadas"
    override val currentRun = "Aciertos seguidos"
    override val bestRun = "Mejor racha de aciertos"
    override val scoreTrend = "Evolución de la puntuación"
    override val recentRounds = "Rondas terminadas"
    override val noRoundsYet = "Termina una ronda para ver aquí tu historial."
    override fun daysAgo(days: Int) = when (days) {
        0 -> "Hoy"
        1 -> "Ayer"
        else -> "Hace $days días"
    }
    override fun questionsCount(n: Int) = if (n == 1) "1 pregunta" else "$n preguntas"
    override fun surahCell(name: String, tier: String, percent: Int?) =
        "Sura $name, $tier" + (percent?.let { ", ${percent(it)}" } ?: "")
    override fun scoreTrendDescription(first: Int, last: Int) =
        "Evolución de la puntuación: del ${percent(first)} en la ronda más antigua al ${percent(last)} en la más reciente"
    override val notMastered = "no dominado"
    override val levelsList = "Niveles"
    override val revelationTitle = "Mequí o Mediní"
    override val revelationSubtitle = "Cinco suras: indica si cada una fue revelada en La Meca o en Medina"
    override val revelationQuestion = "¿Dónde fue revelada esta sura?"
    override fun revelationSurah(name: String) = "Sura $name"
    override val makki = "Mequí"
    override val madani = "Mediní"
    override val revelationGood = "¡Bien hecho! Sigue para lograr la puntuación perfecta"
    override val revelationNeedsReview = "Repasa dónde fueron reveladas las suras e inténtalo de nuevo"
}
