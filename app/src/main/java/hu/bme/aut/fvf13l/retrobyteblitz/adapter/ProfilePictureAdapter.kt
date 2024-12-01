package hu.bme.aut.fvf13l.retrobyteblitz.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import hu.bme.aut.fvf13l.retrobyteblitz.R

class ProfilePictureAdapter(
    private val imageUrls: List<String>,
    private val onImageSelected: (String) -> Unit
) : RecyclerView.Adapter<ProfilePictureAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_picture, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        Glide.with(holder.imageView.context).load(imageUrl).into(holder.imageView)

        holder.imageView.setOnClickListener {
            onImageSelected(imageUrl)
        }
    }

    override fun getItemCount(): Int = imageUrls.size
}
