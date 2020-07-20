package com.hatemabushaala.financeapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_add_account.*
import kotlinx.android.synthetic.main.layout_add_transaction.*
import kotlinx.android.synthetic.main.layout_edit_account.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*


// todo : transaction edit - delete trans -
class MainActivity : AppCompatActivity() {



    lateinit var addTransactionSheet : BottomSheetDialog
    lateinit var addCurrencyBtn : Button
    val GET_CURRENCY = 200
    var selectedCurrency = ""
    set(value) {
        field = value
      try {
          addCurrencyBtn.text = value
      }catch (e:Exception){}
    }
    lateinit var currenciesAdapter : ArrayAdapter<String>
    lateinit var accountsAdapter : AccountsAdapter
    lateinit var accountsSpinnerAdapter : ArrayAdapter<Accounts>
    var accountsList = ArrayList<Accounts>()


    lateinit var transactionsAdapter: TransactionsAdapter
    var transactionsList = ArrayList<Transactions>()


        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
                super.onActivityResult(requestCode, resultCode, data)

                if (requestCode == GET_CURRENCY && resultCode == Activity.RESULT_OK)
                {
                    val selectedCurrency = data?.getStringExtra("currency") ?: "JOD"
                    currencyBtn.text = selectedCurrency
                    Common.defaultCurrency = selectedCurrency
                    val pref = getSharedPreferences( "finance_app_pref",Context.MODE_PRIVATE)
                    pref?.edit()?.putString("currency",selectedCurrency)?.apply()
                    updateBalance()
                }else if(requestCode == 202 && resultCode == Activity.RESULT_OK){
                   accountsAdapter.currency =   data?.getStringExtra("currency") ?: "JOD"
                }else if(requestCode == 203 && resultCode == Activity.RESULT_OK){
                    selectedCurrency =   data?.getStringExtra("currency") ?: "JOD"
                }else if (requestCode == 204  && resultCode == Activity.RESULT_OK){
                    addTransactionSheet.transactionCurrnecyBtn.text = data?.getStringExtra("currency") ?: "JOD"
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        currencyBtn.text = Common.defaultCurrency
        currencyBtn.setOnClickListener {
             startActivityForResult(Intent(this,CurenciesActivity::class.java),GET_CURRENCY)
        }
        currenciesAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_item,Common.favCurrencies)









        val db = AppDatabase.getDatabase(this)

        accountsSpinnerAdapter = ArrayAdapter<Accounts>(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,accountsList)

        accountsAdapter = AccountsAdapter(this,accountsList)

        accountsAdapter.updateListener = {
            updateBalance()
        }
        accountRv.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        accountRv.adapter = accountsAdapter

        transactionsAdapter = TransactionsAdapter(this,transactionsList)
        val itemDecor = DividerItemDecoration(this,LinearLayoutManager.VERTICAL)

        transactionsRv.layoutManager = LinearLayoutManager(this)
        transactionsRv.adapter = transactionsAdapter
        transactionsRv.addItemDecoration(itemDecor)

        CoroutineScope(Dispatchers.IO).launch {


            val accounts = db.userDao().getAll()
            val tr = db.transactionDao().getAll()

            MainScope().launch {


                accountsList.addAll(accounts)
                accountsAdapter.notifyDataSetChanged()
                accountsSpinnerAdapter.notifyDataSetChanged()

                transactionsList.addAll(tr)
                transactionsAdapter.notifyDataSetChanged()
                Log.e("user", accounts.toString())
                Log.e("user", tr.toString())

//                val userTransactions = db.userDao().getUserWithTransactions()
//                Log.e("user", userTransactions.toString())

                updateBalance()

            }
        }


        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object :
            ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.DOWN or ItemTouchHelper.UP
            ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                //Remove swiped item from list and notify the RecyclerView

                CoroutineScope(Dispatchers.IO).launch {
                    val position = viewHolder.adapterPosition

                    db.transactionDao().delete(transactionsList[position])
                    db.userDao().updateById(  transactionsList[position].amount,transactionsList[position].accountId)
                    MainScope().launch {

                        val amount = transactionsList[position].amount
                        for (account in accountsList)
                        {

                            if (account.uid == transactionsList[position].accountId) {

                                // refund to account
                                if (transactionsList[position].isExpense) account.balance += amount else account.balance -= amount
                                accountsAdapter.notifyDataSetChanged()
                                break
                            }
                        }
                        transactionsList.removeAt(position)
                        transactionsAdapter.notifyDataSetChanged()
                        updateBalance()

                    }
                }

            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(transactionsRv)



        addAccountBtn.setOnClickListener {

            BottomSheetDialog(this).apply {

                setOnDismissListener {
                    selectedCurrency = ""
                }


                this.setContentView(R.layout.layout_add_account)


                addCurrencyBtn = addSelectCurrencyBtn
//                addCurrencyBtn.text = Common.defaultCurrency
                addSelectCurrencyBtn.setOnClickListener {
                     startActivityForResult(Intent(this@MainActivity,CurenciesActivity::class.java),203)
                }



                this.confirmAddAccountBtn.setOnClickListener {

                    val name = etAccountName.text.toString()
                    val balance = etAccountBalance.text.toString().toDouble()
                    val currency = selectedCurrency

                    val newAccount = Accounts(null,balance,name,currency)

                    if (name.isEmpty()){
                        Toast.makeText(this@MainActivity, "enter name", Toast.LENGTH_SHORT).show();
                        return@setOnClickListener
                    }
                    if (selectedCurrency.isEmpty()){
                        Toast.makeText(this@MainActivity, "please select a currency ", Toast.LENGTH_SHORT).show();
                        return@setOnClickListener
                    }

                    selectedCurrency = ""


                    CoroutineScope(Dispatchers.IO).launch {
                        var id = db.userDao().insertAll(newAccount)
                        newAccount.uid = id
                        accountsList.add(newAccount)

                        MainScope().launch {
                            dismiss()
                            accountsAdapter.notifyDataSetChanged()

                            updateBalance()
                          //  recreate()
                        }

                    }


                    Log.e("u","$name $balance $currency")
                }
                show()
            }
        }

        addTransactionBtn.setOnClickListener {

            addTransactionSheet =  BottomSheetDialog(this).apply {
                this.setContentView(R.layout.layout_add_transaction)


                transactionCurrnecyBtn.setOnClickListener {
                     startActivityForResult(Intent(this@MainActivity,CurenciesActivity::class.java),204)
                }
                accountSpinner.adapter = accountsSpinnerAdapter
                accountSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {

                        transactionCurrnecyBtn.text =( accountSpinner.selectedItem as Accounts).currency

                    }
                }



                this.confirmAddTransactionBtn.setOnClickListener {

                    val purchase = etPurchaseName.text.toString()
                    var amount = etTransactionAmount.text.toString().toDouble()
                    val account = accountSpinner.selectedItem as Accounts

                    if (purchase.isEmpty()){
                        Toast.makeText(this@MainActivity, "enter name", Toast.LENGTH_SHORT).show();
                        return@setOnClickListener
                    }
                    if (amount.toInt() == 0){
                        Toast.makeText(this@MainActivity, "enter valid amount ", Toast.LENGTH_SHORT).show();
                        return@setOnClickListener
                    }

                    val selectedCurrency = ( accountSpinner.selectedItem as Accounts).currency
                    amount = getRate(amount,transactionCurrnecyBtn.text.toString(),selectedCurrency)



                    val newTransactions = Transactions(null,amount,account.uid!!,!checkBox_profit.isChecked,purchase)
                    CoroutineScope(Dispatchers.IO).launch {

                        // todo check for negative balance

                        db.transactionDao().insertAll(newTransactions)
                        transactionsList.add(newTransactions)

                        if (newTransactions.isExpense) account.balance -= amount else account.balance += amount

                        db.userDao().update(account)

                        MainScope().launch {
                          //  recreate()
                            dismiss()
                            accountsAdapter.notifyDataSetChanged()
                            transactionsAdapter.notifyDataSetChanged()
                            updateBalance()

                        }

                    }


                }
                show()
            }

        }





    }

    fun updateBalance(){
        balanceTv.setText(accountsList.sumByDouble { acc -> getRate(acc.balance ,acc.currency,Common.defaultCurrency) }.toInt().toString() + Common.defaultCurrency)

    }
}


@Database(entities = arrayOf(Accounts::class,Transactions::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao


    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "word_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}

@Entity
data class Accounts(
        @PrimaryKey var uid: Long?,
        var balance : Double,
        var name : String,
        var currency: String
){
    override fun toString(): String {
        return name
    }
}

@Entity
data class Transactions(
        @PrimaryKey var  uid: Long?,
         var amount: Double,
         var accountId: Long,
        var isExpense : Boolean,
        var purchase : String
)

data class UserWithTransactions(
        @Embedded val accounts: Accounts,
        @Relation(
                parentColumn = "uid",
                entityColumn = "accountId"
        )
        val transactions : List<Transactions>
)

@Dao
interface UserDao {

    @Query("SELECT * FROM accounts")
    suspend fun getAll(): List<Accounts>

    @Query("update accounts set balance = balance + :balance where uid = :id")
    suspend fun updateById(balance: Double,id:Long)

    @Insert
    suspend fun insertAll( accounts: Accounts) : Long

    @Delete
    suspend fun delete(accounts: Accounts)

    @Update
    suspend fun update(accounts: Accounts)

    @Transaction
    @Query("SELECT * FROM Accounts")
    suspend fun getUserWithTransactions(): List<UserWithTransactions>
}

@Dao
interface TransactionDao{

    @Query("SELECT * FROM transactions")
    suspend fun getAll(): List<Transactions>

    @Insert
    suspend fun insertAll(vararg transactions: Transactions)

    @Update
    suspend fun update(transactions: Transactions)

    @Delete
    suspend fun delete(transactions: Transactions)

}



// adapters

class AccountsAdapter(var act: Activity,var items : ArrayList<Accounts>) : RecyclerView.Adapter<AccountsAdapter.ViewHolder>() {


    var currency = ""
    set(value) {
        field = value

        try {
            currencyBtnRef.text = value
        }catch (e:Exception){}
    }
    lateinit var currencyBtnRef : Button


   lateinit var updateListener : () -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_account, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = items[position]
        holder.itemView.setOnClickListener {
            BottomSheetDialog(act).apply {

                setContentView(R.layout.layout_edit_account)

                if (Common.defaultCurrency != item.currency){
                    convertTv.visibility = View.VISIBLE
                    val amountInUserCurrency =String.format("%.1f", getRate(item.balance,item.currency,Common.defaultCurrency))
                    convertTv.text = "${amountInUserCurrency}${Common.defaultCurrency}"
                }else{
                    convertTv.visibility = View.GONE
                }

                etEditAccountBalance.setText( String.format("%.1f", item.balance) )
                etEditAccountName.setText(item.name)
                val db = AppDatabase.getDatabase(context)

                deleteAccountTv.setOnClickListener {

                    CoroutineScope(Dispatchers.IO).launch {
                        db.userDao().delete(item)

                        MainScope().launch {
                            items.removeAt(position)
                            notifyDataSetChanged()
                            dismiss()
                            updateListener()
                        }


                    }

                }
/*
                editCurrencySpinner.adapter = ArrayAdapter<String>(context,android.R.layout.simple_spinner_dropdown_item,Common.currencies)
                editCurrencySpinner.setPadding(4,4,4,4)

                val count = editCurrencySpinner.adapter.count

                for (i in 0..count-1){
                    val currency = editCurrencySpinner.adapter.getItem(i)
                    if (currency == item.currency) {
                        editCurrencySpinner.setSelection(i)
                        break
                    }
                }*/


                currencyBtnRef = editSelectCurrencyBtn
                editSelectCurrencyBtn.text = item.currency




                editSelectCurrencyBtn.setOnClickListener {
                    act.startActivityForResult(Intent(context,CurenciesActivity::class.java),202)
                }

                confirmEditAccountBtn.setOnClickListener {


                    if (etEditAccountName.text.isNullOrEmpty()){
                    Toast.makeText(context, "please enter a name", Toast.LENGTH_SHORT).show();
                    return@setOnClickListener
                }
                    if (etEditAccountBalance.text.isNullOrEmpty()){
                        Toast.makeText(context, "please enter valid amount", Toast.LENGTH_SHORT).show();
                        return@setOnClickListener
                    }


                    CoroutineScope(Dispatchers.IO).launch {


                        item.balance = etEditAccountBalance.text.toString().toDouble()
                        item.name = etEditAccountName.text.toString()
                       if(currency != "") {
                           item.currency = currency
                           currency = ""
                       }
                        db.userDao().update(item)




                        MainScope().launch {
                            dismiss()
                            notifyDataSetChanged()
                            updateListener()
                        }

                    }
                }
                show()
            }
        }
        holder.bind(position,item)
    }

    fun setRecyclerData(data: List<Accounts>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorsArray = arrayListOf<IntArray>(
            intArrayOf(0xFF00E676.toInt(),0xFF1BAAAF.toInt()),
            intArrayOf(0xFFF50057.toInt(),0xFF8F135E.toInt()),
            intArrayOf(0xFF00B0FF.toInt(),0xFF00E5FF.toInt())
        )
            // intArrayOf(0xFF00E676.toInt(),0xFFF50057.toInt(),0xFF00B0FF.toInt())
        val accountBalanceTv = itemView.findViewById<TextView>(R.id.accountBalanceTv)
        val accountNameTv = itemView.findViewById<TextView>(R.id.accountNameTv)
        val accountCard = itemView.findViewById<LinearLayout>(R.id.accountCard)

        fun bind(position: Int,data: Accounts) {
            accountNameTv.text = data.name




            val colorIndex = position % colorsArray.size

            val gd = GradientDrawable(
                GradientDrawable.Orientation.TL_BR, colorsArray[colorIndex]
            )
            // gd.setCornerRadius(0f)

            accountCard.background = gd


            accountBalanceTv.text = "${ data.balance.toInt()}${data.currency}"

        }
    }

}

class TransactionsAdapter(var context: Context,var items : ArrayList<Transactions>) : RecyclerView.Adapter<TransactionsAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_transaction, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transactionPurchaseNameTv = itemView.findViewById<TextView>(R.id.transactionPurchaseTv)
        val transactionAmountTv = itemView.findViewById<TextView>(R.id.transactionAmountTv)

        fun bind(data: Transactions) {

            val sign = if (data.isExpense) "-" else "+"
            transactionPurchaseNameTv.text = data.purchase
            transactionAmountTv.text = "$sign${data.amount.toInt()}"
        }
    }

}

// from currency to JD
fun getRate(amount:Double, from: String,to:String):Double{

    // convert to euro
    val toEuro = amount / Common.rates[from].toString().toDouble()
    // return amount in default currency
    return toEuro * Common.rates[to].toString().toDouble()

}
