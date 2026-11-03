package suft.android.hafiz.ui.i18n

import suft.android.hafiz.domain.AppLanguage
import suft.android.hafiz.domain.GameMode
import suft.android.hafiz.domain.GameStyle
import suft.android.hafiz.domain.MushafEdition

object GermanStrings : Strings {
    override val language = AppLanguage.German

    override fun percent(value: Int) = "$value\u00A0%"

    override val appName = "Hafiz"
    override val back = "Zurück"
    override val loadingQuran = "Koran wird geladen …"
    override val tagline = "Teste, wie gut du den Heiligen Koran auswendig kannst"
    override fun styleTitle(style: GameStyle) = when (style) {
        GameStyle.GuessSurah -> "Errate die Sure"
        GameStyle.NextAyah -> "Nächster Vers"
        GameStyle.AyahPosition -> "Wo steht der Vers?"
    }

    override fun styleSubtitle(style: GameStyle) = when (style) {
        GameStyle.GuessSurah -> "Lies den Vers und wähle seine Sure"
        GameStyle.NextAyah -> "Wähle den Vers, der als Nächstes kommt"
        GameStyle.AyahPosition -> "Lies den Vers und wähle, wo er im Medina-Mushaf beginnt"
    }

    override fun gameModeTitle(mode: GameMode) = when (mode) {
        GameMode.Regular -> "Normal"
        GameMode.Survival -> "Überleben"
        GameMode.Mutashabihat -> "Mutashabihat"
    }

    override fun gameModeSubtitle(mode: GameMode) = when (mode) {
        GameMode.Regular -> "Runden fester Länge; meistere jede Stufe"
        GameMode.Survival -> "Endlose Fragen; ein Fehler beendet das Spiel"
        GameMode.Mutashabihat ->
            "Fragen aus dem ganzen Koran mit ähnlichen Versen als Fallen (Mutashabihat); drei Fehler beenden das Spiel"
    }

    override val chooseStyle = "Wähle eine Fragenart"
    override val best = "Bestwert"
    override fun questionNumber(n: Int) = "Frage $n"
    override fun livesLeft(n: Int) = "Verbleibende Leben: $n"
    override val gameOver = "Spiel vorbei"
    override fun survivalResult(score: Int) = "Richtig in Folge: $score"
    override fun livesResult(score: Int, answered: Int) = "$score richtig von ${questionsCount(answered)}"
    override fun longestStreak(n: Int) = "Längste Serie: $n"
    override fun bestScore(n: Int) = "Bestes Ergebnis: $n"
    override fun gamesPlayed(n: Int) = "Gespielte Spiele: $n"
    override fun averageScore(n: Int) = "Durchschnittliches Ergebnis: $n"
    override val noGamesYet = "Noch keine Spiele"
    override fun shareRunText(score: Int, levelName: String, modeTitle: String, styleTitle: String) =
        "Ich habe $score Punkte im Modus „$modeTitle“ auf der Stufe „$levelName“ " +
            "im Koran-Quiz „$appName – $styleTitle“ erreicht"

    override val mastered = "gemeistert"
    override val statistics = "Statistik"
    override val statisticsHint = "Sieh, welche Suren mehr Wiederholung brauchen"
    override val play = "Spielen"
    override val playHint = "Wähle einen Spielmodus, dann eine Fragenart und eine Stufe"
    override val gameModes = "Spielmodi"
    override val howToPlay = "Anleitung"
    override val howToPlayHint = "Lerne Fragenarten, Spielmodi, Stufen und Einstellungen kennen"
    override val guideStylesHeading = "Fragenarten"
    override val guideLevelsHeading = "Stufen und Meisterschaft"
    override val guideLevels =
        "Jede Fragenart hat Stufen, die verschiedene Teile des Korans abdecken, von den kurzen Suren bis zum ganzen Koran. " +
            "Normal speichert deine beste Runde je Stufe, Überleben dein bestes Spiel je Stufe. " +
            "Mutashabihat hat keine Stufen: Die Fragen stammen aus dem ganzen Koran."
    override fun guideMastery(styleTitle: String, questions: Int, mastery: Int) =
        "$styleTitle: Eine normale Runde hat ${questionsCount(questions)}; ab ${number(mastery)} Punkten ist die Stufe gemeistert."
    override val guideStatsHeading = "Statistik und Wiederholung"
    override val guideStats =
        "Die Statistik zeigt deine Trefferquote und die Suren, die Wiederholung brauchen; von dort startest du eine Wiederholungsrunde für eine einzelne Sure."
    override val guideSettings
        get() = "Das Zahnrad oben im Hauptmenü ändert die Sprache der App und den Mushaf für " +
            "${styleTitle(GameStyle.AyahPosition)}: ${mushafEditionName(MushafEdition.V1)} (Standard) oder " +
            "${mushafEditionName(MushafEdition.V2)}. Manche Verse beginnen in jedem Druck an einer anderen Stelle; " +
            "die Ergebnisse gelten für beide."
    override fun statisticsSummary(percent: Int, total: Int) =
        "Trefferquote ${percent(percent)} bei ${questionsCount(total)}"

    override val languageLabel = "Sprache"
    override val settings = "Einstellungen"
    override val mushafEditionLabel get() = "Mushaf für „${styleTitle(GameStyle.AyahPosition)}“"

    override fun mushafEditionName(edition: MushafEdition) = when (edition) {
        MushafEdition.V1 -> "Druck von ${number(edition.hijriYear)} n. H. (v1)"
        MushafEdition.V2 -> "Druck von ${number(edition.hijriYear)} n. H. (v2)"
    }
    override val credits = "Danksagung"
    override val creditsQuranText = "Koran-Text"
    override val creditsRecitations = "Rezitationen"
    override val creditsMutashabihat = "Mutashabihat"
    override val creditsMushafLayout = "Versposition im Mushaf"
    override val creditsTanzil = "Tanzil Project"
    override val creditsAswaatulQurraa = "Aswaatul Qurraa"
    override val creditsDeveloper = "Entwickler"
    override val developerName = "Sufien Tout"
    override val creditsInspiredBy = "Inspiriert von"
    override val privacyPolicy = "Datenschutzerklärung"
    override val creditsSourcesHeading = "Quellen"
    override val creditsPeopleHeading = "Personen"
    override val creditsLegalHeading = "Rechtliches"
    override val creditsTanzilLicence = "CC BY 3.0, unverändert verwendet"
    override val creditsMushafEditions
        get() = "Drucke von ${number(MushafEdition.V1.hijriYear)} und ${number(MushafEdition.V2.hijriYear)} n. H."
    override val creditsIcons = "Symbole"
    override val creditsIconsLicence = "MIT-Lizenz"
    override val opensInBrowser = "Öffnet sich im Browser"
    override fun versionLabel(version: String) = "Version $version"
    override fun totalPoints(points: Int) = "Punkte gesamt: $points"
    override fun masteredCount(mastered: Int, total: Int) = "$mastered von $total gemeistert"
    override val start = "Start"
    override fun questionProgress(current: Int, total: Int) = "Frage $current von $total"
    override val next = "Weiter"
    override fun points(score: Int) = "Punkte: $score"
    override val whichSurah = "Aus welcher Sure stammt dieser Vers?"
    override val whatIsNextAyah = "Welcher Vers kommt als Nächster?"
    override val whereIsAyah = "Wo beginnt dieser Vers im Mushaf?"
    override fun optionMarker(index: Int) = listOf("A", "B", "C", "D")[index]
    override val pause = "Pause"
    override val listen = "Anhören"
    override val chooseReciter = "Rezitator wählen"
    override val autoPlay = "Auto"
    override val correctAnswer = "Richtige Antwort"
    override val wrongAnswer = "Falsche Antwort"
    override val resultTitle = "Ergebnis"
    override val perfectScore = "Masha'Allah! Volle Punktzahl"
    override val levelMastered = "Gut gemacht! Du hast diese Stufe gemeistert"
    override val keepReviewing = "Gut, wiederhole weiter, um sie zu meistern"
    override val needsReview = "Diese Stufe braucht mehr Wiederholung"
    override val surahReviewMastered = "Gut gemacht! Du kennst diese Sure gut"
    override val surahNeedsReview = "Diese Sure braucht mehr Wiederholung"
    override fun newRecord(previous: Int) = "Neuer Rekord! (bisher: $previous)"
    override val shareResult = "Ergebnis teilen"
    override val retry = "Nochmal"
    override val mainMenu = "Hauptmenü"
    override fun shareScoreText(score: Int, total: Int, levelName: String, styleTitle: String) =
        "Ich habe $score von $total auf der Stufe „$levelName“ im Koran-Quiz „$appName – $styleTitle“ erreicht"

    override val playbackFailed = "Die Rezitation konnte nicht geladen werden. Prüfe deine Internetverbindung."
    override val playbackFailedAutoPlayOff =
        "Die Rezitation konnte nicht geladen werden, daher wurde die automatische Wiedergabe ausgeschaltet. " +
            "Prüfe deine Internetverbindung."
    override val clearStatistics = "Statistik löschen"
    override val clearStatisticsQuestion = "Statistik löschen?"
    override val clearStatisticsBody =
        "Dies löscht den Antwortverlauf jeder Sure, die Trefferquote je Fragenart, Tagesserien und Serien richtiger Antworten, " +
            "den Rundenverlauf sowie gespielte Spiele, Durchschnitte und längste Serien von Überleben und Mutashabihat. " +
            "Dein bestes Ergebnis je Stufe bleibt erhalten."
    override val clear = "Löschen"
    override val cancel = "Abbrechen"
    override val statisticsEmpty = "Beantworte einige Fragen, um hier die Suren zu sehen, die Wiederholung brauchen."
    override val noMistakes = "Masha'Allah! Bisher keine Fehler."
    override val surahsToReview = "Suren mit Wiederholungsbedarf"
    override val accuracy = "Trefferquote"
    override fun correctAnswers(n: Int) = "Richtige Antworten: $n"
    override fun wrongAnswers(n: Int) = "Falsche Antworten: $n"
    override fun rankedSurah(rank: Int, name: String) = "$rank. Sure $name"
    override fun surahCounts(correct: Int, wrong: Int) = "$correct richtig · $wrong falsch"
    override val tabOverview = "Übersicht"
    override val tabSurahs = "Suren"
    override val tabActivity = "Aktivität"
    override val noAnswersYet = "Noch keine Antworten"
    override val styleAccuracySinceUpdate =
        "Die Trefferquote je Fragenart zählt Antworten seit diesem Update; ältere Antworten fließen nur in die Gesamtquote ein."
    override val surahMap = "Surenkarte"
    override fun surahsPractised(n: Int, total: Int) = "$n von $total Suren geübt"
    override val tierNotPractised = "Nicht geübt"
    override val tierWeak = "Wiederholen"
    override val tierFair = "Gut"
    override val tierStrong = "Stark"
    override val strongestSurahs = "Stärkste Suren"
    override val practice = "Üben"
    override val notPractisedYet = "Noch nicht geübt"
    override val currentStreak = "Aktuelle Tagesserie"
    override val bestStreak = "Beste Tagesserie"
    override val questionsToday = "Fragen heute"
    override val roundsCompleted = "Abgeschlossene Runden"
    override val currentRun = "Richtig in Folge"
    override val bestRun = "Beste Serie richtiger Antworten"
    override val scoreTrend = "Ergebnisverlauf"
    override val recentRounds = "Beendete Runden"
    override val noRoundsYet = "Beende eine Runde, um hier deinen Verlauf zu sehen."
    override fun daysAgo(days: Int) = when (days) {
        0 -> "Heute"
        1 -> "Gestern"
        else -> "Vor $days Tagen"
    }
    override fun questionsCount(n: Int) = if (n == 1) "${number(n)} Frage" else "${number(n)} Fragen"
    override fun surahCell(name: String, tier: String, percent: Int?) =
        "Sure $name, $tier" + (percent?.let { ", ${percent(it)}" } ?: "")
    override fun scoreTrendDescription(first: Int, last: Int) =
        "Ergebnisverlauf: von ${percent(first)} in der ältesten Runde auf ${percent(last)} in der neuesten"
    override val notMastered = "nicht gemeistert"
    override val levelsList = "Stufen"
    override val revelationTitle = "Mekkanisch oder Medinensisch"
    override val revelationSubtitle = "Fünf Suren: Sag, ob jede in Mekka oder Medina offenbart wurde"
    override val revelationQuestion = "Wo wurde diese Sure offenbart?"
    override fun revelationSurah(name: String) = "Sure $name"
    override val makki = "Mekkanisch"
    override val madani = "Medinensisch"
    override val revelationGood = "Gut gemacht! Weiter so für die volle Punktzahl"
    override val revelationNeedsReview = "Wiederhole, wo die Suren offenbart wurden, und versuche es erneut"
}
