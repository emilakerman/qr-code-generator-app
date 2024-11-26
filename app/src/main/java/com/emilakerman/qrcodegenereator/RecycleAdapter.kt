package com.emilakerman.qrcodegenereator

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import coil.load
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RecycleAdapter(private val fragmentManager: FragmentManager, private val context: Context, private var qrCodes: Array<String>?) :
    RecyclerView.Adapter<RecycleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageHelper = ImageHelper();
        val qrRepository = QrRepository();
        val qrCode = qrCodes?.get(position)
        holder.imageView.apply {
            setImageResource(R.drawable.whiteprogress) // Placeholder
            load(qrCode) {
                placeholder(R.drawable.whiteprogress)
                error(R.drawable.whiteprogress)
            }
        }

        holder.downloadButton.setOnClickListener {
            qrCodes?.get(position)?.let { it1 -> imageHelper.saveImageFromUrl(context, it1) }
        }
        holder.deleteButton.setOnClickListener {
            qrCodes?.get(position)?.let { qrCode ->
                qrRepository.deleteQrCode(qrCode)
                val updatedList = qrCodes?.toMutableList()
                updatedList?.removeAt(position)
                qrCodes = updatedList?.toTypedArray()
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, itemCount)
            }
            // Moves back to home activity/fragment if user deleted all Qr Codes.
            if (itemCount == 0) {
                val transaction = fragmentManager.beginTransaction()
                val fragment = fragmentManager.findFragmentById(R.id.fragment_container_view) // Replace with your container ID
                if (fragment != null) {
                    transaction.remove(fragment).commit()
                }
            }
        }
    }
    override fun getItemCount(): Int {
        return qrCodes?.size ?: 0
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val downloadButton: Button = itemView.findViewById(R.id.download)
        val deleteButton: Button = itemView.findViewById(R.id.delete)
        val fragmentStuff: View? = itemView.findViewById(R.id.fragment_container_view);
    }
}