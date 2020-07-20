package com.hatemabushaala.financeapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_curencies.*

class CurenciesActivity : AppCompatActivity() {

    var list = ArrayList<String>()
    lateinit var adp : ArrayAdapter<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_curencies)

        list.addAll(Common.favCurrencies)
         adp = ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,list)
        currenciesLv.adapter = adp
        currenciesLv.setOnItemClickListener { parent, view, position, id ->


            val resIntent = Intent()
            resIntent.putExtra("currency",adp.getItem(position))
            setResult(Activity.RESULT_OK, resIntent)
            finish()
        }



        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.e("txt",newText.toString()
                )
                filter(newText ?: "")
                return false
            }
        })



    }

    fun filter(newTxt : String){

        list.clear()

        for (c in Common.currencies){
            if (c.contains(newTxt.toUpperCase()))
                list.add(c)
        }
        adp.notifyDataSetChanged()
    }
}