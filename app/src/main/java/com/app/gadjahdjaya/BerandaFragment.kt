package com.app.gadjahdjaya

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gadjahdjaya.model.MenuItem
import com.app.gadjahdjaya.ui.menu.MenuFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class BerandaFragment : Fragment(), MenuAdapterBeranda.OnItemClickListener {

    private lateinit var userNameTextView: TextView
    private lateinit var currentDateTextView: TextView
    private lateinit var totalEarningsTextView: TextView
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var transactionsRecyclerView: RecyclerView
    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var menuAdapter: MenuAdapterBeranda

    private val transactionsList = mutableListOf<Transaction>()
    private val menuList = mutableListOf<MenuItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_beranda, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userNameTextView = view.findViewById(R.id.user_name)
        currentDateTextView = view.findViewById(R.id.current_date)
        totalEarningsTextView = view.findViewById(R.id.total_earnings)
        transactionsRecyclerView = view.findViewById(R.id.transhari)
        menuRecyclerView = view.findViewById(R.id.transmen)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        transactionsRecyclerView.layoutManager = LinearLayoutManager(context)
        menuRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        displayCurrentDate()
        fetchUserName()
        fetchTodayEarnings()
        fetchMenuItems()

        view.findViewById<TextView>(R.id.text_lisemu).setOnClickListener {
            navigateToKeuanganFragment()
        }

        view.findViewById<TextView>(R.id.text_lisemenu).setOnClickListener {
            navigateToMenuFragment()
        }
    }

    private fun displayCurrentDate() {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        currentDateTextView.text = currentDate
    }

    private fun fetchUserName() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            database.child("Users").child(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (!isAdded) return
                        val name = dataSnapshot.child("name").getValue(String::class.java)
                        userNameTextView.text = name
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle possible errors.
                    }
                })
        } else {
            userNameTextView.text = "Nama User"
        }
    }

    private fun fetchTodayEarnings() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDate = dateFormat.format(Date())

        database.child("transactions")
            .orderByChild("timestamp")
            .startAt("$todayDate 00:00:00")
            .endAt("$todayDate 23:59:59")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    var totalEarnings = 0
                    transactionsList.clear()

                    for (dataSnapshot in snapshot.children) {
                        val transaction = dataSnapshot.getValue(Transaction::class.java)
                        if (transaction != null) {
                            totalEarnings += transaction.totalPrice
                            transactionsList.add(transaction)
                        }
                    }

                    // Sort transactions by timestamp in descending order
                    transactionsList.sortByDescending { it.timestamp }

                    // Only take the latest 3 transactions
                    val latestTransactions = transactionsList.take(3)

                    // Initialize adapter with latest transactions and item click listener
                    transactionAdapter = TransactionAdapter(latestTransactions) { transaction ->
                        // Handle item click here
                    }
                    transactionsRecyclerView.adapter = transactionAdapter

                    totalEarningsTextView.text = "Rp ${Utils.formatCurrency(totalEarnings)}"
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors
                }
            })
    }

    private fun fetchMenuItems() {
        database.child("menuItems")
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    menuList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val menuItem = dataSnapshot.getValue(MenuItem::class.java)
                        if (menuItem != null) {
                            menuList.add(menuItem)
                        }
                    }

                    // Sort menus by timestamp in descending order
                    menuList.sortByDescending { it.timestamp }

                    // Initialize adapter with menu list and item click listener
                    menuAdapter = MenuAdapterBeranda(requireContext(), menuList, this@BerandaFragment)
                    menuRecyclerView.adapter = menuAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors
                }
            })
    }

    override fun onEditClick(menuItem: MenuItem) {
        // Handle edit click here
    }

    override fun onDeleteClick(menuItem: MenuItem) {
        // Handle delete click here
    }

    private fun navigateToKeuanganFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, KeuanganFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToMenuFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MenuFragment())
            .addToBackStack(null)
            .commit()
    }
}
