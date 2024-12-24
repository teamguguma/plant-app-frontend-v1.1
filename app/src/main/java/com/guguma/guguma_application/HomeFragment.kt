import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.guguma.guguma_application.PlantAdapter
import com.guguma.guguma_application.databinding.FragmentHomeBinding
import com.guguma.guguma_application.dto.PlantDto
import com.guguma.guguma_application.viewmodel.PlantViewModel
import com.guguma.guguma_application.viewmodel.PlantViewModelFactory
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {
    companion object {
        const val REQUEST_ADD_PLANT = 1001 // DetailPlantActivity의 요청 코드
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val plantViewModel: PlantViewModel by lazy {
        val prefs = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val userId = prefs.getString("userUuid", "default_user_id") // 기본값 제공
        val factory = PlantViewModelFactory(userId!!)
        ViewModelProvider(this, factory).get(PlantViewModel::class.java)
    }

//    private val addPlantLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val data = result.data
//            val plantId = data?.getLongExtra("plantId", -1L) ?: -1L
//            val newPlantName = data?.getStringExtra("newPlantName")
//            val newPlantImageUrl = data?.getStringExtra("newPlantImageUrl")
//
//            if (plantId != -1L && !newPlantName.isNullOrEmpty() && !newPlantImageUrl.isNullOrEmpty()) {
//                val newPlant = PlantDto(plantId, newPlantName, newPlantImageUrl)
//                plantViewModel.addPlant(newPlant) // ViewModel에 데이터 추가
//            }
//        }
//    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val adapter = PlantAdapter(requireContext())

        binding.plantListView.layoutManager = LinearLayoutManager(requireContext())
//        binding.plantListView.adapter = PlantAdapter(requireContext(), mutableListOf())
        binding.plantListView.adapter = adapter
        plantViewModel.plantList.observe(viewLifecycleOwner) { updatedPlantList ->
//            updateUI(updatedPlantList)
            adapter.updateData(updatedPlantList)
        }
        return binding.root
    }



    private val addPlantResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val id = data.getLongExtra("id", -1L) // 기본값을 -1L로 설정
                val name = data.getStringExtra("name")?: "unknown"
                val nickname = data.getStringExtra("nickname")?: "unknown"
                val creatDate = data.getStringExtra("createDate")?: "unknown"
                val checkDate = data.getIntExtra("checkdate", 1)
                val remedy = data.getStringExtra("remedy")?: "unknown"
                val imageUrl = data.getStringExtra("imageUrl")?: "unknown"
                val newPlant = PlantDto(id, creatDate, name, checkDate, nickname, remedy, imageUrl) // ID는 자동 생성되거나 즉시 필요하지 않다고 가정
                plantViewModel.addPlant(newPlant)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        plantViewModel.fetchPlantsFromServer() // 서버에서 최신 데이터를 가져옴
    }


    private fun updateUI(plantList: MutableList<PlantDto>) {
        val adapter = binding.plantListView.adapter as? PlantAdapter
        adapter?.updateData(plantList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}