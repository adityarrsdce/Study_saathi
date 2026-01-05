package com.babu.appp.screen

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.babu.appp.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        setPreferencesFromResource(
            R.xml.settings_preferences,
            rootKey
        )
    }
}
