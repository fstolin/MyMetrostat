package cz.uhk.stolifi1.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cz.uhk.stolifi1.R

class StationAdapter (var stationList: List<ListStation>, private var listener: OnItemClickListener) : RecyclerView.Adapter<StationAdapter.StationViewHolder>(){
    inner class StationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val logo: ImageView = itemView.findViewById(R.id.lineIcon)
        val logo2: ImageView = itemView.findViewById(R.id.lineIcon2)
        val titleText: TextView = itemView.findViewById(R.id.stationName)
        val distance: TextView = itemView.findViewById(R.id.stationDistance)

        // Item on click listeners in init
        init {
            itemView.setOnClickListener(this)
        }

        // On click function
        override fun onClick(v: View?) {
            val position: Int = adapterPosition
            // is position valid
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }
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
        if(transfer) {
            holder.logo2.visibility = View.VISIBLE
        } else {
            holder.logo2.visibility = View.INVISIBLE
        }
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

    fun updateStationList(list: List<ListStation>){
        this.stationList = list
        notifyDataSetChanged()
    }

    fun updateStationListDistanceOnly(list: List<ListStation>){
        for (station in this.stationList){
            for (listStation in list){
                if (station.dbId == listStation.dbId) station.distance = listStation.distance
            }
        }
        this.stationList = ArrayList(this.stationList.sortedBy { it.distance })
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}