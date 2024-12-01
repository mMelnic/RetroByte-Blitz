package hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import hu.bme.aut.fvf13l.retrobyteblitz.MainActivity
import hu.bme.aut.fvf13l.retrobyteblitz.R
import hu.bme.aut.fvf13l.retrobyteblitz.adapter.LeaderboardAdapter
import hu.bme.aut.fvf13l.retrobyteblitz.adapter.LeaderboardEntry
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.DialogLeaderboardBinding
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentBottomBinding
import hu.bme.aut.fvf13l.retrobyteblitz.model.StatisticsActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BottomFragment : Fragment() {

    private lateinit var binding: FragmentBottomBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomBinding.inflate(inflater, container, false)
        binding.buttonNav1.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }
        binding.buttonNav2.setOnClickListener {
            startActivity(Intent(activity, StatisticsActivity::class.java))
        }

        binding.buttonNav3.setOnClickListener {
            showLeaderboardDialog()
        }

        binding.buttonNav4.setOnClickListener {
            // Access the DrawerLayout in the activity and open the drawer
            (activity as? MainActivity)?.openNavigationDrawer()
        }
        return binding.root
    }

    private fun showLeaderboardDialog() {
        val dialogBinding = DialogLeaderboardBinding.inflate(layoutInflater)

        val adapter = LeaderboardAdapter()
        dialogBinding.leaderboardRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        dialogBinding.leaderboardRecyclerView.adapter = adapter

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        dialogBinding.dateTextView.text = getString(R.string.leaderboard_date, currentDate)

        val database = Firebase.database.reference
        database.child("leaderboard/$currentDate").get().addOnSuccessListener { snapshot ->
            val leaderboard = snapshot.children.mapNotNull { entry ->
                val score = entry.child("score").getValue(Long::class.java) ?: return@mapNotNull null
                val username = entry.child("username").getValue(String::class.java) ?: "Unknown"

                LeaderboardEntry(username, score)
            }.sortedByDescending { it.score }

            adapter.submitList(leaderboard)
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to load leaderboard", Toast.LENGTH_SHORT).show()
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Close", null)
            .create()

        dialog.show()
    }
}