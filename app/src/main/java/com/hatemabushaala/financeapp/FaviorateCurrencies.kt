package com.hatemabushaala.financeapp

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.SearchView
import kotlinx.android.synthetic.main.activity_curencies.*
import kotlinx.android.synthetic.main.activity_curencies.currenciesLv
import kotlinx.android.synthetic.main.activity_curencies.searchView
import kotlinx.android.synthetic.main.activity_faviorate_currencies.*

class FaviorateCurrencies : AppCompatActivity() {

    var list = ArrayList<String>()
    lateinit var adp : ArrayAdapter<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faviorate_currencies)


        list.addAll(Common.currencies)
        adp = ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,list)
        currenciesLv.adapter = adp
        currenciesLv.setOnItemClickListener { parent, view, position, id ->


            view.setBackgroundColor(Color.BLUE)
            Common.favCurrencies.add(adp.getItem(position).toString())

//
//            val resIntent = Intent()
//            resIntent.putExtra("currency",adp.getItem(position))
//            setResult(Activity.RESULT_OK, resIntent)
//            finish()
        }

        submitFavCurrencies.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
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