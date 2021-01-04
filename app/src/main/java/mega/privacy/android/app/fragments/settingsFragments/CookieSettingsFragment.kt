package mega.privacy.android.app.fragments.settingsFragments

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import dagger.hilt.android.AndroidEntryPoint
import mega.privacy.android.app.R
import mega.privacy.android.app.constants.SettingsConstants.*
import mega.privacy.android.app.fragments.settingsFragments.CookieSettingsViewModel.Cookie
import mega.privacy.android.app.fragments.settingsFragments.CookieSettingsViewModel.Cookie.*

@AndroidEntryPoint
class CookieSettingsFragment : SettingsBaseFragment() {

    private val viewModel by viewModels<CookieSettingsViewModel>()

    private lateinit var acceptCookiesPreference: SwitchPreferenceCompat
    private lateinit var essentialCookiesPreference: SwitchPreferenceCompat
    private lateinit var preferenceCookiesPreference: SwitchPreferenceCompat
    private lateinit var analyticsCookiesPreference: SwitchPreferenceCompat
    private lateinit var advertisingCookiesPreference: SwitchPreferenceCompat
    private lateinit var thirdPartyCookiesPreference: SwitchPreferenceCompat

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_cookie)

        acceptCookiesPreference = findPreference(KEY_COOKIE_ACCEPT)!!
        essentialCookiesPreference = findPreference(KEY_COOKIE_ESSENTIAL)!!
        preferenceCookiesPreference = findPreference(KEY_COOKIE_PREFERENCE)!!
        analyticsCookiesPreference = findPreference(KEY_COOKIE_ANALYTICS)!!
        advertisingCookiesPreference = findPreference(KEY_COOKIE_ADVERTISING)!!
        thirdPartyCookiesPreference = findPreference(KEY_COOKIE_THIRD_PARTY)!!
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getEnabledCookies().observe(viewLifecycleOwner, ::showConfiguration)

        acceptCookiesPreference.onPreferenceChangeListener = this
        preferenceCookiesPreference.onPreferenceChangeListener = this
        analyticsCookiesPreference.onPreferenceChangeListener = this
        advertisingCookiesPreference.onPreferenceChangeListener = this
        thirdPartyCookiesPreference.onPreferenceChangeListener = this
    }

    private fun showConfiguration(settings: Set<Cookie>?) {
        essentialCookiesPreference.isChecked = settings?.contains(ESSENTIAL) == true
        preferenceCookiesPreference.isChecked = settings?.contains(PREFERENCE) == true
        analyticsCookiesPreference.isChecked = settings?.contains(ANALYTICS) == true
        advertisingCookiesPreference.isChecked = settings?.contains(ADVERTISEMENT) == true
        thirdPartyCookiesPreference.isChecked = settings?.contains(THIRDPARTY) == true

        acceptCookiesPreference.isChecked = preferenceCookiesPreference.isChecked ||
                analyticsCookiesPreference.isChecked ||
                advertisingCookiesPreference.isChecked ||
                thirdPartyCookiesPreference.isChecked
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        val enable = newValue as? Boolean ?: false

        when (preference?.key) {
            acceptCookiesPreference.key -> viewModel.toggleCookies(enable)
            preferenceCookiesPreference.key -> viewModel.changeCookie(PREFERENCE, enable)
            analyticsCookiesPreference.key -> viewModel.changeCookie(ANALYTICS, enable)
            advertisingCookiesPreference.key -> viewModel.changeCookie(ADVERTISEMENT, enable)
            thirdPartyCookiesPreference.key -> viewModel.changeCookie(THIRDPARTY, enable)
        }

        return false
    }
}
