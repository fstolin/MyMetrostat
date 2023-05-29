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
        var lineInt = R.drawable.metro
        var lineInt2 = R.drawable.metro
        when (line){
            "" -> lineInt = R.drawable.metro
            "A" -> lineInt = R.drawable.metroa
            "B" -> lineInt = R.drawable.metrob
            "C" -> lineInt = R.drawable.metroc
            "AB" -> {
                lineInt = R.drawable.metroa
                lineInt2 = R.drawable.metrob
            }
            "BA" -> {
                lineInt = R.drawable.metroa
                lineInt2 = R.drawable.metrob
            }
            "AC" -> {
                lineInt = R.drawable.metroa
                lineInt2 = R.drawable.metroc
            }
            "CA" -> {
                lineInt = R.drawable.metroa
                lineInt2 = R.drawable.metroc
            }
            "CB" -> {
                lineInt = R.drawable.metrob
                lineInt2 = R.drawable.metroc
            }
            "BC" -> {
                lineInt = R.drawable.metrob
                lineInt2 = R.drawable.metroc
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