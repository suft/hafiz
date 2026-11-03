package suft.android.hafiz.ui.i18n

import suft.android.hafiz.domain.AppLanguage
import suft.android.hafiz.domain.GameMode
import suft.android.hafiz.domain.GameStyle
import suft.android.hafiz.domain.MushafEdition

object PortugueseStrings : Strings {
    override val language = AppLanguage.Portuguese

    override fun percent(value: Int) = "$value%"

    override val appName = "Hafiz"
    override val back = "Voltar"
    override val loadingQuran = "Carregando o Alcorão…"
    override val tagline = "Teste sua memorização do Sagrado Alcorão"
    override fun styleTitle(style: GameStyle) = when (style) {
        GameStyle.GuessSurah -> "Adivinhe a surata"
        GameStyle.NextAyah -> "Próximo versículo"
        GameStyle.AyahPosition -> "Onde está o versículo?"
    }

    override fun styleSubtitle(style: GameStyle) = when (style) {
        GameStyle.GuessSurah -> "Leia o versículo e escolha sua surata"
        GameStyle.NextAyah -> "Escolha o versículo que vem a seguir"
        GameStyle.AyahPosition -> "Leia o versículo e escolha onde ele começa no mushaf de Medina"
    }

    override fun gameModeTitle(mode: GameMode) = when (mode) {
        GameMode.Regular -> "Normal"
        GameMode.Survival -> "Sobrevivência"
        GameMode.Mutashabihat -> "Mutashabihat"
    }

    override fun gameModeSubtitle(mode: GameMode) = when (mode) {
        GameMode.Regular -> "Rodadas de tamanho fixo; domine cada nível"
        GameMode.Survival -> "Perguntas sem fim; um erro encerra o jogo"
        GameMode.Mutashabihat ->
            "Perguntas de todo o Alcorão com versículos parecidos como armadilha (mutashabihat); três erros encerram o jogo"
    }

    override val chooseStyle = "Escolha um estilo de pergunta"
    override val best = "melhor"
    override fun questionNumber(n: Int) = "Pergunta $n"
    override fun livesLeft(n: Int) = "Vidas restantes: $n"
    override val gameOver = "Fim de jogo"
    override fun survivalResult(score: Int) = "Acertos seguidos: $score"
    override fun livesResult(score: Int, answered: Int) = "$score acertos em ${questionsCount(answered)}"
    override fun longestStreak(n: Int) = "Maior sequência: $n"
    override fun bestScore(n: Int) = "Melhor pontuação: $n"
    override fun gamesPlayed(n: Int) = "Partidas jogadas: $n"
    override fun averageScore(n: Int) = "Pontuação média: $n"
    override val noGamesYet = "Nenhuma partida ainda"
    override fun shareRunText(score: Int, levelName: String, modeTitle: String, styleTitle: String) =
        "Fiz $score pontos no modo $modeTitle no nível “$levelName” do quiz do Alcorão “$appName – $styleTitle”"

    override val mastered = "dominado"
    override val statistics = "Estatísticas"
    override val statisticsHint = "Veja quais suratas precisam de mais revisão"
    override val play = "Jogar"
    override val playHint = "Escolha um modo de jogo, depois um estilo de pergunta e um nível"
    override val gameModes = "Modos de jogo"
    override val howToPlay = "Como jogar"
    override val howToPlayHint = "Conheça os estilos de pergunta, modos de jogo, níveis e configurações"
    override val guideStylesHeading = "Estilos de pergunta"
    override val guideLevelsHeading = "Níveis e domínio"
    override val guideLevels =
        "Cada estilo de pergunta tem níveis que cobrem partes diferentes do Alcorão, das suratas curtas até o Alcorão inteiro. " +
            "O modo Normal guarda sua melhor rodada em cada nível e o modo Sobrevivência sua melhor partida em cada nível. " +
            "O Mutashabihat não tem níveis: as perguntas vêm de todo o Alcorão."
    override fun guideMastery(styleTitle: String, questions: Int, mastery: Int) =
        "$styleTitle: uma rodada Normal tem ${questionsCount(questions)}; uma pontuação de ${number(mastery)} ou mais domina o nível."
    override val guideStatsHeading = "Estatísticas e revisão"
    override val guideStats =
        "As estatísticas mostram sua precisão e as suratas que precisam de revisão; a partir delas você pode iniciar uma rodada de revisão de uma única surata."
    override val guideSettings
        get() = "A engrenagem no alto do menu principal muda o idioma do app e o mushaf usado em " +
            "${styleTitle(GameStyle.AyahPosition)}: a ${mushafEditionName(MushafEdition.V1)}, a padrão, ou a " +
            "${mushafEditionName(MushafEdition.V2)}. Alguns versículos começam em lugares diferentes em cada edição; " +
            "as pontuações são compartilhadas entre as duas."
    override fun statisticsSummary(percent: Int, total: Int) =
        "Precisão de ${percent(percent)} em ${questionsCount(total)}"

    override val languageLabel = "Idioma"
    override val settings = "Configurações"
    override val mushafEditionLabel get() = "Mushaf para “${styleTitle(GameStyle.AyahPosition)}”"

    override fun mushafEditionName(edition: MushafEdition) = when (edition) {
        MushafEdition.V1 -> "Edição de ${number(edition.hijriYear)} H (v1)"
        MushafEdition.V2 -> "Edição de ${number(edition.hijriYear)} H (v2)"
    }
    override val credits = "Créditos"
    override val creditsQuranText = "Texto do Alcorão"
    override val creditsRecitations = "Recitações"
    override val creditsMutashabihat = "Mutashabihat"
    override val creditsMushafLayout = "Posições dos versículos no mushaf"
    override val creditsTanzil = "Tanzil Project"
    override val creditsAswaatulQurraa = "Aswaatul Qurraa"
    override val creditsDeveloper = "Desenvolvedor"
    override val developerName = "Sufien Tout"
    override val creditsInspiredBy = "Inspirado por"
    override val privacyPolicy = "Política de privacidade"
    override val creditsSourcesHeading = "Fontes"
    override val creditsPeopleHeading = "Pessoas"
    override val creditsLegalHeading = "Legal"
    override val creditsTanzilLicence = "CC BY 3.0, usado sem modificações"
    override val creditsMushafEditions
        get() = "Edições de ${number(MushafEdition.V1.hijriYear)} H e ${number(MushafEdition.V2.hijriYear)} H"
    override val creditsIcons = "Ícones"
    override val creditsIconsLicence = "Licença MIT"
    override val opensInBrowser = "Abre no seu navegador"
    override fun versionLabel(version: String) = "Versão $version"
    override fun totalPoints(points: Int) = "Total de pontos: $points"
    override fun masteredCount(mastered: Int, total: Int) = "$mastered de $total dominados"
    override val start = "Começar"
    override fun questionProgress(current: Int, total: Int) = "Pergunta $current de $total"
    override val next = "Avançar"
    override fun points(score: Int) = "Pontos: $score"
    override val whichSurah = "De qual surata é este versículo?"
    override val whatIsNextAyah = "Qual é o próximo versículo?"
    override val whereIsAyah = "Onde este versículo começa no mushaf?"
    override fun optionMarker(index: Int) = listOf("A", "B", "C", "D")[index]
    override val pause = "Pausar"
    override val listen = "Ouvir"
    override val chooseReciter = "Escolher recitador"
    override val autoPlay = "Auto"
    override val correctAnswer = "Resposta correta"
    override val wrongAnswer = "Resposta errada"
    override val resultTitle = "Resultado"
    override val perfectScore = "Masha'Allah! Pontuação perfeita"
    override val levelMastered = "Muito bem! Você dominou este nível"
    override val keepReviewing = "Bom, continue revisando para dominá-lo"
    override val needsReview = "Este nível precisa de mais revisão"
    override val surahReviewMastered = "Muito bem! Você conhece bem esta surata"
    override val surahNeedsReview = "Esta surata precisa de mais revisão"
    override fun newRecord(previous: Int) = "Novo recorde! (anterior: $previous)"
    override val shareResult = "Compartilhar resultado"
    override val retry = "Tentar de novo"
    override val mainMenu = "Menu principal"
    override fun shareScoreText(score: Int, total: Int, levelName: String, styleTitle: String) =
        "Fiz $score de $total no nível “$levelName” do quiz do Alcorão “$appName – $styleTitle”"

    override val playbackFailed = "Não foi possível carregar a recitação. Verifique sua conexão com a internet."
    override val playbackFailedAutoPlayOff =
        "Não foi possível carregar a recitação, então a reprodução automática foi desativada. " +
            "Verifique sua conexão com a internet."
    override val clearStatistics = "Apagar estatísticas"
    override val clearStatisticsQuestion = "Apagar estatísticas?"
    override val clearStatisticsBody =
        "Isso apaga o histórico de respostas de todas as suratas, a precisão por estilo de pergunta, as sequências de dias " +
            "e de acertos, o histórico de rodadas e as partidas jogadas, médias e maiores sequências dos modos " +
            "Sobrevivência e Mutashabihat. " +
            "Sua melhor pontuação em cada nível é mantida."
    override val clear = "Apagar"
    override val cancel = "Cancelar"
    override val statisticsEmpty = "Responda algumas perguntas para ver aqui as suratas que precisam de revisão."
    override val noMistakes = "Masha'Allah! Nenhum erro até agora."
    override val surahsToReview = "Suratas que precisam de revisão"
    override val accuracy = "Precisão"
    override fun correctAnswers(n: Int) = "Respostas corretas: $n"
    override fun wrongAnswers(n: Int) = "Respostas erradas: $n"
    override fun rankedSurah(rank: Int, name: String) = "$rank. Surata $name"
    override fun surahCounts(correct: Int, wrong: Int) = "$correct certas · $wrong erradas"
    override val tabOverview = "Resumo"
    override val tabSurahs = "Suratas"
    override val tabActivity = "Atividade"
    override val noAnswersYet = "Nenhuma resposta ainda"
    override val styleAccuracySinceUpdate =
        "A precisão por estilo conta as respostas dadas desde esta atualização; as mais antigas entram só na precisão geral."
    override val surahMap = "Mapa das suratas"
    override fun surahsPractised(n: Int, total: Int) = "$n de $total suratas praticadas"
    override val tierNotPractised = "Não praticada"
    override val tierWeak = "Precisa de revisão"
    override val tierFair = "Boa"
    override val tierStrong = "Forte"
    override val strongestSurahs = "Suratas mais fortes"
    override val practice = "Praticar"
    override val notPractisedYet = "Ainda não praticada"
    override val currentStreak = "Sequência atual de dias"
    override val bestStreak = "Melhor sequência de dias"
    override val questionsToday = "Perguntas hoje"
    override val roundsCompleted = "Rodadas concluídas"
    override val currentRun = "Acertos seguidos"
    override val bestRun = "Maior sequência de acertos"
    override val scoreTrend = "Evolução da pontuação"
    override val recentRounds = "Rodadas concluídas"
    override val noRoundsYet = "Termine uma rodada para ver seu histórico aqui."
    override fun daysAgo(days: Int) = when (days) {
        0 -> "Hoje"
        1 -> "Ontem"
        else -> "Há $days dias"
    }
    override fun questionsCount(n: Int) = if (n == 1) "${number(n)} pergunta" else "${number(n)} perguntas"
    override fun surahCell(name: String, tier: String, percent: Int?) =
        "Surata $name, $tier" + (percent?.let { ", ${percent(it)}" } ?: "")
    override fun scoreTrendDescription(first: Int, last: Int) =
        "Evolução da pontuação: de ${percent(first)} na rodada mais antiga a ${percent(last)} na mais recente"
    override val notMastered = "não dominado"
    override val levelsList = "Níveis"
    override val revelationTitle = "Mecana ou Medinense"
    override val revelationSubtitle = "Cinco suratas: diga se cada uma foi revelada em Meca ou em Medina"
    override val revelationQuestion = "Onde esta surata foi revelada?"
    override fun revelationSurah(name: String) = "Surata $name"
    override val makki = "Mecana"
    override val madani = "Medinense"
    override val revelationGood = "Muito bem! Continue para uma pontuação perfeita"
    override val revelationNeedsReview = "Revise onde as suratas foram reveladas e tente de novo"
}
