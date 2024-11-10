package hu.bme.aut.fvf13l.retrobyteblitz.data.api

import hu.bme.aut.fvf13l.retrobyteblitz.data.model.SudokuResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SudokuApi {

    @GET("api/dosuku")
    suspend fun getSudokuGrid(@Query("query") query: String): SudokuResponse
}
