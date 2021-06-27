package org.brucknem.distractiontracker.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.brucknem.distractiontracker.R
import org.brucknem.distractiontracker.data.Entry
import org.brucknem.distractiontracker.databinding.ActivityMainBinding
import org.brucknem.distractiontracker.util.InjectorUtils
import org.brucknem.distractiontracker.util.UserProvider
import org.brucknem.distractiontracker.viewmodel.EntriesViewModel
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), RecyclerViewAdapter.OnEntryClickListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: started")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.swipeRefresh.setOnRefreshListener {
            // TODO
            Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.floatingActionButton.setOnClickListener {
            switchToDetail(-1)
        }
    }

    private fun initializeUI() {
        val user = UserProvider.checkUserLoggedIn(this) ?: return

        val viewModel =
            ViewModelProvider(this, InjectorUtils.provideFirebaseEntriesViewModelFactory(user)).get(
                EntriesViewModel::class.java
            )

        viewModel.getEntries().observe(this, { entries ->
            initRecyclerView(entries)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.reload -> {
                initializeUI()
                true
            }
            R.id.settings -> {
                val settingsIntent = Intent(this, SettingsActivity::class.java)
                startActivity(settingsIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onStart() {
        super.onStart()

        initializeUI()
    }


    private fun initRecyclerView(entries: List<Entry>) {
        Log.d(TAG, "initRecyclerView: init recycler view")

        val recyclerView: RecyclerView = binding.recyclerView
        val recyclerViewAdapter = RecyclerViewAdapter(
            context = this,
            entries = entries,
            onClickListener = this
        )
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onClick(entryId: Long) {
        Log.d(TAG, "onClick: clicked $entryId")
        switchToDetail(entryId)
    }

    private fun switchToDetail(entryId: Long) {
        val intent: Intent = Intent(this, DetailViewActivity::class.java).apply {
            putExtra("entryId", entryId)
        }
        startActivity(intent)
    }
}