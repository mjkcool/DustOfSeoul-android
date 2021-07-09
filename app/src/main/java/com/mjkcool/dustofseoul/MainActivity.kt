package com.mjkcool.dustofseoul

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    //서울시 구 이름 리스트
    val guArray: Array<String> = resources.getStringArray(R.array.location_gu_array)


    //XML VIEW COMPONENTS
    lateinit var nowTimeView: TextView //현재 시간
    lateinit var nowLocationView: TextView //현재 위치
    lateinit var conPm10View: TextView //미세먼지 농도
    lateinit var conPm25View: TextView //초미세먼지 농도
    lateinit var statPm10View: TextView //미세먼지 상태
    lateinit var statPm25View: TextView //초미세먼지 상태

    val STAT_LEVEL = mapOf(0 to "GOOD", 30 to "NORMAL", 80 to "BAD", 150 to "WORST")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Log.d("구 리스트", guArray[0])

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

}