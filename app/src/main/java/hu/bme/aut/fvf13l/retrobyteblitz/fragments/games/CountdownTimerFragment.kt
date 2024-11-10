package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.MainActivity
import hu.bme.aut.fvf13l.retrobyteblitz.R

class CountdownTimerFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var timerTextView: TextView
    private var timer: CountDownTimer? = null
    private var totalTime: Long = 0

    companion object {
        private const val ARG_TIME_LIMIT = "time_limit"

        fun newInstance(timeLimit: Long): CountdownTimerFragment {
            val fragment = CountdownTimerFragment()
            val args = Bundle().apply {
                putLong(ARG_TIME_LIMIT, timeLimit)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register the callback in the fragment but delegate the handling to the activity
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            (activity as? MainActivity)?.handleBackPressed()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_countdown_timer, container, false)
        progressBar = rootView.findViewById(R.id.progressBar)
        timerTextView = rootView.findViewById(R.id.timerTextView)

        arguments?.getLong(ARG_TIME_LIMIT)?.let {
            totalTime = it
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startCountdown()
    }

    private fun startCountdown() {
        timer?.cancel()  // Cancel any previous timers

        timer = object : CountDownTimer(totalTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = ((millisUntilFinished.toFloat() / totalTime) * 100).toInt()

                // Update the progress bar and time text
                progressBar.progress = progress
                val minutes = (millisUntilFinished / 60000).toString().padStart(2, '0')
                val seconds = ((millisUntilFinished % 60000) / 1000).toString().padStart(2, '0')
                timerTextView.text = "Time: $minutes:$seconds"
            }

            override fun onFinish() {
                // Handle the end of the countdown (e.g., notify game logic)
                timerTextView.text = "Time's up!"
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()  // Cancel the timer if the view is destroyed
    }
}
