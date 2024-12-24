package com.guguma.guguma_application

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import androidx.appcompat.app.AlertDialog
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class EditPlantActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var plantNickNameTextView: TextView
    private lateinit var plantNameTextView: TextView
    private lateinit var plantNameEditText: EditText
//    private lateinit var plantRemedyTextView: TextView
//    private lateinit var plantRemedyEditText: EditText
    private lateinit var plantCheckDate: TextView
    private lateinit var nameEditButton: ImageButton
    private lateinit var remedyEditButton: ImageButton
    private lateinit var addButton: Button

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_plant)


        // View 초기화
        imageView = findViewById(R.id.userPlantPic)
        plantNickNameTextView = findViewById(R.id.plantNicknameEditText)
        plantNameTextView = findViewById(R.id.plantSearchView)
        plantNameEditText = findViewById(R.id.plantSearchEditText)
        nameEditButton = findViewById(R.id.SearchEditButton)
//        plantRemedyTextView = findViewById(R.id.plantRemedyText)
//        plantRemedyEditText = findViewById(R.id.plantRemedyEditText)
        plantCheckDate = findViewById(R.id.dateEditText)
//        remedyEditButton = findViewById(R.id.RemedyEditButton)
        addButton = findViewById(R.id.addButton)


        // 전달받은 ID 가져오기

        val id = intent.getLongExtra("plantID", -1)
        val plantName = intent.getStringExtra("plantName")
        val nickname = intent.getStringExtra("plantNickname")
        val checkDate = intent.getIntExtra("plantCheckDate", 7)
        val createDate = intent.getStringExtra("plantCreateDate")
        val plantRemedy = intent.getStringExtra("plantRemedy")
        val imageUrl = intent.getStringExtra("plantImageUrl") ?: "알 수 없음"

        plantNickNameTextView.text = nickname
        plantNameTextView.text = plantName
//        plantRemedyTextView.text = plantRemedy
        plantCheckDate.text = checkDate.toString()
        loadImage(imageUrl)

        nameEditButton.setOnClickListener {
            showEditDialog(plantNameTextView, "식물 종 이름을 입력하세요")
        }

//        remedyEditButton.setOnClickListener {
//            showEditDialog(plantRemedyTextView, "관리 방법을 입력하세요")
//        }

        addButton.setOnClickListener {
            val updatedNickname = plantNickNameTextView.text.toString()
            val updatedPlantName = plantNameTextView.text.toString() // EditText로부터 새로운 식물 이름 읽기
//            val updatedPlantRemedy = plantRemedyTextView.text.toString()
            val updatedCheckDateInterval = plantCheckDate.text.toString().toInt() // EditText로부터 새로운 검사 날짜 간격 읽기
            // 서버로 데이터 전송
            sendUpdatedPlantInfo(id, updatedNickname, updatedPlantName, updatedCheckDateInterval)

            val resultIntent = Intent().apply {
                putExtra("updatePlantName", updatedNickname)
                putExtra("updatePlantNickname", updatedPlantName)
                putExtra("updatePlantCheckDate", updatedCheckDateInterval)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun loadImage(url: String) {
        if (url.isNotEmpty()) {
            Glide.with(this)
                .load(url)
                .apply(RequestOptions.centerCropTransform())
                .into(imageView)
        }
    }

    private fun showEditDialog(textView: TextView, dialogHint: String) {
        val editText = EditText(this).apply {
            hint = dialogHint  // 사용자가 어떤 정보를 입력해야 하는지 힌트를 제공
            setSelectAllOnFocus(true)
        }
        AlertDialog.Builder(this)
            .setTitle("정보 수정")
            .setView(editText)
            .setPositiveButton("확인") { dialog, _ ->
                textView.text = editText.text.toString()
                dialog.dismiss()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    // 식물 정보를 서버로 전송하는 함수
    private fun sendUpdatedPlantInfo(plantId: Long, nickname: String, name: String, checkDateInterval: Int) {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userUuid = prefs.getString("userUuid", null)

        if (userUuid.isNullOrEmpty()) {
            Toast.makeText(this, "UUID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val json = JSONObject().apply {
            put("nickname", nickname)
            put("name", name)
            put("checkDateInterval", checkDateInterval)
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = json.toString().toRequestBody(mediaType)

        val plantid = plantId.toString()
        val url = BuildConfig.API_PLANT_EDIT.replace("{plantId}", plantid)

        val request = Request.Builder()
            .url(url)
            .put(requestBody) // HTTP PUT 메소드 사용
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@EditPlantActivity,
                        "업데이트 실패: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@EditPlantActivity,
                            "식물 정보가 성공적으로 업데이트 되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish() // 화면을 종료하여 변경 사항을 반영
                    } else {
                        Toast.makeText(
                            this@EditPlantActivity,
                            "서버 오류: ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

}