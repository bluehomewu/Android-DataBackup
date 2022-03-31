package com.xayah.databackup

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.xayah.databackup.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        binding.viewModel = viewModel
        setContentView(binding.root)

        initialize()
    }

    private fun initialize() {
        setSupportActionBar(binding.toolbar)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration.Builder(
            R.id.homeFragment,
            R.id.backupFragment,
            R.id.restoreFragment,
            R.id.settingsFragment,
        ).build()
        setupActionBarWithNavController(navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.backupFragment, R.id.restoreFragment, R.id.settingsFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
                else -> {
                    binding.bottomNavigation.visibility = View.GONE
                }
            }
        }
        binding.bottomNavigation.setupWithNavController(navController)
        viewModel.versionName =
            this.packageManager.getPackageInfo(this.packageName, 0).versionName
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}