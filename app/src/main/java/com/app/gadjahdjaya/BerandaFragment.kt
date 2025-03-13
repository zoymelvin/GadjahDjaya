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
import com.app.gadjahdjaya.model.Transaksi
import com.app.gadjahdjaya.adapter.TransaksiHariAdapter
import com.app.gadjahdjaya.ui.FragmentKeuangan
import com.app.gadjahdjaya.ui.menu.MenuFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
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
    private lateinit var transactionAdapter: TransaksiHariAdapter
    private lateinit var menuAdapter: MenuAdapterBeranda

    private val transactionsList = mutableListOf<Transaksi>()
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

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
        } else {
            userNameTextView.text = "Nama User"
        }
    }

    private fun fetchTodayEarnings() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDate = dateFormat.format(Date())

        database.child("transactions").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                var totalEarnings = 0
                transactionsList.clear()

                for (category in snapshot.children) {
                    for (dataSnapshot in category.children) {
                        val orderId = dataSnapshot.child("order_id").getValue(String::class.java) ?: "Unknown"
                        val timestamp = dataSnapshot.child("timestamp").getValue(String::class.java) ?: "Unknown"
                        val grossAmount = dataSnapshot.child("gross_amount").getValue(Int::class.java) ?: 0

                        if (timestamp.startsWith(todayDate)) {
                            totalEarnings += grossAmount
                            transactionsList.add(Transaksi(orderId, timestamp, grossAmount))
                        }
                    }
                }

                transactionsList.sortByDescending { it.timestamp }
                val latestTransactions = transactionsList.take(3)

                transactionAdapter = TransaksiHariAdapter(latestTransactions)
                transactionsRecyclerView.adapter = transactionAdapter

                val symbols = DecimalFormatSymbols(Locale("id", "ID"))
                symbols.groupingSeparator = '.'
                val formatter = DecimalFormat("#,###", symbols)
                totalEarningsTextView.text = "Rp ${formatter.format(totalEarnings)}"

            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchMenuItems() {
        database.child("menuItems").orderByChild("timestamp")
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

                    menuList.sortByDescending { it.timestamp }
                    menuAdapter = MenuAdapterBeranda(requireContext(), menuList, this@BerandaFragment)
                    menuRecyclerView.adapter = menuAdapter
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onEditClick(menuItem: MenuItem) {}

    override fun onDeleteClick(menuItem: MenuItem) {}

    private fun navigateToKeuanganFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, FragmentKeuangan())
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
