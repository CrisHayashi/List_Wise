package com.example.list_wise.ui.lists

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.list_wise.LoginActivity
import com.example.list_wise.R
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import java.util.*
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Toolbar title
        val titleView = requireActivity().findViewById<TextView>(R.id.txtToolbarTitle)
        titleView.text = getString(R.string.title_settings)

        // Referências aos componentes
        spinnerTheme = view.findViewById(R.id.spinnerTheme)
        spinnerLanguage = view.findViewById(R.id.spinnerLanguage)
        switchNotifications = view.findViewById(R.id.switchNotifications)
        switchOffline = view.findViewById(R.id.switchOffline)
        buttonExport = view.findViewById(R.id.buttonExport)
        buttonClearData = view.findViewById(R.id.buttonClearData)
        buttonLogout = view.findViewById(R.id.buttonLogout)

        // Popular os spinners com arrays
        setupDropdownAdapters()

        // === Restaurar preferências salvas ===
        loadPreferences()

        // === Configurar listeners ===
        setupListeners(view)

        return view
    }

    private fun setupDropdownAdapters() {
        // Tema
        val themes = resources.getStringArray(R.array.theme_options)
        val themeAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, themes)
        spinnerTheme.setAdapter(themeAdapter)

        // Idioma
        val languages = resources.getStringArray(R.array.language_options)
        val langAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, languages)
        spinnerLanguage.setAdapter(langAdapter)
    }

    private fun setupListeners(view: View) {
        // Listener para tema
        spinnerTheme.setOnItemClickListener { parent, _, pos, _ ->
            val selectedTheme = parent?.getItemAtPosition(pos).toString()
            savePreference("theme", selectedTheme)
            applyTheme(selectedTheme)
            Snackbar.make(
                view,
                "Tema alterado para: $selectedTheme",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        // Listener para idioma
        spinnerLanguage.setOnItemClickListener { parent, _, pos, _ ->
            val selectedLang = parent.getItemAtPosition(pos).toString()
            savePreference("language", selectedLang)
            applyLanguage(selectedLang, recreate = true)
            Snackbar.make(view, "Idioma definido: $selectedLang", Snackbar.LENGTH_SHORT).show()
        }

        // Notificações
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            savePreference("notifications", isChecked)
            val msg = if (isChecked) "Notificações ativadas" else "Notificações desativadas"
            Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show()
        }

        // Modo Offline
        switchOffline.setOnCheckedChangeListener { _, isChecked ->
            savePreference("offline", isChecked)
            val msg = if (isChecked) "Modo offline ativado" else "Modo offline desativado"
            Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show()
        }

        // Exportar dados
        buttonExport.setOnClickListener {
            Snackbar.make(view, "Exportando dados...", Snackbar.LENGTH_LONG).show()
            // TODO: Implementar exportação real
        }

        // Limpar dados (limpa só SharedPreferences desse fragment)
        buttonClearData.setOnClickListener {
            clearPreferences()
            resetUI()
            Snackbar.make(view, "Configurações resetadas!", Snackbar.LENGTH_LONG).show()
        }

        // Logout
        buttonLogout.setOnClickListener {
            Snackbar.make(view, "Logout realizado com sucesso", Snackbar.LENGTH_LONG).show()
            // manda para tela de login e fecha o app atual
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }

    // ====== PREFERÊNCIAS ======
    private fun savePreference(key: String, value: Any) {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            when (value) {
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
            }
            apply()
        }
    }

    private fun loadPreferences() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val theme = prefs.getString("theme", "Sistema") ?: "Sistema"
        val language = prefs.getString("language", "Português") ?: "Português"
        val notifications = prefs.getBoolean("notifications", false)
        val offline = prefs.getBoolean("offline", false)

        // Aplicar tema e idioma
        applyTheme(theme)
        applyLanguage(language, recreate = false)

        // Define os valores nos campos (sem disparar listener)
        val themeOptions = resources.getStringArray(R.array.theme_options)
        if (themeOptions.contains(theme)) {
            spinnerTheme.setText(theme, false)
        }

        val langOptions = resources.getStringArray(R.array.language_options)
        if (langOptions.contains(language)) {
            spinnerLanguage.setText(language, false)
        }

        switchNotifications.isChecked = notifications
        switchOffline.isChecked = offline
    }

    private fun clearPreferences() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    private fun resetUI() {
        spinnerTheme.setText("", false)
        spinnerLanguage.setText("", false)
        switchNotifications.isChecked = false
        switchOffline.isChecked = false
    }

    // === APLICAÇÕES DE TEMA E IDIOMA ===
    private fun applyTheme(theme: String) {
        when (theme) {
            "Claro" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "Escuro" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun applyLanguage(language: String, recreate: Boolean) {
        val locale = when (language) {
            "Inglês" -> Locale("en")
            "Espanhol" -> Locale("es")
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