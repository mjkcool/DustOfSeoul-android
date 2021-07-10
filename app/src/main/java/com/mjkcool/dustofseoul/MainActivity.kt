package com.mjkcool.dustofseoul

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.mjkcool.dustofseoul.kakaodata.Coord2regioncode
import com.mjkcool.dustofseoul.kakaodata.KakaoAPI
import com.mjkcool.dustofseoul.kakaodata.kakaoAPIRetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {
    //미세먼지 등급
    private val STAT_LEVEL = mapOf(0 to "GOOD", 30 to "NORMAL", 80 to "BAD", 150 to "WORST")

    //Datetime formatter
    @RequiresApi(Build.VERSION_CODES.O)
    private val DatetimeFormatter = DateTimeFormatter.ofPattern("M월 d일 h:mm")

    //XML VIEW COMPONENTS
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var nowTimeView: TextView //현재 시간
    private lateinit var nowLocationView: TextView //현재 위치
    private lateinit var conPm10View: TextView //미세먼지 농도
    private lateinit var conPm25View: TextView //초미세먼지 농도
    private lateinit var statPm10View: TextView //미세먼지 상태
    private lateinit var statPm25View: TextView //초미세먼지 상태
    private lateinit var syncBtn: ImageButton

    //위치 불러오기 매니저
    lateinit var mLocationManager: LocationManager

    private var kakaoApi = kakaoAPIRetrofitClient.apiService


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //서울시 구 이름 리스트
        val GU_NAMES: Array<String> = resources.getStringArray(R.array.location_gu_array)






        //Initialize XML components
        mainLayout = findViewById(R.id.rootlayout)
        nowTimeView = findViewById(R.id.nowtime_textview)
        nowLocationView = findViewById(R.id.nowlocation_textview)
        conPm10View = findViewById(R.id.pm10_con)
        conPm25View = findViewById(R.id.pm25_con)
        statPm10View = findViewById(R.id.pm10_stat)
        statPm25View = findViewById(R.id.pm25_stat)
        syncBtn = findViewById(R.id.sync_btn)


        //최초 위치 권한 요청 &  날짜시간 sync
        tedPermission()
        getTime()


        syncBtn.setOnClickListener {
            sync()
        }



        val spinner: Spinner = findViewById(R.id.gu_spinner)

        ArrayAdapter.createFromResource(this,
            R.array.location_gu_array,
            R.layout.spinner_view
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }



    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sync(){
        getTime() //현재 시간 표시
        getLocation() //위치권한 받기 & 위치 불러오기
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTime(){
        var now = LocalDateTime.now()
        nowTimeView.text = now.format(DatetimeFormatter)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun tedPermission(){
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                getLocation()
            }
            override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {
                makeSnackBar("설정에서 권한을 허가 해주세요.")
            }
        }

        TedPermission.with(this)
            .setPermissionListener(permissionListener)
            .setRationaleMessage("서비스 사용을 위해서 위치 권한이 필요합니다.")
            .setDeniedMessage("설정에서 권한을 설정할 수 있습니다.")
            .setPermissions(
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .check()
    }

    private fun getLocation(){
        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if(ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED){
            //위도&경도 좌표값 불러오기 with 네트워크
            val locationProvider = LocationManager.NETWORK_PROVIDER
            var currentLatLng = mLocationManager.getLastKnownLocation(locationProvider)


            getGu(currentLatLng?.latitude, currentLatLng?.longitude)

            //Connect Dataseoul API

        }
    }

    private fun getGu(latitude: Double?, longitude: Double?){
        callkakaoApi(latitude.toString(), longitude.toString())
    }


    private fun makeSnackBar(s: String) {
        Snackbar.make(mainLayout, s, Snackbar.LENGTH_LONG).show()
    }

    fun callkakaoApi(lat: String, lon: String) { //only called by method 'getGu'
        //var returnval = "위치 로드 실패" //response value of this method

        //Connect Kakao API
        kakaoApi.getApiGu(key = KakaoAPI.API_KEY, x = lon, y = lat)
            .enqueue(object : Callback<Coord2regioncode> {
                override fun onResponse(call: Call<Coord2regioncode>, response: Response<Coord2regioncode>) {
                    var gu = response.body()!!.documents[0].region_2depth_name
                    nowLocationView.text = gu

                }
                override fun onFailure(call: Call<Coord2regioncode>, t: Throwable) {
                    nowLocationView.text = "위치 로드 실패"
                }

            })
    }

    fun callDataSeoulApi(){
        //Connect Dataseoul API

    }

}