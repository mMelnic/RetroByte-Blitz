package hu.bme.aut.fvf13l.retrobyteblitz.data.model

data class Grid(
    val value: List<List<Int>>,
    val solution: List<List<Int>>,
    val difficulty: String
)
