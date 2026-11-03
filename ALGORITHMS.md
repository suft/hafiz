# Answer-option algorithms

How `QuestionGenerator` (`app/src/main/java/suft/android/hafiz/domain/QuestionGenerator.kt`) picks prompts and wrong options.
Regular and Survival build options the same way; they differ only in how the game ends (a fixed round versus the first
mistake), so they share each diagram below. Mutashabihat differs in the generator itself (`trapMode`), so it has its own.

Topic overlap comes from `domain/ThemeIndex.kt` (BM25 over the bundled Tanzil text). Words are folded (marks dropped,
alef/ya/ta marbuta/hamza-seat variants unified); a leading و or ف is dropped only when the rest is itself a word of the
Quran, then a leading ال/لل/بال/كال/فال/وال. Mutashabihat now always plays the whole Quran, so its "no level filter" step
excludes nothing.

Where's the Ayah (`GameStyle.AyahPosition`) has no wrong-option algorithm: its six options are fixed mushaf cells (section 6). Mutashabihat does not offer it (`GameStyle.hasLookAlikes = false`).

## 1. Shared: choosing the prompt

```mermaid
flowchart TD
    A["Level + style"] --> B{"Level is openings?"}
    B -- yes --> C["Prompt = first 2 ayahs of each surah in range"]
    B -- no --> D["All ayahs in the level's surah range"]
    D --> E{"Style"}
    E -- "Guess Surah" --> F["Drop ayah 1:1 (the bismillah)"]
    E -- "Next Ayah" --> G["Drop last ayah of each surah"]
    E -- "Where's the Ayah" --> O["Keep every ayah"]
    C --> P{"Where's the Ayah?"}
    O --> P
    P -- yes --> Q["Keep only prompts with one possible cell: not on the framed pages 1-2, and no identical copy starting in another cell (2-ayah openings prompt: found in one surah only)"]
    P -- no --> H["Prompt pool"]
    F --> H
    G --> H
    Q --> H
    H --> I{"Mutashabihat mode?"}
    I -- yes --> J["Keep only prompts that have a trap; every prompt if none has one"]
    I -- "no: Regular / Survival" --> K["Keep every prompt"]
    J --> L["Shuffled deck: each prompt once, then reshuffle"]
    K --> L
    L --> M["Skip prompts among the last 19 dealt (practice round: last questionsPerRound - 1; only the last one if the pool has no more prompts than that)"]
    M --> N["Next prompt"]
```

## 2. Guess Surah: Regular and Survival

```mermaid
flowchart TD
    P["Prompt ayah(s)"] --> A["Answer = surah of the prompt"]
    P --> B["alsoCorrect = every surah containing the same ayah text"]
    P --> T["Look-alike surahs from the mutashabihat data, strongest first"]
    T --> T1["Remove alsoCorrect, keep only surahs inside the level, drop duplicates"]
    T1 --> T2["Take at most 1 trap"]
    A --> C["Candidates = level surahs minus answer, alsoCorrect and traps"]
    B --> C
    T2 --> C
    C --> D{"Enough candidates for the remaining slots?"}
    D -- no --> E["Candidates = whole Quran minus the same exclusions"]
    D -- yes --> F["Keep level candidates"]
    E --> G["relatedness = word overlap of prompt vs each surah, prompt excluded from its own surah"]
    F --> G
    G --> H["weight = prompts^0.75 x e^(6 x (relatedness - top)) x e^(-1.5 x |ln(ayah length / answer's)|) x 3 if same place of revelation as the answer; top = highest relatedness among candidates; ayah length = geometric mean of the words per ayah of the surah; place = Makki or Madani"]
    H --> I["Draw remaining wrong surahs without replacement, 3 minus traps"]
    T2 --> J["Wrong options = trap + weighted draws"]
    I --> J
    J --> K["Shuffle with the answer, giving 4 options"]
```

## 3. Guess Surah: Mutashabihat

```mermaid
flowchart TD
    P["Prompt ayah(s), only prompts that have a trap"] --> A["Answer = surah of the prompt"]
    P --> B["alsoCorrect = every surah containing the same ayah text"]
    P --> T["Look-alike surahs from the mutashabihat data, strongest first"]
    T --> T1["Remove alsoCorrect, drop duplicates, no level filter: whole Quran"]
    T1 --> T2["Take up to 3 traps"]
    A --> C["Candidates = whole Quran minus answer, alsoCorrect and traps"]
    B --> C
    T2 --> C
    C --> D{"Slots left after traps?"}
    D -- "0" --> J
    D -- ">0" --> W["weight = prompts^0.75 only, no topic term"]
    W --> I["Draw fillers without replacement"]
    I --> J["Wrong options = traps + fillers"]
    T2 --> J
    J --> K["Shuffle with the answer, giving 4 options"]
```

## 4. Next Ayah: Regular and Survival

```mermaid
flowchart TD
    P["Prompt ayah(s)"] --> A["Answer = the ayah right after the prompt"]
    P --> V["Forbidden texts = the prompt's own text + answer text + the ayah after every identical copy of the prompt"]
    P --> T["Look-alikes of the answer, plus the ayah after look-alikes of the prompt (slips), strongest first"]
    V --> F["Filter: not a prompt ayah or the answer, text not forbidden, inside the level, distinct text"]
    T --> F
    F --> T2["Take at most 1 trap"]
    A --> R["Search window = answer +/- 60 ayahs"]
    R --> S["Sort: same surah first, then closest to the answer"]
    V --> S
    S --> S1["Filter forbidden texts, trap texts, duplicates"]
    S1 --> S2["Keep the first 8"]
    S2 --> S3["Shuffle, take 3 minus traps"]
    T2 --> W["Wrong options = trap + neighbours"]
    S3 --> W
    W --> X["Strip the sajdah sign from every option"]
    X --> Y["Shuffle with the answer, giving 4 options"]
```

## 5. Next Ayah: Mutashabihat

```mermaid
flowchart TD
    P["Prompt ayah(s), only prompts that have a trap"] --> A["Answer = the ayah right after the prompt"]
    P --> V["Forbidden texts = the prompt's own text + answer text + the ayah after every identical copy of the prompt"]
    P --> T["Look-alikes of the answer, plus the ayah after look-alikes of the prompt (slips), strongest first"]
    V --> F["Filter: not a prompt ayah or the answer, text not forbidden, distinct text, no level filter"]
    T --> F
    F --> T2["Take up to 3 traps"]
    A --> R["Search window = answer +/- 60 ayahs"]
    R --> S["Sort: same surah first, then closest to the answer"]
    V --> S
    S --> S1["Filter forbidden texts, trap texts, duplicates"]
    S1 --> S2["Keep the first 8"]
    S2 --> S3["Shuffle, take 3 minus traps"]
    T2 --> W["Wrong options = traps + neighbours"]
    S3 --> W
    W --> X["Strip the sajdah sign from every option"]
    X --> Y["Shuffle with the answer, giving 4 options"]
```

## 6. Where's the Ayah: Regular and Survival

```mermaid
flowchart TD
    P["Prompt ayah(s), only those with one possible cell"] --> A["Cell where the first prompt ayah starts in the player's chosen Madinah mushaf edition (1405H v1 or 1421H v2): right or left page x top, middle or bottom third (lines 1-5, 6-10, 11-15)"]
    A --> B["Options = the six MushafCell labels in fixed order, not shuffled"]
    B --> C["Correct option = that cell's position in the order"]
```

The edition is a setting (`Progress.mushafEdition`, default v1) read when a round starts. Each edition has its own layout file, so the same ayah can start on a different page or line, and in a different cell, in the other print; the prompt pool is built from that edition's layout.

## Summary

| | Regular / Survival | Mutashabihat |
|---|---|---|
| Prompt pool | all prompts | only prompts with a trap (all if none has one) |
| Trap options | at most 1, from inside the level | up to 3, from the whole Quran |
| Guess Surah fillers | weighted by surah length, topic overlap, typical ayah length and Makki/Madani place | weighted by surah length only |
| Next Ayah fillers | nearby ayahs | nearby ayahs |
| Where's the Ayah options | six fixed mushaf cells | not offered |
