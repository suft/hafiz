package suft.android.hafiz.ui.i18n

import suft.android.hafiz.domain.AppLanguage
import suft.android.hafiz.domain.GameMode
import suft.android.hafiz.domain.GameStyle
import suft.android.hafiz.domain.MushafEdition

object FrenchStrings : Strings {
    override val language = AppLanguage.French

    override fun percent(value: Int) = "$value\u202F%"

    override val appName = "Hafiz"
    override val back = "Retour"
    override val loadingQuran = "Chargement du Coran…"
    override val loadFailed = "Hafiz n’a pas pu charger ses données. Veuillez réinstaller l’application."
    override val tagline = "Testez votre mémorisation du saint Coran"
    override fun styleTitle(style: GameStyle) = when (style) {
        GameStyle.GuessSurah -> "Devinez la sourate"
        GameStyle.NextAyah -> "Verset suivant"
        GameStyle.AyahPosition -> "Où est le verset\u202F?"
    }

    override fun styleSubtitle(style: GameStyle) = when (style) {
        GameStyle.GuessSurah -> "Lisez le verset et choisissez sa sourate"
        GameStyle.NextAyah -> "Choisissez le verset qui suit"
        GameStyle.AyahPosition -> "Lisez le verset et choisissez où il commence dans le mushaf de Médine"
    }

    override fun gameModeTitle(mode: GameMode) = when (mode) {
        GameMode.Regular -> "Classique"
        GameMode.Survival -> "Survie"
        GameMode.Mutashabihat -> "Mutashabihat"
    }

    override fun gameModeSubtitle(mode: GameMode) = when (mode) {
        GameMode.Regular -> "Manches de longueur fixe\u00A0; maîtrisez chaque niveau"
        GameMode.Survival -> "Questions sans fin\u00A0; une erreur et c’est fini"
        GameMode.Mutashabihat ->
            "Questions sur tout le Coran avec des pièges de versets similaires (mutashabihat)\u00A0; trois erreurs et c’est fini"
    }

    override val chooseStyle = "Choisissez un type de question"
    override val best = "record"
    override fun questionNumber(n: Int) = "Question $n"
    override fun livesLeft(n: Int) = "Vies restantes\u00A0: $n"
    override val gameOver = "Partie terminée"
    override fun survivalResult(score: Int) = "Bonnes réponses d’affilée\u00A0: $score"
    override fun livesResult(score: Int, answered: Int) =
        "$score ${if (score <= 1) "bonne réponse" else "bonnes réponses"} sur ${questionsCount(answered)}"
    override fun longestStreak(n: Int) = "Plus longue série\u00A0: $n"
    override fun bestScore(n: Int) = "Meilleur score\u00A0: $n"
    override fun gamesPlayed(n: Int) = "Parties jouées\u00A0: $n"
    override fun averageScore(n: Int) = "Score moyen\u00A0: $n"
    override val noGamesYet = "Aucune partie pour l’instant"
    override fun shareRunText(score: Int, levelName: String, modeTitle: String, styleTitle: String) =
        "J’ai obtenu $score en mode $modeTitle au niveau «\u00A0$levelName\u00A0» du quiz du Coran " +
            "«\u00A0$appName – $styleTitle\u00A0»"

    override val mastered = "maîtrisé"
    override val statistics = "Statistiques"
    override val statisticsHint = "Voyez quelles sourates demandent plus de révision"
    override val play = "Jouer"
    override val playHint = "Choisissez un mode de jeu, puis un type de question et un niveau"
    override val gameModes = "Modes de jeu"
    override val howToPlay = "Comment jouer"
    override val howToPlayHint = "Découvrez les types de questions, les modes de jeu, les niveaux et les paramètres"
    override val guideStylesHeading = "Types de questions"
    override val guideLevelsHeading = "Niveaux et maîtrise"
    override val guideLevels =
        "Chaque type de question propose des niveaux couvrant différentes parties du Coran, des sourates courtes au Coran entier. " +
            "Le mode Classique garde votre meilleure manche à chaque niveau et le mode Survie votre meilleure partie. " +
            "Mutashabihat n’a pas de niveaux\u00A0: ses questions portent sur tout le Coran."
    override fun guideMastery(styleTitle: String, questions: Int, mastery: Int) =
        "$styleTitle\u00A0: une manche Classique compte ${questionsCount(questions)}\u00A0; " +
            "un score de ${number(mastery)} ou plus maîtrise le niveau."
    override val guideStatsHeading = "Statistiques et révision"
    override val guideStats =
        "Les statistiques montrent votre précision et les sourates à réviser\u00A0; " +
            "lancez-y une manche de révision pour une seule sourate."
    override val guideSettings
        get() = "La roue dentée en haut du menu principal change la langue de l’application et le mushaf utilisé par " +
            "${styleTitle(GameStyle.AyahPosition)}\u00A0: l’${mushafEditionName(MushafEdition.V1)}, par défaut, ou l’" +
            "${mushafEditionName(MushafEdition.V2)}. Certains versets commencent à un endroit différent selon l’édition\u00A0; " +
            "les scores sont communs aux deux."
    override val guideRevelationSource =
        "Les réponses suivent le Mushaf de Médine, comme sur quran.com. Les savants divergent sur quelques sourates."
    override fun statisticsSummary(percent: Int, total: Int) =
        "Précision ${percent(percent)} sur ${questionsCount(total)}"

    override val languageLabel = "Langue"
    override val settings = "Paramètres"
    override val mushafEditionLabel get() = "Mushaf pour «\u00A0${styleTitle(GameStyle.AyahPosition)}\u00A0»"

    override fun mushafEditionName(edition: MushafEdition) = when (edition) {
        MushafEdition.V1 -> "Édition de ${number(edition.hijriYear)} H (v1)"
        MushafEdition.V2 -> "Édition de ${number(edition.hijriYear)} H (v2)"
    }
    override val credits = "Crédits"
    override val creditsQuranText = "Texte du Coran"
    override val creditsRecitations = "Récitations"
    override val creditsMutashabihat = "Mutashabihat"
    override val creditsMushafLayout = "Position des versets dans le mushaf"
    override val creditsTanzil = "Tanzil Project"
    override val creditsAswaatulQurraa = "Aswaatul Qurraa"
    override val creditsDeveloper = "Développeur"
    override val developerName = "Sufien Tout"
    override val creditsInspiredBy = "Inspiré par"
    override val privacyPolicy = "Politique de confidentialité"
    override val creditsSourcesHeading = "Sources"
    override val creditsPeopleHeading = "Personnes"
    override val creditsLegalHeading = "Mentions légales"
    override val creditsTanzilLicence =
        "CC BY 3.0, utilisé sans modification (les réponses proposées masquent le signe de prosternation ۩)"
    override val creditsMushafEditions
        get() = "Éditions de ${number(MushafEdition.V1.hijriYear)} H et ${number(MushafEdition.V2.hijriYear)} H"
    override val creditsIcons = "Icônes"
    override val creditsIconsLicence = "Licence MIT"
    override val opensInBrowser = "S’ouvre dans votre navigateur"
    override fun versionLabel(version: String) = "Version $version"
    override fun totalPoints(points: Int) = "Total de points\u00A0: $points"
    override fun masteredCount(mastered: Int, total: Int) = "$mastered sur $total maîtrisés"
    override val start = "Commencer"
    override fun questionProgress(current: Int, total: Int) = "Question $current sur $total"
    override val next = "Suivant"
    override fun points(score: Int) = "Points\u00A0: $score"
    override val whichSurah = "De quelle sourate vient ce verset\u202F?"
    override val whatIsNextAyah = "Quel est le verset suivant\u202F?"
    override val whereIsAyah = "Où commence ce verset dans le mushaf\u202F?"
    override fun optionMarker(index: Int) = listOf("A", "B", "C", "D")[index]
    override val pause = "Pause"
    override val listen = "Écouter"
    override val chooseReciter = "Choisir le récitateur"
    override val autoPlay = "Auto"
    override val correctAnswer = "Bonne réponse"
    override val wrongAnswer = "Mauvaise réponse"
    override val resultTitle = "Résultat"
    override val perfectScore = "Masha'Allah\u202F! Un score parfait"
    override val levelMastered = "Bravo\u202F! Vous avez maîtrisé ce niveau"
    override val keepReviewing = "Bien, continuez à réviser pour le maîtriser"
    override val needsReview = "Ce niveau demande plus de révision"
    override val surahReviewMastered = "Bravo\u202F! Vous connaissez bien cette sourate"
    override val surahNeedsReview = "Cette sourate demande plus de révision"
    override fun newRecord(previous: Int) = "Nouveau record\u202F! (précédent\u00A0: $previous)"
    override val shareResult = "Partager le résultat"
    override val retry = "Réessayer"
    override val mainMenu = "Menu principal"
    override fun shareScoreText(score: Int, total: Int, levelName: String, styleTitle: String) =
        "J’ai obtenu $score sur $total au niveau «\u00A0$levelName\u00A0» du quiz du Coran " +
            "«\u00A0$appName – $styleTitle\u00A0»"

    override val playbackFailed = "Impossible de charger la récitation. Vérifiez votre connexion internet."
    override val playbackFailedAutoPlayOff =
        "Impossible de charger la récitation\u00A0: la lecture automatique a été désactivée. " +
            "Vérifiez votre connexion internet."
    override val clearStatistics = "Effacer les statistiques"
    override val clearStatisticsQuestion = "Effacer les statistiques\u202F?"
    override val clearStatisticsBody =
        "Cela supprime l’historique des réponses de chaque sourate, la précision par type de question, " +
            "les séries de jours et de bonnes réponses, l’historique des manches, ainsi que les parties jouées, " +
            "les moyennes et les plus longues séries des modes Survie et Mutashabihat. " +
            "Votre meilleur score à chaque niveau est conservé."
    override val clear = "Effacer"
    override val cancel = "Annuler"
    override val statisticsEmpty = "Répondez à quelques questions pour voir ici les sourates à réviser."
    override val noMistakes = "Masha'Allah\u202F! Aucune erreur pour l’instant."
    override val surahsToReview = "Sourates à réviser"
    override val accuracy = "Précision"
    override fun correctAnswers(n: Int) = "Bonnes réponses\u00A0: $n"
    override fun wrongAnswers(n: Int) = "Mauvaises réponses\u00A0: $n"
    override fun rankedSurah(rank: Int, name: String) = "$rank. Sourate $name"
    override fun surahCounts(correct: Int, wrong: Int) =
        "$correct ${if (correct <= 1) "bonne" else "bonnes"} · $wrong ${if (wrong <= 1) "mauvaise" else "mauvaises"}"
    override val tabOverview = "Aperçu"
    override val tabSurahs = "Sourates"
    override val tabActivity = "Activité"
    override val noAnswersYet = "Aucune réponse pour l’instant"
    override val styleAccuracySinceUpdate =
        "La précision par type de question compte les réponses données depuis cette mise à jour\u00A0; " +
            "les plus anciennes ne figurent que dans la précision globale."
    override val surahMap = "Carte des sourates"
    override fun surahsPractised(n: Int, total: Int) =
        "$n ${if (n <= 1) "sourate travaillée" else "sourates travaillées"} sur $total"
    override val tierNotPractised = "Non travaillée"
    override val tierWeak = "À réviser"
    override val tierFair = "Bien"
    override val tierStrong = "Solide"
    override val strongestSurahs = "Sourates les plus solides"
    override val practice = "S’entraîner"
    override val notPractisedYet = "Pas encore travaillée"
    override val currentStreak = "Série de jours en cours"
    override val bestStreak = "Meilleure série de jours"
    override val questionsToday = "Questions aujourd’hui"
    override val roundsCompleted = "Manches terminées"
    override val currentRun = "Bonnes réponses d’affilée"
    override val bestRun = "Meilleure série de bonnes réponses"
    override val scoreTrend = "Évolution du score"
    override val recentRounds = "Manches terminées"
    override val noRoundsYet = "Terminez une manche pour voir votre historique ici."
    override fun daysAgo(days: Int) = when (days) {
        0 -> "Aujourd’hui"
        1 -> "Hier"
        else -> "Il y a $days jours"
    }
    override fun questionsCount(n: Int) = if (n <= 1) "${number(n)} question" else "${number(n)} questions"
    override fun surahCell(name: String, tier: String, percent: Int?) =
        "Sourate $name, $tier" + (percent?.let { ", ${percent(it)}" } ?: "")
    override fun scoreTrendDescription(first: Int, last: Int) =
        "Évolution du score\u00A0: de ${percent(first)} dans la plus ancienne manche à ${percent(last)} dans la plus récente"
    override val notMastered = "non maîtrisé"
    override val levelsList = "Niveaux"
    override val revelationTitle = "Mecquoise ou médinoise"
    override val revelationSubtitle = "Cinq sourates\u00A0: dites si chacune a été révélée à La Mecque ou à Médine"
    override val revelationQuestion = "Où cette sourate a-t-elle été révélée\u202F?"
    override fun revelationSurah(name: String) = "Sourate $name"
    override val makki = "Mecquoise"
    override val madani = "Médinoise"
    override val revelationGood = "Bravo\u202F! Continuez pour un score parfait"
    override val revelationNeedsReview = "Révisez où les sourates ont été révélées et réessayez"
}
