package com.guguma.guguma_application

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import okhttp3.OkHttpClient
import okhttp3.*
import java.io.IOException

class InfoDetailActivity : AppCompatActivity() {

    private lateinit var editPlantActivityResultLauncher: ActivityResultLauncher<Intent>
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_detail)

        val id = intent.getLongExtra("plantID", -1)
        val name= intent.getStringExtra("plantName")
        val nickname = intent.getStringExtra("plantNickname")
        val checkDate = intent.getIntExtra("plantCheckDate", 7)
        val createDate = intent.getStringExtra("plantCreateDate")
        val remedy = intent.getStringExtra("plantRemedy")
        val imageUrl = intent.getStringExtra("plantImageUrl")

        // 뷰 초기화
        val infoDetailNickName: TextView = findViewById(R.id.InfoDetailNickName)
        val infoDetailPlantName: TextView = findViewById(R.id.InfoDetailPlantName)
        val infoDetailCheckDate: TextView = findViewById(R.id.InfoDetailCheckDate)
        val infoDetailAddDate: TextView = findViewById(R.id.InfoDetailAddDate)
        val detailInfo: TextView = findViewById(R.id.detail_info)
        val plantImageView: ImageView = findViewById(R.id.InfoDetailPlantPic)
        val backButton: ImageButton = findViewById(R.id.detail_back_imgbtn)

        val editButton: Button = findViewById(R.id.detail_EDIT_imgbtn)
        val deleteButton: Button = findViewById(R.id.detail_DELETE_imgbtn)

        // 데이터 설정
        infoDetailNickName.text = nickname
        infoDetailPlantName.text = name
        infoDetailCheckDate.text = checkDate.toString()
        infoDetailAddDate.text = createDate // 이 부분은 createDate 대신 적절한 데이터를 사용하세요.
        detailInfo.text = remedy
        Glide.with(this).load(imageUrl).into(plantImageView)

        // Result Launcher 초기화
        editPlantActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { intent ->
                    val updatedNickname = intent.getStringExtra("updatePlantName")
                    val updatedPlantName = intent.getStringExtra("updatePlantNickname")
                    val updatedCheckDateInterval = intent.getIntExtra("updatePlantCheckDate", 7)

                    // 뷰 업데이트
                    infoDetailNickName.text = updatedNickname
                    infoDetailPlantName.text = updatedPlantName
                    infoDetailCheckDate.text = updatedCheckDateInterval.toString()
                }
            }
        }

        deleteButton.setOnClickListener{
            id.let { id ->
                if (id != null) {
                    deletePlant(id)
                } // 식물 ID를 사용하여 삭제 함수 호출
                else{
                    Toast.makeText(this@InfoDetailActivity, "식물 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            finish()
        }

        editButton.setOnClickListener{
//            val intent = Intent(this, EditPlantActivity::class.java)
//            startActivity(intent)
            val intent = Intent(this, EditPlantActivity::class.java).apply {
                putExtra("plantID", id)
                putExtra("plantName", name)
                putExtra("plantNickname", nickname)
                putExtra("plantCheckDate", checkDate)
                putExtra("plantCreateDate", createDate)
                putExtra("plantRemedy", remedy)
                putExtra("plantImageUrl", imageUrl)
            }
            editPlantActivityResultLauncher.launch(intent)
        }

        backButton.setOnClickListener {
            finish() // 현재 액티비티 종료 -> 이전 화면으로 돌아감
        }
    }

    // 식물을 서버에서 삭제하는 함수
    private fun deletePlant(plantId: Long) {

        val id = plantId.toString()
        val url = BuildConfig.API_PLANT_DELETE.replace("{plantId}", id)

        val request = Request.Builder()
            .url(url) // 실제 삭제를 처리할 서버의 URL
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@InfoDetailActivity, "삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@InfoDetailActivity, "식물이 성공적으로 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                            finish() // 삭제 후 액티비티 종료
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@InfoDetailActivity, "서버 오류: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}
