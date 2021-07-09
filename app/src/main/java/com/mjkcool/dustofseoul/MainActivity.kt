package com.mjkcool.dustofseoul

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private val STAT_LEVEL = mapOf(0 to "GOOD", 30 to "NORMAL", 80 to "BAD", 150 to "WORST")
    //서울시 구 이름 리스트
    private val GU_NAMES: Array<String> = resources.getStringArray(R.array.location_gu_array)

    @RequiresApi(Build.VERSION_CODES.O)
    private val DatetimeFormatter = DateTimeFormatter.ofPattern("MM월 dd일 h:mm")


    //XML VIEW COMPONENTS
    private lateinit var nowTimeView: TextView //현재 시간
    private lateinit var nowLocationView: TextView //현재 위치
    private lateinit var conPm10View: TextView //미세먼지 농도
    private lateinit var conPm25View: TextView //초미세먼지 농도
    private lateinit var statPm10View: TextView //미세먼지 상태
    private lateinit var statPm25View: TextView //초미세먼지 상태

    private lateinit var syncBtn: ImageButton





    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Log.d("구 리스트", guArray[0])

        //Initialize XML components
        nowTimeView = findViewById(R.id.nowtime_textview)
        nowLocationView = findViewById(R.id.nowlocation_textview)
        conPm10View = findViewById(R.id.pm10_con)
        conPm25View = findViewById(R.id.pm25_con)
        statPm10View = findViewById(R.id.pm10_stat)
        statPm25View = findViewById(R.id.pm25_stat)
        syncBtn = findViewById(R.id.sync_btn)

        syncBtn.setOnClickListener { sync() }



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
        var now = LocalDateTime.now()
        nowTimeView.text = now.format(DatetimeFormatter)
    }

}