package suft.android.hafiz.ui.i18n

import suft.android.hafiz.domain.AppLanguage
import suft.android.hafiz.domain.GameMode
import suft.android.hafiz.domain.GameStyle
import suft.android.hafiz.domain.MushafEdition

object ItalianStrings : Strings {
    override val language = AppLanguage.Italian

    override fun percent(value: Int) = "$value%"

    override val appName = "Hafiz"
    override val back = "Indietro"
    override val loadingQuran = "Caricamento del Corano…"
    override val tagline = "Metti alla prova la tua memorizzazione del Sacro Corano"
    override fun styleTitle(style: GameStyle) = when (style) {
        GameStyle.GuessSurah -> "Indovina la sura"
        GameStyle.NextAyah -> "Versetto successivo"
        GameStyle.AyahPosition -> "Dov’è il versetto?"
    }

    override fun styleSubtitle(style: GameStyle) = when (style) {
        GameStyle.GuessSurah -> "Leggi il versetto e scegli la sua sura"
        GameStyle.NextAyah -> "Scegli il versetto che segue"
        GameStyle.AyahPosition -> "Leggi il versetto e scegli dove inizia nel mushaf di Medina"
    }

    override fun gameModeTitle(mode: GameMode) = when (mode) {
        GameMode.Regular -> "Normale"
        GameMode.Survival -> "Sopravvivenza"
        GameMode.Mutashabihat -> "Mutashabihat"
    }

    override fun gameModeSubtitle(mode: GameMode) = when (mode) {
        GameMode.Regular -> "Round di lunghezza fissa; supera ogni livello"
        GameMode.Survival -> "Domande senza fine; un errore chiude la partita"
        GameMode.Mutashabihat -> "Domande su tutto il Corano con trappole di versetti simili (mutashabihat); tre errori chiudono la partita"
    }

    override val chooseStyle = "Scegli un tipo di domanda"
    override val best = "record"
    override fun questionNumber(n: Int) = "Domanda $n"
    override fun livesLeft(n: Int) = "Vite rimaste: $n"
    override val gameOver = "Partita finita"
    override fun survivalResult(score: Int) = "Risposte esatte di fila: $score"
    override fun livesResult(score: Int, answered: Int) = "$score esatte su ${questionsCount(answered)}"
    override fun longestStreak(n: Int) = "Serie più lunga: $n"
    override fun bestScore(n: Int) = "Punteggio migliore: $n"
    override fun gamesPlayed(n: Int) = "Partite giocate: $n"
    override fun averageScore(n: Int) = "Punteggio medio: $n"
    override val noGamesYet = "Ancora nessuna partita"
    override fun shareRunText(score: Int, levelName: String, modeTitle: String, styleTitle: String) =
        "Ho totalizzato $score in modalità $modeTitle al livello «$levelName» nel quiz sul Corano «$appName – $styleTitle»"

    override val mastered = "superato"
    override val statistics = "Statistiche"
    override val statisticsHint = "Scopri quali sure hanno bisogno di ripasso"
    override val play = "Gioca"
    override val playHint = "Scegli una modalità, poi un tipo di domanda e un livello"
    override val gameModes = "Modalità di gioco"
    override val howToPlay = "Come si gioca"
    override val howToPlayHint = "Scopri i tipi di domanda, le modalità, i livelli e le impostazioni"
    override val guideStylesHeading = "Tipi di domanda"
    override val guideLevelsHeading = "Livelli e padronanza"
    override val guideLevels =
        "Ogni tipo di domanda ha livelli che coprono parti diverse del Corano, dalle sure brevi a tutto il Corano. " +
            "Normale conserva il tuo round migliore di ogni livello e Sopravvivenza la tua partita migliore di ogni livello. " +
            "Mutashabihat non ha livelli: le sue domande provengono da tutto il Corano."
    override fun guideMastery(styleTitle: String, questions: Int, mastery: Int) =
        "$styleTitle: un round Normale ha ${questionsCount(questions)}; un punteggio di ${number(mastery)} o più supera il livello."
    override val guideStatsHeading = "Statistiche e ripasso"
    override val guideStats = "Le statistiche mostrano la tua precisione e le sure da ripassare; da lì puoi iniziare un round di ripasso per una singola sura."
    override val guideSettings
        get() = "L’ingranaggio in alto nel menu principale cambia la lingua dell’app e il mushaf usato da " +
            "${styleTitle(GameStyle.AyahPosition)}: la ${mushafEditionName(MushafEdition.V1)}, predefinita, o la " +
            "${mushafEditionName(MushafEdition.V2)}. Alcuni versetti iniziano in un punto diverso in ciascuna edizione; " +
            "i punteggi sono condivisi da entrambe."
    override fun statisticsSummary(percent: Int, total: Int) =
        "Precisione ${percent(percent)} su ${questionsCount(total)}"

    override val languageLabel = "Lingua"
    override val settings = "Impostazioni"
    override val mushafEditionLabel get() = "Mushaf per «${styleTitle(GameStyle.AyahPosition)}»"

    override fun mushafEditionName(edition: MushafEdition) = when (edition) {
        MushafEdition.V1 -> "Edizione del ${number(edition.hijriYear)} H (v1)"
        MushafEdition.V2 -> "Edizione del ${number(edition.hijriYear)} H (v2)"
    }
    override val credits = "Crediti"
    override val creditsQuranText = "Testo del Corano"
    override val creditsRecitations = "Recitazioni"
    override val creditsMutashabihat = "Mutashabihat"
    override val creditsMushafLayout = "Posizione dei versetti nel mushaf"
    override val creditsTanzil = "Tanzil Project"
    override val creditsAswaatulQurraa = "Aswaatul Qurraa"
    override val creditsDeveloper = "Sviluppatore"
    override val developerName = "Sufien Tout"
    override val creditsInspiredBy = "Ispirato da"
    override val privacyPolicy = "Informativa sulla privacy"
    override val creditsSourcesHeading = "Fonti"
    override val creditsPeopleHeading = "Persone"
    override val creditsLegalHeading = "Note legali"
    override val creditsTanzilLicence = "CC BY 3.0, usato senza modifiche"
    override val creditsMushafEditions get() = "Edizioni del ${number(MushafEdition.V1.hijriYear)} H e del ${number(MushafEdition.V2.hijriYear)} H"
    override val creditsIcons = "Icone"
    override val creditsIconsLicence = "Licenza MIT"
    override val opensInBrowser = "Si apre nel browser"
    override fun versionLabel(version: String) = "Versione $version"
    override fun totalPoints(points: Int) = "Punti totali: $points"
    override fun masteredCount(mastered: Int, total: Int) = "$mastered su $total superati"
    override val start = "Inizia"
    override fun questionProgress(current: Int, total: Int) = "Domanda $current di $total"
    override val next = "Avanti"
    override fun points(score: Int) = "Punti: $score"
    override val whichSurah = "Di quale sura è questo versetto?"
    override val whatIsNextAyah = "Qual è il versetto successivo?"
    override val whereIsAyah = "Dove inizia questo versetto nel mushaf?"
    override fun optionMarker(index: Int) = listOf("A", "B", "C", "D")[index]
    override val pause = "Pausa"
    override val listen = "Ascolta"
    override val chooseReciter = "Scegli il recitatore"
    override val autoPlay = "Auto"
    override val correctAnswer = "Risposta esatta"
    override val wrongAnswer = "Risposta sbagliata"
    override val resultTitle = "Risultato"
    override val perfectScore = "Masha'Allah! Punteggio perfetto"
    override val levelMastered = "Bravo! Hai superato questo livello"
    override val keepReviewing = "Bene, continua a ripassare per superarlo"
    override val needsReview = "Questo livello ha bisogno di più ripasso"
    override val surahReviewMastered = "Bravo! Conosci bene questa sura"
    override val surahNeedsReview = "Questa sura ha bisogno di più ripasso"
    override fun newRecord(previous: Int) = "Nuovo record! (precedente: $previous)"
    override val shareResult = "Condividi il risultato"
    override val retry = "Riprova"
    override val mainMenu = "Menu principale"
    override fun shareScoreText(score: Int, total: Int, levelName: String, styleTitle: String) =
        "Ho totalizzato $score su $total al livello «$levelName» nel quiz sul Corano «$appName – $styleTitle»"

    override val playbackFailed = "Impossibile caricare la recitazione. Controlla la connessione a internet."
    override val playbackFailedAutoPlayOff =
        "Impossibile caricare la recitazione, quindi la riproduzione automatica è stata disattivata. Controlla la connessione a internet."
    override val clearStatistics = "Cancella le statistiche"
    override val clearStatisticsQuestion = "Cancellare le statistiche?"
    override val clearStatisticsBody =
        "Verranno eliminati la cronologia delle risposte di ogni sura, la precisione per tipo di domanda, le serie di giorni e di risposte esatte, " +
            "la cronologia dei round e le partite giocate, le medie e le serie più lunghe di Sopravvivenza e Mutashabihat. " +
            "Il tuo punteggio migliore di ogni livello viene conservato."
    override val clear = "Cancella"
    override val cancel = "Annulla"
    override val statisticsEmpty = "Rispondi ad alcune domande per vedere qui le sure da ripassare."
    override val noMistakes = "Masha'Allah! Nessun errore finora."
    override val surahsToReview = "Sure da ripassare"
    override val accuracy = "Precisione"
    override fun correctAnswers(n: Int) = "Risposte esatte: $n"
    override fun wrongAnswers(n: Int) = "Risposte sbagliate: $n"
    override fun rankedSurah(rank: Int, name: String) = "$rank. Sura $name"
    override fun surahCounts(correct: Int, wrong: Int) = "$correct esatte · $wrong sbagliate"
    override val tabOverview = "Panoramica"
    override val tabSurahs = "Sure"
    override val tabActivity = "Attività"
    override val noAnswersYet = "Ancora nessuna risposta"
    override val styleAccuracySinceUpdate = "La precisione per tipo conta le risposte date dopo questo aggiornamento; quelle precedenti sono solo nella precisione complessiva."
    override val surahMap = "Mappa delle sure"
    override fun surahsPractised(n: Int, total: Int) = "$n su $total sure esercitate"
    override val tierNotPractised = "Non esercitata"
    override val tierWeak = "Da ripassare"
    override val tierFair = "Buona"
    override val tierStrong = "Solida"
    override val strongestSurahs = "Sure più solide"
    override val practice = "Esercitati"
    override val notPractisedYet = "Non ancora esercitata"
    override val currentStreak = "Serie di giorni attuale"
    override val bestStreak = "Migliore serie di giorni"
    override val questionsToday = "Domande di oggi"
    override val roundsCompleted = "Round completati"
    override val currentRun = "Esatte di fila"
    override val bestRun = "Migliore serie esatta"
    override val scoreTrend = "Andamento del punteggio"
    override val recentRounds = "Round conclusi"
    override val noRoundsYet = "Concludi un round per vedere qui la cronologia."
    override fun daysAgo(days: Int) = when (days) {
        0 -> "Oggi"
        1 -> "Ieri"
        else -> "$days giorni fa"
    }
    override fun questionsCount(n: Int) = if (n == 1) "${number(n)} domanda" else "${number(n)} domande"
    override fun surahCell(name: String, tier: String, percent: Int?) =
        "Sura $name, $tier" + (percent?.let { ", ${percent(it)}" } ?: "")
    override fun scoreTrendDescription(first: Int, last: Int) =
        "Andamento del punteggio: da ${percent(first)} nel round più vecchio a ${percent(last)} in quello più recente"
    override val notMastered = "non superato"
    override val levelsList = "Livelli"
    override val revelationTitle = "Meccana o Medinese"
    override val revelationSubtitle = "Cinque sure: indica se ciascuna è stata rivelata alla Mecca o a Medina"
    override val revelationQuestion = "Dove è stata rivelata questa sura?"
    override fun revelationSurah(name: String) = "Sura $name"
    override val makki = "Meccana"
    override val madani = "Medinese"
    override val revelationGood = "Bravo! Continua per un punteggio perfetto"
    override val revelationNeedsReview = "Ripassa dove sono state rivelate le sure e riprova"
}
