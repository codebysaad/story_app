package com.saadfauzi.storyapp.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.saadfauzi.storyapp.databinding.ActivitySplashBinding
import com.saadfauzi.storyapp.utils.MySettingsPreference
import com.saadfauzi.storyapp.viewmodels.LoginViewModel
import com.saadfauzi.storyapp.viewmodels.ViewModelFactory

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }
    private lateinit var viewModel: LoginViewModel
    private val delaySplashScreen = 3000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        ObjectAnimator.ofFloat(binding.icSplash, View.TRANSLATION_Y, -30f, 30f).apply {
            duration = 3000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val pref = MySettingsPreference.getInstance(dataStore)

        viewModel = ViewModelProvider(this, ViewModelFactory(this, pref))[
                LoginViewModel::class.java
        ]

        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.getStateLogin().observe(this) {
                if (it) {
                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                    val optionsCompat: ActivityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this,
                            Pair(binding.icSplash, "icon"),
                        )
                    startActivity(intent, optionsCompat.toBundle())
                    finish()
                } else {
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    finish()
                }
            }
        }, delaySplashScreen)
    }
}