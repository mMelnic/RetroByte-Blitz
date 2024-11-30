package hu.bme.aut.fvf13l.retrobyteblitz.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.ItemLeaderboardEntryBinding

data class LeaderboardEntry(val username: String, val score: Long)

class LeaderboardAdapter : ListAdapter<LeaderboardEntry, LeaderboardAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(private val binding: ItemLeaderboardEntryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: LeaderboardEntry) {
            binding.usernameTextView.text = entry.username
            binding.scoreTextView.text = entry.score.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<LeaderboardEntry>() {
        override fun areItemsTheSame(oldItem: LeaderboardEntry, newItem: LeaderboardEntry): Boolean {
            return oldItem.username == newItem.username
        }

        override fun areContentsTheSame(oldItem: LeaderboardEntry, newItem: LeaderboardEntry): Boolean {
            return oldItem == newItem
        }
    }
}