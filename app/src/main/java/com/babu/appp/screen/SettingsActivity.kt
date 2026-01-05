package com.babu.appp.screen


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.babu.appp.screen.SettingsFragment

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, SettingsFragment())
            .commit()
    }
}
