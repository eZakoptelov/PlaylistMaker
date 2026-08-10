package com.example.playlistmaker.main.ui.activity

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupKeyboardListener()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
    }

    private fun setupKeyboardListener() {
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val rootRect = Rect()
                binding.root.getWindowVisibleDisplayFrame(rootRect)

                // Высота экрана
                val screenHeight = binding.root.rootView.height
                // Насколько видимая область меньше экрана = высота клавиатуры + статус/нав бар
                val diff = screenHeight - rootRect.bottom

                // Если «пропало» больше 25% высоты экрана — считаем, что клавиатура открыта
                if (diff > screenHeight / 4) {
                    binding.bottomNav.visibility = View.GONE
                } else {
                    binding.bottomNav.visibility = View.VISIBLE
                }
            }
        })
    }
}
