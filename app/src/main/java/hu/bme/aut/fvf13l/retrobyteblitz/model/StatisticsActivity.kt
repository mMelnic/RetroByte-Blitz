package hu.bme.aut.fvf13l.retrobyteblitz.model

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import hu.bme.aut.fvf13l.retrobyteblitz.auth.UserDatabase
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.ActivityStatisticsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    private val categoryColors = mapOf(
        "Logic" to Color.RED,
        "Memory" to Color.GREEN,
        "Calculation" to Color.BLUE,
        "Visual" to Color.YELLOW
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val progressData = loadProgressData()

        setupLineChart(progressData)
    }

    private fun setupLineChart(progressData: Map<String, List<Entry>>) {
        val lineDataSets = ArrayList<LineDataSet>()

        for ((category, entries) in progressData) {
            val lineDataSet = LineDataSet(entries, category)
            lineDataSet.color = categoryColors[category] ?: Color.BLACK
            lineDataSet.setCircleColor(lineDataSet.color)
            lineDataSet.circleRadius = 7f
            lineDataSet.setDrawCircleHole(false)
            lineDataSets.add(lineDataSet)
        }

        val lineData = LineData(lineDataSets as List<ILineDataSet>?)
        binding.progressLineChart.data = lineData

        // Adjust the scale
        binding.progressLineChart.xAxis.apply {
            axisMinimum = 0f
            axisMaximum = 10f
            setLabelCount(5, true)
        }

        binding.progressLineChart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 5000f
        }

        binding.progressLineChart.axisRight.isEnabled = false

        binding.progressLineChart.isDragEnabled = true
        binding.progressLineChart.isScaleXEnabled = true
        binding.progressLineChart.isScaleYEnabled = true
        binding.progressLineChart.setVisibleXRangeMaximum(5f)

        binding.progressLineChart.invalidate()
    }



    private fun loadProgressData(): Map<String, List<Entry>> {
        val categoryScores = mutableMapOf<String, MutableList<Entry>>()
        val database = UserDatabase.getDatabase(this)

        CoroutineScope(Dispatchers.IO).launch {
            val results = database.dailyExerciseScoreDao().getAllScores()
            results.groupBy { it.category }.forEach { (category, scores) ->
                val entries = scores.mapIndexed { index, score ->
                    Entry(index.toFloat(), score.score.toFloat())
                }
                categoryScores[category] = entries.toMutableList()
            }

            withContext(Dispatchers.Main) {
                setupLineChart(categoryScores)
            }
        }

        return categoryScores
    }
}
