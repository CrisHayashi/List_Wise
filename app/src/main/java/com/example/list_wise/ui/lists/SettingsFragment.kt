package com.example.list_wise.ui.lists

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Filter
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.example.list_wise.LoginActivity
import com.example.list_wise.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import java.util.Locale

class SettingsFragment : Fragment() {

    private lateinit var spinnerTheme: MaterialAutoCompleteTextView
    private lateinit var spinnerLanguage: MaterialAutoCompleteTextView
    private lateinit var switchNotifications: SwitchMaterial
    private lateinit var switchOffline: SwitchMaterial
    private lateinit var buttonExport: Button
    private lateinit var buttonClearData: Button
    private lateinit var buttonLogout: Button

    private val PREFS_NAME = "settings_prefs"

    // ORDEM precisa ser igual à dos arrays (theme_options e language_options)
    private val themeKeys = arrayOf("system", "light", "dark")
    private val languageCodes = arrayOf("pt", "en", "es")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Toolbar title
        val titleView = requireActivity().findViewById<TextView>(R.id.txtToolbarTitle)
        titleView.text = getString(R.string.title_settings)

        // Referências
        spinnerTheme = view.findViewById(R.id.spinnerTheme)
        spinnerLanguage = view.findViewById(R.id.spinnerLanguage)
        switchNotifications = view.findViewById(R.id.switchNotifications)
        switchOffline = view.findViewById(R.id.switchOffline)
        buttonExport = view.findViewById(R.id.buttonExport)
        buttonClearData = view.findViewById(R.id.buttonClearData)
        buttonLogout = view.findViewById(R.id.buttonLogout)

        setupDropdownAdapters()
        loadPreferences()
        setupListeners(view)

        return view
    }

    private fun setupDropdownAdapters() {
        // ===== TEMA (adapter SEM filtro) =====
        val themes = resources.getStringArray(R.array.theme_options)

        val themeAdapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            themes
        ) {
            override fun getFilter(): Filter {
                return object : Filter() {
                    override fun performFiltering(constraint: CharSequence?): FilterResults {
                        return FilterResults().apply {
                            values = themes
                            count = themes.size
                        }
                    }

                    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                        notifyDataSetChanged()
                    }
                }
            }
        }

        spinnerTheme.setAdapter(themeAdapter)
        spinnerTheme.keyListener = null
        spinnerTheme.setOnClickListener {
            spinnerTheme.showDropDown()
        }

        // ===== IDIOMA (adapter SEM filtro) =====
        val languages = resources.getStringArray(R.array.language_options)

        val langAdapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            languages
        ) {
            override fun getFilter(): Filter {
                return object : Filter() {
                    override fun performFiltering(constraint: CharSequence?): FilterResults {
                        return FilterResults().apply {
                            values = languages
                            count = languages.size
                        }
                    }

                    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                        notifyDataSetChanged()
                    }
                }
            }
        }

        spinnerLanguage.setAdapter(langAdapter)
        spinnerLanguage.keyListener = null
        spinnerLanguage.setOnClickListener {
            spinnerLanguage.showDropDown()
        }
    }

    private fun setupListeners(view: View) {
        // Tema
        spinnerTheme.setOnItemClickListener { parent, _, pos, _ ->
            val labels = resources.getStringArray(R.array.theme_options)
            val selectedLabel = labels[pos]
            val themeKey = themeKeys.getOrNull(pos) ?: "system"

            savePreference("theme", themeKey)
            applyTheme(themeKey)

            Snackbar.make(
                view,
                getString(R.string.snackbar_theme_changed, selectedLabel),
                Snackbar.LENGTH_SHORT
            ).show()
        }

        // Idioma (agora TODOS funcionam)
        spinnerLanguage.setOnItemClickListener { _, _, pos, _ ->
            val labels = resources.getStringArray(R.array.language_options)
            val selectedLabel = labels[pos]
            val langCode = languageCodes.getOrNull(pos) ?: "pt"

            savePreference("language", langCode)
            applyLanguage(langCode, recreate = true)

            Snackbar.make(
                view,
                getString(R.string.snackbar_language_changed, selectedLabel),
                Snackbar.LENGTH_SHORT
            ).show()
        }

        // Notificações
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            savePreference("notifications", isChecked)
            val msgRes = if (isChecked) {
                R.string.snackbar_notifications_on
            } else {
                R.string.snackbar_notifications_off
            }
            Snackbar.make(view, getString(msgRes), Snackbar.LENGTH_SHORT).show()
        }


        // Modo Offline
        switchOffline.setOnCheckedChangeListener { _, isChecked ->
            savePreference("offline", isChecked)
            val msgRes = if (isChecked) {
                R.string.snackbar_offline_on
            } else {
                R.string.snackbar_offline_off
            }
            Snackbar.make(view, getString(msgRes), Snackbar.LENGTH_SHORT).show()
        }

        // Exportar dados
        buttonExport.setOnClickListener {
            Snackbar.make(
                view,
                getString(R.string.snackbar_exporting_data),
                Snackbar.LENGTH_LONG
            ).show()
        }

        // Limpar dados (apenas prefs de settings)
        buttonClearData.setOnClickListener {
            clearPreferences()
            resetUI()
            Snackbar.make(
                view,
                getString(R.string.snackbar_settings_reset),
                Snackbar.LENGTH_LONG
            ).show()
        }


        // Logout + Google sign out + limpar nome
        buttonLogout.setOnClickListener {
            val userPrefs =
                requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            userPrefs.edit {
                clear()
            }

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            val googleClient = GoogleSignIn.getClient(requireContext(), gso)

            googleClient.signOut().addOnCompleteListener {
                Snackbar.make(
                    view,
                    getString(R.string.snackbar_logout_success),
                    Snackbar.LENGTH_LONG
                ).show()

                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }
    }

    // ===== PREFS =====
    private fun savePreference(key: String, value: Any) {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            when (value) {
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
            }
            // edit{} do androidx.core já dá apply() automaticamente
        }
    }

    private fun loadPreferences() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val themeKey = prefs.getString("theme", "system") ?: "system"
        val langCode = prefs.getString("language", "pt") ?: "pt"
        val notifications = prefs.getBoolean("notifications", false)
        val offline = prefs.getBoolean("offline", false)

        // Aplica tema e idioma na inicialização
        applyTheme(themeKey)
        applyLanguage(langCode, recreate = false)

        // Ajusta texto do spinner de tema
        val themeLabels = resources.getStringArray(R.array.theme_options)
        val themeIndex = themeKeys.indexOf(themeKey).takeIf { it >= 0 } ?: 0
        if (themeIndex in themeLabels.indices) {
            spinnerTheme.setText(themeLabels[themeIndex], false)
        }

        // Ajusta texto do spinner de idioma
        val languageLabels = resources.getStringArray(R.array.language_options)
        val langIndex = languageCodes.indexOf(langCode).takeIf { it >= 0 } ?: 0
        if (langIndex in languageLabels.indices) {
            spinnerLanguage.setText(languageLabels[langIndex], false)
        }

        switchNotifications.isChecked = notifications
        switchOffline.isChecked = offline
    }

    private fun clearPreferences() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            clear()
        }
    }

    private fun resetUI() {
        spinnerTheme.setText("", false)
        spinnerLanguage.setText("", false)
        switchNotifications.isChecked = false
        switchOffline.isChecked = false
    }

    // ===== TEMA & IDIOMA =====
    private fun applyTheme(themeKey: String) {
        when (themeKey) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    @SuppressLint("AppBundleLocaleChanges")
    private fun applyLanguage(languageCode: String, recreate: Boolean) {
        val locale = when (languageCode) {
            "en" -> Locale("en")
            "es" -> Locale("es")
            else -> Locale("pt")
        }

        Locale.setDefault(locale)

        val resources = requireContext().resources
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        if (recreate) {
            requireActivity().recreate()
        }
    }
}
