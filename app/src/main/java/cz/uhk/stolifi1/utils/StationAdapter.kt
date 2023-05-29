package cz.uhk.stolifi1.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cz.uhk.stolifi1.R

class StationAdapter (var stationList: List<ListStation>) : RecyclerView.Adapter<StationAdapter.StationViewHolder>(){

    // Creating the recyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.station_list_layout, parent, false)
        return StationViewHolder(view)
    }

    override fun getItemCount(): Int {
        return stationList.size
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        // check the line
        val line = stationList[position].line
        var lineInt = 0
        var lineInt2 = 0
        when (line){
            "" -> lineInt = 0
            "A" -> lineInt = 1
            "B" -> lineInt = 2
            "C" -> lineInt = 3
            "AB" -> {
                lineInt = 1
                lineInt2 = 2
            }
            "BA" -> {
                lineInt = 1
                lineInt2 = 2
            }
            "AC" -> {
                lineInt = 1
                lineInt2 = 3
            }
            "CA" -> {
                lineInt = 1
                lineInt2 = 3
            }
            "CB" -> {
                lineInt = 2
                lineInt2 = 3
            }
            "BC" -> {
                lineInt = 2
                lineInt2 = 3
            }
        }

        holder.logo.setImageResource(lineInt)
        holder.logo2.setImageResource(lineInt2)
        holder.titleText.text = stationList[position].name
    }

    inner class StationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val logo: ImageView = itemView.findViewById(R.id.lineIcon)
        val logo2: ImageView = itemView.findViewById(R.id.lineIcon2)
        val titleText: TextView = itemView.findViewById(R.id.stationName)
    }
}