package com.guguma.guguma_application

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.guguma.guguma_application.dto.PlantDto
import androidx.recyclerview.widget.RecyclerView

class PlantAdapter(
    private val context: Context,
    private val plantList: MutableList<PlantDto> = mutableListOf() // 기본값으로 빈 리스트 제공
//    private val plantList: MutableList<PlantDto>,
) : RecyclerView.Adapter<PlantAdapter.PlantViewHolder>() {
    inner class PlantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val plantNicknameTextView: TextView = itemView.findViewById(R.id.plantNicknameTextView)
        val DateTextView: TextView = itemView.findViewById(R.id.plantCreateDateTextView)
        val plantImageView: ImageView = itemView.findViewById(R.id.plantImageView)

        fun bind(plant: PlantDto) {
            plantNicknameTextView.text = plant.nickname
            DateTextView.text = plant.createDate
            Glide.with(context).load(plant.imageUrl).into(plantImageView)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_plant, parent, false)
        return PlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val plant = plantList[position]
        holder.bind(plant)

        // 클릭 이벤트: InfoDetailActivity로 이동
        holder.itemView.setOnClickListener {
            val intent = Intent(context, InfoDetailActivity::class.java).apply {
                putExtra("plantID", plant.id)
                putExtra("plantCreateDate", plant.createDate)
                putExtra("plantName", plant.name)
                putExtra("plantCheckDate", plant.checkdate)
                putExtra("plantNickname", plant.nickname)
                putExtra("plantRemedy", plant.remedy)
                putExtra("plantImageUrl", plant.imageUrl)
            }
            context.startActivity(intent)
        }
    }

    // getItemCount: 아이템 총 개수 반환 (최대 10개만 반환)
    override fun getItemCount(): Int {
        return if (plantList.size > 10) 10 else plantList.size
    }

    // 특정 위치의 데이터를 가져오는 getItem 메서드
    fun getItem(position: Int): PlantDto {
        return plantList[position]
    }

    // 데이터 업데이트 메서드
    fun updateData(newPlantList: List<PlantDto>) {
        plantList.clear()
        plantList.addAll(newPlantList)
        notifyDataSetChanged() // 데이터 변경 후 UI 갱신
    }

    // 아이템 추가 메서드 (Create)
    fun addItem(plant: PlantDto) {
        plantList.add(plant)
        notifyItemInserted(plantList.size - 1)
    }

    // 아이템 수정 메서드 (Update)
    fun updateItem(position: Int, updatedPlant: PlantDto) {
        plantList[position] = updatedPlant
        notifyItemChanged(position)
    }
}