package Game

data class GameParameters(
    val difficultyModifier: Int,
    val rows: Int,
    val cols: Int,
    val pieceTypes: Int,
    val timeLimit: Int
)