package hu.bme.aut.fvf13l.retrobyteblitz.model

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.applandeo.materialcalendarview.CalendarDay
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import hu.bme.aut.fvf13l.retrobyteblitz.R
import hu.bme.aut.fvf13l.retrobyteblitz.auth.UserDatabase
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.ActivityStatisticsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
        fetchCategoryCompletionEvents()
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
            val results = database.dailyExerciseScoreDao().getCategoryCompletionCounts()
            val validDays = results.filter { it.categoryCount == 4 }.map { it.date }

            val scores = database.dailyExerciseScoreDao().getAllScores()

            scores.groupBy { it.category }.forEach { (category, scores) ->
                val entries = scores
                    .filter { it.date in validDays }
                    .mapIndexed { index, score -> Entry(index.toFloat(), score.score.toFloat()) }
                categoryScores[category] = entries.toMutableList()
            }

            withContext(Dispatchers.Main) {
                setupLineChart(categoryScores)
            }
        }
        return categoryScores
    }

    private fun fetchCategoryCompletionEvents() {
        val database = UserDatabase.getDatabase(this)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        CoroutineScope(Dispatchers.IO).launch {
            val categoryCompletionCount = database.dailyExerciseScoreDao().getCategoryCompletionCounts()

            val calendarDays = mutableListOf<CalendarDay>()
            categoryCompletionCount.forEach { dateCategoryCount ->
                val calendar = Calendar.getInstance()
                calendar.time = dateFormat.parse(dateCategoryCount.date)!!

                val colorRes = when (dateCategoryCount.categoryCount) {
                    1 -> R.color.red_pastel
                    2 -> R.color.yellow_pastel
                    3 -> R.color.blue_pastel
                    4 -> R.color.green_pastel
                    else -> R.color.white
                }

                val calendarDay = CalendarDay(calendar).apply {
                    labelColor = R.color.purple_700
                    backgroundResource = colorRes
                }
                calendarDays.add(calendarDay)
            }

            withContext(Dispatchers.Main) {
                applyCalendarEvents(calendarDays)
            }
        }
    }

    private fun applyCalendarEvents(events: List<CalendarDay>) {
        binding.exerciseCalendar.setCalendarDays(events)
    }
}
