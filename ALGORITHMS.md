# Answer-option algorithms

How `QuestionGenerator` (`app/src/main/java/suft/android/hafiz/domain/QuestionGenerator.kt`) picks prompts and wrong options.
Regular and Survival build options the same way; they differ only in how the game ends (a fixed round versus the first
mistake), so they share each diagram below. Mutashabihat differs in the generator itself (`trapMode`), so it has its own.

Topic overlap comes from `domain/ThemeIndex.kt` (BM25 over the bundled Tanzil text). Mutashabihat now always plays the
whole Quran, so its "no level filter" step excludes nothing.

## 1. Shared: choosing the prompt

```mermaid
flowchart TD
    A["Level + style"] --> B{"Level is openings?"}
    B -- yes --> C["Prompt = first 2 ayahs of each surah in range"]
    B -- no --> D["All ayahs in the level's surah range"]
    D --> E{"Style"}
    E -- "Guess Surah" --> F["Drop ayah 1:1 (the bismillah)"]
    E -- "Next Ayah" --> G["Drop last ayah of each surah"]
    C --> H["Prompt pool"]
    F --> H
    G --> H
    H --> I{"Mutashabihat mode?"}
    I -- yes --> J["Keep only prompts that have a trap"]
    I -- "no: Regular / Survival" --> K["Keep every prompt"]
    J --> L["Shuffled deck: each prompt once, then reshuffle"]
    K --> L
    L --> M["Skip prompts among the last 20 dealt"]
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
    G --> H["weight = prompts^0.75 x e^(6 x relatedness - top)"]
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
    P --> V["Forbidden texts = answer text + the ayah after every identical copy of the prompt"]
    P --> T["Look-alikes of the answer, plus the ayah after look-alikes of the prompt (slips), strongest first"]
    V --> F["Filter: not between prompt and answer, text not forbidden, inside the level, distinct text"]
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
    P --> V["Forbidden texts = answer text + the ayah after every identical copy of the prompt"]
    P --> T["Look-alikes of the answer, plus the ayah after look-alikes of the prompt (slips), strongest first"]
    V --> F["Filter: not between prompt and answer, text not forbidden, distinct text, no level filter"]
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

## Summary

| | Regular / Survival | Mutashabihat |
|---|---|---|
| Prompt pool | all prompts | only prompts with a trap |
| Trap options | at most 1, from inside the level | up to 3, from the whole Quran |
| Guess Surah fillers | weighted by length and topic overlap | weighted by length only |
| Next Ayah fillers | nearby ayahs | nearby ayahs |
