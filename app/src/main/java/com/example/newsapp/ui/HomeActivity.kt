package com.example.newsapp.ui

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.newsapp.R
import com.example.newsapp.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private lateinit var binding : ActivityHomeBinding
    private lateinit var bottomNavBar : BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavBar = binding.bottomNavigation

        val toolbar: Toolbar = binding.toolbar
        toolbar.setTitle(R.string.app_name)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        setSupportActionBar(toolbar)
        supportActionBar?.setIcon(R.drawable.ic_toolbar_icon)

        val res = checkInternetConnectivity()
        var navController = Navigation.findNavController(
            this,
            R.id.navHostFragmentContainer
        )
        bottomNavBar.setupWithNavController(navController)
        if(!res){
            Toast.makeText(this,"No internet connection",Toast.LENGTH_SHORT).show()
            val navHostFragment = (supportFragmentManager.findFragmentById(R.id.navHostFragmentContainer) as NavHostFragment)
            val inflater = navHostFragment.navController.navInflater
            val graph = inflater.inflate(R.navigation.news_app_home_nav_controller)
            graph.setStartDestination(R.id.savedNewsFragmentNav)
            navHostFragment.navController.graph = graph
        }

    }

    private fun checkInternetConnectivity() : Boolean{
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }
}