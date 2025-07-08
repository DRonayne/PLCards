package com.darach.plcards.ui.my_xi

enum class Formation(
    val displayName: String,
    val defenders: Int,
    val midfielders: Int,
    val forwards: Int
) {
    F442("4-4-2", 4, 4, 2),
    F433("4-3-3", 4, 3, 3),
    F343("3-4-3", 3, 4, 3),
    F352("3-5-2", 3, 5, 2),
    F451("4-5-1", 4, 5, 1)
}

enum class FormationRow {
    GOALKEEPER,
    DEFENDERS,
    MIDFIELDERS,
    FORWARDS
}