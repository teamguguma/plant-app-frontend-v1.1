package com.guguma.guguma_application.viewmodel
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.guguma.guguma_application.BuildConfig
import com.guguma.guguma_application.dto.PlantDto
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONArray

class PlantViewModel(private val userId: String) : ViewModel() {
    private val _plantList = MutableLiveData<MutableList<PlantDto>>()
    val plantList: LiveData<MutableList<PlantDto>> = _plantList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val client = OkHttpClient()

    init {
        fetchPlantsFromServer()
    }

    // addPlant 메서드를 클래스 최상위로 이동
    fun addPlant(plant: PlantDto) {
        val currentList = _plantList.value ?: mutableListOf()
        currentList.add(plant) // 새로운 식물 추가
        _plantList.postValue(currentList) // LiveData 업데이트
    }

    // 서버에서 식물 목록 가져오기
    fun fetchPlantsFromServer() {
        val url = BuildConfig.API_PLANT_ALL_LIST.replace("{userUuid}", userId)
        val request = Request.Builder()
            .url(url) // 서버 URL
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    if (!res.isSuccessful) {
                        Log.e("PlantViewModel", "Failed to fetch plants: ${res.message}")
                        _errorMessage.postValue("Failed to fetch plants: ${res.message}")
                        return
                    }
                    val responseData = res.body?.string()
                    Log.d("PlantViewModel", "Received JSON: $responseData")

                    if (!responseData.isNullOrEmpty()) {
                        val plants = parsePlantData(responseData).toMutableList()
                        Log.d("PlantViewModel", "Fetched plants: $plants")
                        _plantList.postValue(plants) // LiveData 갱신
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("PlantViewModel", "Error fetching plants: ${e.message}")
                _errorMessage.postValue("Error fetching plants: ${e.message}")
            }
        })
    }

    // JSON 데이터를 PlantDto 객체로 변환
    private fun parsePlantData(json: String): List<PlantDto> {
        val jsonArray = JSONArray(json)
        val plantList = mutableListOf<PlantDto>()

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val id = item.getLong("id")
            val plantNickname  = item.getString("plantNickname")
            val name = item.getString("plantName")
            val createdAt = item.getString("createdAt")
            val chckdate = item.getInt("checkDateInterval")

            // statuses 배열 내의 첫 번째 객체에서 imageUrl을 가져옵니다.
            val recentStatus = item.getJSONObject("recentStatus")
            val imageUrl = recentStatus.getString("imageUrl")
            val remedy = recentStatus.getString("remedy")

            plantList.add(PlantDto(id, createdAt, name, chckdate, plantNickname, remedy, imageUrl))
        }
        return plantList
    }
}