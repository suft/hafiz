package suft.android.hafiz.domain

import kotlinx.serialization.Serializable

/** Print of the King Fahd Complex Madinah mushaf that Where's the Ayah? uses; both have 604 pages of 15 lines. */
@Serializable
enum class MushafEdition(val hijriYear: Int, val asset: String) {
    V1(1405, "mushaf_layout_v1.txt"),
    V2(1421, "mushaf_layout_v2.txt"),
}
