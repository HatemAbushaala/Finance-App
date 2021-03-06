package com.hatemabushaala.financeapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Launcher : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pref = getSharedPreferences( "finance_app_pref", Context.MODE_PRIVATE)
        Common.defaultCurrency = pref.getString("currency","USD") ?: "USD"

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val url = "http://data.fixer.io/api/latest?access_key=b3192bf34cdc2527ccab34cf78bbbd0c&format=1"

// Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                // Display the first 500 characters of the response string.
                val rates = JSONObject(response).getJSONObject("rates")
                Common.rates = rates

                var i = 0
                for (item in rates.keys()){

                    if (item == Common.defaultCurrency )
                        Common.position = i
                    Common.currencies.add(item)
                    i++
                }

                // todo
                Common.favCurrencies = arrayListOf("USD","LYD","TRY","EUR","GBP","JOD")
              startActivity(Intent(this,MainActivity::class.java))
                 // startActivity(Intent(this,FaviorateCurrencies::class.java))
                finish()


              // Log.e("res",rates["USD"].toString())
                // Log.e("res",rates.toString())
            },
            Response.ErrorListener { err -> Log.e("res",err.localizedMessage) })

// Add the request to the RequestQueue.
        queue.add(stringRequest)



    }
}


object Common {

    lateinit var rates : JSONObject
    var currencies = ArrayList<String>()
    var favCurrencies = ArrayList<String>()
    var defaultCurrency = "EUR"
    var position = 0
}

/* currency rates response
*
*  url = "http://data.fixer.io/api/latest?access_key=b3192bf34cdc2527ccab34cf78bbbd0c&format=1"
* base is EUR and can't be changed on free version of api
*
* steps of convert
* 1. store rates json object
* 2. get rate from eur to user default currency
* 3. convert from default currency to (x) currency
*
* example convert 100$ to defualt currency (JD)
* steps
* convert 100$ to eur =>   "USD":1.14295, =>  87.49289120258979 (divide)
* convert eur to jd => (multiply)  0.810397 * 87.49289120258979 = 70.9
*
* 100GBP to JD
* 100GBP to EUR 100 / 0.909543 = 109.945324190280174 euro
* to JOD => 109.945324190280174 euro  * (eur to jod rate )  0.810397
* "rates":{
        "AED":4.198103,
        "AFN":88.179055,
        "ALL":124.387711,
        "AMD":555.039847,
        "ANG":2.051768,
        "AOA":652.06789,
        "ARS":81.681598,
        "AUD":1.633252,
        "AWG":2.05731,
        "AZN":1.947553,
        "BAM":1.956284,
        "BBD":2.307914,
        "BDT":96.914115,
        "BGN":1.954862,
        "BHD":0.431109,
        "BIF":2200.17878,
        "BMD":1.14295,
        "BND":1.589054,
        "BOB":7.892815,
        "BRL":6.156855,
        "BSD":1.143105,
        "BTC":0.000125,
        "BTN":85.700313,
        "BWP":13.199367,
        "BYN":2.739335,
        "BYR":22401.820308,
        "BZD":2.304013,
        "CAD":1.552252,
        "CDF":2201.322144,
        "CHF":1.072801,
        "CLF":0.032632,
        "CLP":900.420494,
        "CNY":7.99174,
        "COP":4175.310702,
        "CRC":665.429363,
        "CUC":1.14295,
        "CUP":30.288175,
        "CVE":110.642015,
        "CZK":26.641068,
        "DJF":203.125527,
        "DKK":7.446667,
        "DOP":66.867018,
        "DZD":146.675225,
        "EGP":18.233,
        "ERN":17.144659,
        "ETB":40.186571,
        "EUR":1,
        "FJD":2.46922,
        "FKP":0.909598,
        "GBP":0.909543,
        "GEL":3.520733,
        "GGP":0.909598,
        "GHS":6.583838,
        "GIP":0.909598,
        "GMD":59.209215,
        "GNF":10989.464807,
        "GTQ":8.798678,
        "GYD":239.142895,
        "HKD":8.862298,
        "HNL":28.44847,
        "HRK":7.5322,
        "HTG":126.678717,
        "HUF":353.289324,
        "IDR":16911.545612,
        "ILS":3.929943,
        "IMP":0.909598,
        "INR":85.636845,
        "IQD":1360.110519,
        "IRR":48123.910814,
        "ISK":160.162027,
        "JEP":0.909598,
        "JMD":163.622347,
        "JOD":0.810397,
        "JPY":122.34713,
        "KES":122.798989,
        "KGS":88.636213,
        "KHR":4687.238415,
        "KMF":492.440453,
        "KPW":1028.655014,
        "KRW":1375.689366,
        "KWD":0.351515,
        "KYD":0.952571,
        "KZT":473.30974,
        "LAK":10360.842291,
        "LBP":1728.525488,
        "LKR":212.451675,
        "LRD":227.818557,
        "LSL":19.030554,
        "LTL":3.374835,
        "LVL":0.69136,
        "LYD":1.589136,
        "MAD":10.943791,
        "MDL":19.501421,
        "MGA":4377.498957,
        "MKD":61.689304,
        "MMK":1569.524568,
        "MNT":3245.354605,
        "MOP":9.129216,
        "MRO":408.033282,
        "MUR":45.672715,
        "MVR":17.605706,
        "MWK":840.068656,
        "MXN":25.768384,
        "MYR":4.871829,
        "MZN":80.526588,
        "NAD":19.030549,
        "NGN":442.897391,
        "NIO":39.436033,
        "NOK":10.621401,
        "NPR":137.119188,
        "NZD":1.742701,
        "OMR":0.43999,
        "PAB":1.143105,
        "PEN":4.018898,
        "PGK":3.960366,
        "PHP":56.416441,
        "PKR":191.301303,
        "PLN":4.47797,
        "PYG":7899.116189,
        "QAR":4.161525,
        "RON":4.837008,
        "RSD":117.621413,
        "RUB":82.15148,
        "RWF":1085.802515,
        "SAR":4.286781,
        "SBD":9.487676,
        "SCR":20.17356,
        "SDG":63.233753,
        "SEK":10.347024,
        "SGD":1.589078,
        "SHP":0.909598,
        "SLL":11155.19254,
        "SOS":665.197296,
        "SRD":8.524165,
        "STD":25025.081475,
        "SVC":10.001793,
        "SYP":586.519734,
        "SZL":19.03054,
        "THB":36.18927,
        "TJS":11.782632,
        "TMT":4.011755,
        "TND":3.225448,
        "TOP":2.609245,
        "TRY":7.839156,
        "TTD":7.730486,
        "TWD":33.681637,
        "TZS":2651.64442,
        "UAH":31.327063,
        "UGX":4224.794766,
        "USD":1.14295,
        "UYU":50.226009,
        "UZS":11679.806593,
        "VEF":11.415218,
        "VND":26507.296764,
        "VUV":131.144766,
        "WST":3.063361,
        "XAF":656.109131,
        "XAG":0.05906,
        "XAU":0.000631,
        "XCD":3.08888,
        "XDR":0.823891,
        "XOF":658.339589,
        "XPF":119.610135,
        "YER":286.137952,
        "ZAR":19.067039,
        "ZMK":10287.925788,
        "ZMW":20.848322,
        "ZWL":368.030124
      }
* */