package cz.uhk.stolifi1.utils

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cz.uhk.stolifi1.R

class StationAdapter (var stationList: List<ListStation>) : RecyclerView.Adapter<StationAdapter.StationViewHolder>(){
    inner class StationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val logo: ImageView = itemView.findViewById(R.id.lineIcon)
        val logo2: ImageView = itemView.findViewById(R.id.lineIcon2)
        val titleText: TextView = itemView.findViewById(R.id.stationName)
        val distance: TextView = itemView.findViewById(R.id.stationDistance)
    }

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
        var transfer = false
        val line = stationList[position].line
        var lineInt = R.drawable.metroa
        //default
        var lineInt2 = R.drawable.metroa
        when (line){
            "" -> lineInt = R.drawable.metroa
            "A" -> lineInt = R.drawable.metroa
            "B" -> lineInt = R.drawable.metrob
            "C" -> lineInt = R.drawable.metroc
            "AB" -> {
                lineInt = R.drawable.metroa
                lineInt2 = R.drawable.metrob
                transfer = true
            }
            "BA" -> {
                lineInt = R.drawable.metroa
                lineInt2 = R.drawable.metrob
                transfer = true
            }
            "AC" -> {
                lineInt = R.drawable.metroa
                lineInt2 = R.drawable.metroc
                transfer = true
            }
            "CA" -> {
                lineInt = R.drawable.metroa
                lineInt2 = R.drawable.metroc
                transfer = true
            }
            "CB" -> {
                lineInt = R.drawable.metrob
                lineInt2 = R.drawable.metroc
                transfer = true
            }
            "BC" -> {
                lineInt = R.drawable.metrob
                lineInt2 = R.drawable.metroc
                transfer = true
            }
        }

        // Line logos
        holder.logo.setImageResource(lineInt)
        holder.logo2.setImageResource(lineInt2)
        if(transfer) holder.logo2.visibility = View.VISIBLE
        // Distance
        var unit = "m"
        var distanceValue = stationList[position].distance
        if (distanceValue > 5000) {
            distanceValue /= 1000.0
            unit = "km"
        }
        holder.distance.text = "${distanceValue.toInt()} $unit"
        // Name
        holder.titleText.text = stationList[position].name
    }

    fun setFilteredList(list: List<ListStation>){
        this.stationList = list
        notifyDataSetChanged()
    }

}