package com.saadfauzi.storyapp.ui

import android.content.Context
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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.saadfauzi.storyapp.R
import com.saadfauzi.storyapp.databinding.ActivityRegisterBinding
import com.saadfauzi.storyapp.utils.MySettingsPreference
import com.saadfauzi.storyapp.viewmodels.RegisterViewModel
import com.saadfauzi.storyapp.viewmodels.ViewModelFactory
import java.util.*

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class RegisterActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }

    private lateinit var viewModels: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setButtonRegisterEnabled()
        initEditText()
        backToLogin()

        val pref = MySettingsPreference.getInstance(dataStore)

        viewModels = ViewModelProvider(this, ViewModelFactory(this, pref))[
                RegisterViewModel::class.java
        ]

        viewModels.isLoading.observe(this){
            showLoading(it)
        }

        viewModels.isMessage.observe(this){
            Toast.makeText(this@RegisterActivity, it.toString(), Toast.LENGTH_SHORT).show()
        }

        binding.btnRegister.setOnClickListener {
            val username = binding.edtUsername.text.toString()
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()
            binding.btnRegister.isEnabled = false
            viewModels.registerNewUser(username, email, password)
            viewModels.registerRes.observe(this){ result ->
                binding.btnRegister.isEnabled = true
                if (result != null){
                    finish()
                    Log.d(TAG, result.message.toString())
                }
            }
        }
    }

    private fun backToLogin(){
        val spannableString = SpannableString(resources.getString(R.string.already_have_an_account))
        val register: ClickableSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {
                finish()
            }
        }
        if (Locale.getDefault().language == "in"){
            spannableString.setSpan(register, 18, 32, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.tvAlreadyHaveAccount.apply {
                text = spannableString
                movementMethod = LinkMovementMethod.getInstance()
            }
        } else {
            spannableString.setSpan(register, 32, 37, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.tvAlreadyHaveAccount.apply {
                text = spannableString
                movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }

    private fun initEditText(){
        binding.apply {
            edtUsername.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0 != null){
                        setButtonRegisterEnabled()
                    }
                }

                override fun afterTextChanged(p0: Editable?) {

                }
            })
            edtEmail.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0?.contains("@") == false) {
                        binding.edtEmail.error = resources.getString(R.string.warning_email)
                    }
                    setButtonRegisterEnabled()
                }

                override fun afterTextChanged(p0: Editable?) {

                }
            })
            edtPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0 != null){
                        setButtonRegisterEnabled()
                    }
                }

                override fun afterTextChanged(p0: Editable?) {

                }
            })
        }
    }

    private fun setButtonRegisterEnabled() {
        val username = binding.edtUsername.text
        val emailRes = binding.edtEmail.text
        val passRes = binding.edtPassword.text

        binding.btnRegister.isEnabled =
            username != null && emailRes != null && passRes != null && emailRes.contains("@") && passRes.length >= 6
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbRegister.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.pbRegister.isEnabled = !isLoading
    }

    companion object {
        private const val TAG = "RegisterActivity"
    }
}