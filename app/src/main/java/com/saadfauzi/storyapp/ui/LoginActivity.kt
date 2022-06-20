package com.saadfauzi.storyapp.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityOptionsCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.saadfauzi.storyapp.R
import com.saadfauzi.storyapp.databinding.ActivityLoginBinding
import com.saadfauzi.storyapp.utils.MySettingsPreference
import com.saadfauzi.storyapp.viewmodels.LoginViewModel
import com.saadfauzi.storyapp.viewmodels.ViewModelFactory
import java.util.*

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initViewModels()
        setButtonLoginEnabled()
        initEditText()
        moveToRegister()
        playAnimations()

        binding.btnLogin.setOnClickListener {
            getLogin()
        }
    }

    override fun onBackPressed() {
        exitDialog()
    }

    private fun playAnimations(){
        ObjectAnimator.ofFloat(binding.icStoryApp, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val welcome = ObjectAnimator.ofFloat(binding.tvWelcome, View.ALPHA, 1f).setDuration(500)
        val email = ObjectAnimator.ofFloat(binding.outlinedEmail, View.ALPHA, 1f).setDuration(500)
        val password = ObjectAnimator.ofFloat(binding.outlinedPassword, View.ALPHA, 1f).setDuration(500)
        val btnLogin = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(500)
        val v = ObjectAnimator.ofFloat(binding.view, View.ALPHA, 1f).setDuration(500)
        val text = ObjectAnimator.ofFloat(binding.tvNotHavAccount, View.ALPHA, 1f).setDuration(500)

        AnimatorSet().apply {
            playSequentially(welcome, email, password, btnLogin, v, text)
            startDelay = 500
        }.start()
    }

    private fun initViewModels() {
        val pref = MySettingsPreference.getInstance(dataStore)

        viewModel = ViewModelProvider(this, ViewModelFactory(this, pref))[
                LoginViewModel::class.java
        ]

        viewModel.isLoading.observe(this) {
            showLoading(it)
        }

        viewModel.isMessage.observe(this) {
            it.getContentIfNotHandled()?.let { message ->
                showToast(message)
            }
        }
    }

    private fun initEditText() {
        binding.edtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0?.contains("@") == false) {
                    binding.edtEmail.error = resources.getString(R.string.warning_email)
                }
                setButtonLoginEnabled()
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        binding.edtPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0?.length != null) {
                    if (p0.length < 6) {
                        binding.edtPassword.error = resources.getString(R.string.warning_password)
                    }
                }
                setButtonLoginEnabled()
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }

    private fun moveToRegister() {
        val spannableString = SpannableString(resources.getString(R.string.don_t_have_an_account))
        val register: ClickableSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@LoginActivity,
                        androidx.core.util.Pair(binding.icStoryApp, "icon"),
                        androidx.core.util.Pair(binding.tvWelcome, "welcome"),
                        androidx.core.util.Pair(binding.outlinedEmail, "email"),
                    )
                startActivity(intent, optionsCompat.toBundle())
            }
        }
        Log.d("Language", Locale.getDefault().language)
        if (Locale.getDefault().language == "in"){
            spannableString.setSpan(register, 18, 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.tvNotHavAccount.apply {
                text = spannableString
                movementMethod = LinkMovementMethod.getInstance()
            }
        } else {
            spannableString.setSpan(register, 23, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.tvNotHavAccount.apply {
                text = spannableString
                movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }

    private fun setButtonLoginEnabled() {
        val emailRes = binding.edtEmail.text
        val passRes = binding.edtPassword.text
        binding.btnLogin.isEnabled =
            emailRes != null && passRes != null && emailRes.contains("@") && passRes.length >= 6
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
    }

    private fun showToast(message: String) {
        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun getLogin() {
        val email = binding.edtEmail.text.toString()
        val password = binding.edtPassword.text.toString()

        viewModel.postLogin(email, password)
        viewModel.loginResult.observe(this) { result ->
            if (result != null) {
                if (result.error) {
                    Log.d("LoginActivity", result.message)
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                } else {
                    val token = result.loginResult?.token.toString()
                    viewModel.saveAccessToken(true, token)
                    Log.d("LoginActivity", token)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            } else {
                Toast.makeText(this, resources.getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exitDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.app_name))
        builder.setMessage(resources.getString(R.string.dialog_exit_app))
        builder.setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
            finish()
        }
        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, _ -> dialogInterface.cancel() }
        builder.show()
    }
}