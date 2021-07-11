package com.mjkcool.dustofseoul

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.Color.rgb
import android.graphics.Paint
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.mjkcool.dustofseoul.dataseouldata.TimeAverageAirQuality
import com.mjkcool.dustofseoul.dataseouldata.dataSeoulAPIRetrofitClient
import com.mjkcool.dustofseoul.dataseouldata.dataseoulAPI
import com.mjkcool.dustofseoul.kakaodata.Coord2regioncode
import com.mjkcool.dustofseoul.kakaodata.KakaoAPI
import com.mjkcool.dustofseoul.kakaodata.kakaoAPIRetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {
    //미세먼지 등급
    private val STAT_LEVEL_PM10 = mapOf("GOOD" to 0, "NORMAL" to 30, "BAD" to 80, "WORST" to 150)
    private val COLOR_LEVEL = mapOf("GOOD" to rgb(0, 147, 206), "NORMAL" to rgb(0, 206, 86), "BAD" to rgb(194, 158, 0), "WORST" to rgb(194, 23, 0))
    private val STAT_LEVEL_PM25 = mapOf("GOOD" to 0, "NORMAL" to 15, "BAD" to 35, "WORST" to 75)
    private val NAME_LEVEL = mapOf("GOOD" to "좋음", "NORMAL" to "보통", "BAD" to "나쁨", "WORST" to "매우 나쁨")

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
    private lateinit var syncBtn: LinearLayout
    private lateinit var syncIcon: ImageView
    private lateinit var spinner: Spinner
    private lateinit var moveToOfficialPage: Button
    private lateinit var showInfoBtn: LinearLayout
    private lateinit var appInfoTxt: TextView
    private lateinit var applogoBtn: ImageButton

    private lateinit var mLocationManager: LocationManager //위치 불러오기 매니저
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //API client manager
    private var kakaoApi = kakaoAPIRetrofitClient.apiService
    private var dataSeoulApi = dataSeoulAPIRetrofitClient.apiService

    private lateinit var GU_NAMES: Array<String>

    private lateinit var fadeInAnim: Animation
    private lateinit var fadeOutAnim: Animation

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //서울시 구 이름 리스트
        GU_NAMES = resources.getStringArray(R.array.location_gu_array)

        //Initialize XML components
        mainLayout = findViewById(R.id.rootlayout)
        nowTimeView = findViewById(R.id.nowtime_textview)
        nowLocationView = findViewById(R.id.nowlocation_textview)
        conPm10View = findViewById(R.id.pm10_con)
        conPm25View = findViewById(R.id.pm25_con)
        statPm10View = findViewById(R.id.pm10_stat)
        statPm25View = findViewById(R.id.pm25_stat)
        syncBtn = findViewById(R.id.sync_comp_layout)
        syncIcon = findViewById(R.id.sync_btn)
        moveToOfficialPage = findViewById(R.id.move_to_dust_page)
        moveToOfficialPage.paintFlags = moveToOfficialPage.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        showInfoBtn = findViewById(R.id.show_info_app_btn)
        appInfoTxt = findViewById(R.id.info_msg)
        applogoBtn = findViewById(R.id.applogo_btn)

        fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        fadeOutAnim = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        showInfoBtn.setOnClickListener {
            appInfoTxt.visibility = View.VISIBLE
            appInfoTxt.startAnimation(fadeInAnim)
            Handler().postDelayed({
                appInfoTxt.startAnimation(fadeOutAnim)
                appInfoTxt.visibility = View.INVISIBLE
            }, 2000L)
        }

        applogoBtn.setOnClickListener {

        }

        //최초 위치 권한 요청 &  날짜시간 sync
        getTime()
        tedPermission()


        var rotateanim = ObjectAnimator.ofFloat(syncIcon, "rotation", 360F).apply {
            duration = 1000
            addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?) {

                }
            })
        }

        syncBtn.setOnClickListener {
            rotateanim.start()
            sync()
        }

        spinner = findViewById(R.id.gu_spinner)

        ArrayAdapter.createFromResource(this,
            R.array.location_gu_array,
            R.layout.spinner_view
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(getNetworkState()) callDataSeoulApi(GU_NAMES[position])
                else makeSnackBar("인터넷에 연결되어 있지 않습니다", Snackbar.LENGTH_LONG)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        moveToOfficialPage.setOnClickListener {
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.me.go.kr/mamo/web/index.do?menuId=16201"))
            startActivity(intent)
        }

        if(checkLocationPermission()){ //위치 권한 허용시
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    if (locationResult != null) {
                        return
                    }
                    for (location in locationResult.locations) {
                        if (location != null) {
                            val latitude = location.latitude
                            val longitude = location.longitude
                            getGu(latitude, longitude)
                        }
                    }
                }
            }
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 20 * 1000
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getNetworkState(): Boolean {
        var networkManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return networkManager.activeNetwork != null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sync(){
        if(getNetworkState()){ //인터넷 연결시
            getTime() //현재 시간 표시
            getLocation() //위치권한 받기 & 위치 불러오기
            //makeSnackBar("정보를 새로고침했습니다.", Snackbar.LENGTH_SHORT)
        }else{
            makeSnackBar("인터넷에 연결되어 있지 않습니다", Snackbar.LENGTH_LONG)
        }

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
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                makeSnackBar("설정에서 권한을 허가 해주세요.", Snackbar.LENGTH_LONG)
            }
        }
        TedPermission.with(this)
            .setPermissionListener(permissionListener)
            .setRationaleMessage("서비스 사용을 위해서 위치 권한이 필요합니다.")
            .setDeniedMessage("설정에서 권한을 설정할 수 있습니다.")
            .setPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .check()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getLocation(){
        if(getNetworkState()) { //인터넷 연결시
            if(checkLocationPermission()){
                //위도&경도 좌표값 불러오기 with 네트워크
                val locationProvider = LocationManager.NETWORK_PROVIDER
                var currentLatLng = mLocationManager.getLastKnownLocation(locationProvider)

                if(currentLatLng == null){
                    nowLocationView.text = "위치정보 OFF"
                    makeSnackBar("위치 정보를 켜주세요", Snackbar.LENGTH_LONG)
                }else{
                    getGu(currentLatLng?.latitude, currentLatLng?.longitude)
                }
            }
        }else{
            makeSnackBar("인터넷에 연결되어 있지 않습니다", Snackbar.LENGTH_LONG)
            nowLocationView.text = "위치 로드 실패"
        }

    }

    private fun checkLocationPermission(): Boolean {
        if(ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) { //위치 권한 허용시
            return true
        }
        return false
    }

    private fun getGu(latitude: Double?, longitude: Double?){
        callkakaoApi(latitude.toString(), longitude.toString())
    }


    private fun makeSnackBar(s: String, time: Int) {
        Snackbar.make(mainLayout, s, time).show()
    }

    fun callkakaoApi(lat: String, lon: String) { //only called by method 'getGu'
        //Connect Kakao API
        kakaoApi.getApiGu(key = KakaoAPI.API_KEY, x = lon, y = lat)
            .enqueue(object : Callback<Coord2regioncode> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<Coord2regioncode>, response: Response<Coord2regioncode>) {
                    if(response.body()!!.documents[0].region_1depth_name == "서울특별시"){
                        //Get information of 'gu' location
                        var gu = response.body()!!.documents[0].region_2depth_name
                        nowLocationView.text = gu //sync에 표시
                        //Spinner에 표시
                        spinner.setSelection(GU_NAMES.indexOf(gu))
                    }else{
                        nowLocationView.text = "서울 외 지역"
                    }
                }
                override fun onFailure(call: Call<Coord2regioncode>, t: Throwable) {
                    nowLocationView.text = "위치 로드 실패"
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun callDataSeoulApi(gu: String){
        var now = LocalDateTime.now()
        var formatting = if(now.hour == 0){
            var yesterday = now.minusDays(1)
            leadingZeros(yesterday.year, 4) + leadingZeros(yesterday.monthValue, 2) + leadingZeros(yesterday.dayOfMonth, 2) + 23
        }else{
            leadingZeros(now.year, 4) + leadingZeros(now.monthValue, 2) + leadingZeros(now.dayOfMonth, 2) + leadingZeros(now.hour-1, 2)
        }

        //Connect Dataseoul API
        dataSeoulApi.getApiQulity(key = dataseoulAPI.API_KEY, format = formatting, gu = gu)
            .enqueue(object : Callback<TimeAverageAirQuality>{
                override fun onResponse(call: Call<TimeAverageAirQuality>, response: Response<TimeAverageAirQuality>) {
                    var data = response.body()!!
                    if(data.TimeAverageAirQuality.RESULT.CODE != "INFO-000"){
                        makeSnackBar("미세먼지 정보가 없습니다", Snackbar.LENGTH_SHORT)
                    }
                    else{
                        var pm10 = data.TimeAverageAirQuality.row[0].PM10
                        var pm25 = data.TimeAverageAirQuality.row[0].PM25
                        setAirQulity(pm10, pm25)
                    }
                }
                override fun onFailure(call: Call<TimeAverageAirQuality>, t: Throwable) {
                    makeSnackBar("미세먼지 정보 불러오기에 실패했습니다", Snackbar.LENGTH_SHORT)
                }
            })
    }

    private fun setAirQulity(pm10: Int, pm25: Int) {
        conPm10View.text = pm10.toString()
        conPm25View.text = pm25.toString()

        when{
            pm10 > STAT_LEVEL_PM10["WORST"]!! -> setLevelPm10("WORST")
            pm10 > STAT_LEVEL_PM10["BAD"]!! -> setLevelPm10("BAD")
            pm10 > STAT_LEVEL_PM10["NORMAL"]!! -> setLevelPm10("BAD")
            pm10 > STAT_LEVEL_PM10["GOOD"]!! -> setLevelPm10("GOOD")
        }
        when{
            pm25 > STAT_LEVEL_PM25["WORST"]!! -> setLevelPm25("WORST")
            pm25 > STAT_LEVEL_PM25["BAD"]!! -> setLevelPm25("BAD")
            pm25 > STAT_LEVEL_PM25["NORMAL"]!! -> setLevelPm25("BAD")
            pm25 > STAT_LEVEL_PM25["GOOD"]!! -> setLevelPm25("GOOD")
        }
    }

    fun setLevelPm10(level: String){
        conPm10View.setTextColor(COLOR_LEVEL[level]!!)
        statPm10View.text = NAME_LEVEL[level]!!
        statPm10View.setTextColor(COLOR_LEVEL[level]!!)
    }

    fun setLevelPm25(level: String){
        conPm25View.setTextColor(COLOR_LEVEL[level]!!)
        statPm25View.text = NAME_LEVEL[level]!!
        statPm25View.setTextColor(COLOR_LEVEL[level]!!)
    }

    fun leadingZeros(num: Int, digits: Int): String {
        var zero = ""
        var time = num.toString()
        if (time.length < digits) {
            for (i in 0 until (digits - time.length)) zero += "0"
        }
        return zero + time;
    }
}