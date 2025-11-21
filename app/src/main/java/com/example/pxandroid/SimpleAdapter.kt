package com.example.pxandroid

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SimpleAdapter(
  private val items: List<String>
) : RecyclerView.Adapter<SimpleAdapter.ViewHolder>() {

  class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val tv = TextView(parent.context)
    tv.setPadding(16, 16, 16, 16)
    return ViewHolder(tv)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.textView.text = items[position]
  }

  override fun getItemCount(): Int = items.size
}
