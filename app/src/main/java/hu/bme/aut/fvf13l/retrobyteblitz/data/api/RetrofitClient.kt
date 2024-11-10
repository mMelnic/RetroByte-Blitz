package hu.bme.aut.fvf13l.retrobyteblitz.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://sudoku-api.vercel.app/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: SudokuApi = retrofit.create(SudokuApi::class.java)
}
