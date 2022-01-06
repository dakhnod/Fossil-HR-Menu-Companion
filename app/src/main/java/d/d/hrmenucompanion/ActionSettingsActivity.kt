package d.d.hrmenucompanion

import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import d.d.hrmenucompanion.model.MenuAction

class ActionSettingsActivity : AppCompatActivity() {
    companion object {
        val RESULT_CODE_OK = 0
        val RESULT_CODE_CANCELED = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        val settingsFragment = SettingsFragment(this)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, settingsFragment)
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val layout = findViewById<LinearLayout>(R.id.layoutSave)
        setResult(RESULT_CODE_CANCELED)
        layout.getChildAt(0).setOnClickListener {
            finish()
        }
        layout.getChildAt(1).setOnClickListener {
            try {
                saveAndExit(settingsFragment)
            } catch (exception: java.lang.IllegalArgumentException) {
                Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initIntent(settingsFragment: SettingsFragment) {
        val intent = intent
        if (!intent.hasExtra("EXTRA_ACTION")) {
            return
        }
        val action = intent.getSerializableExtra("EXTRA_ACTION") as MenuAction
        val isRoot = intent.getBooleanExtra("EXTRA_ACTION_IS_ROOT", false)
        settingsFragment.findPreference<EditTextPreference>("label")!!.text = action.label
        if (isRoot) {
            settingsFragment.findPreference<ListPreference>("action")!!.isEnabled = false
        } else {
            settingsFragment.findPreference<ListPreference>("action")!!.value = action.action
        }
        if(isRoot){
            settingsFragment.findPreference<SwitchPreference>("action_goes_back")!!.isEnabled = false
            settingsFragment.findPreference<SwitchPreference>("action_closes_app")!!.isEnabled = false
            settingsFragment.findPreference<SwitchPreference>("action_goes_back")!!.isChecked =
                false
            settingsFragment.findPreference<SwitchPreference>("action_closes_app")!!.isChecked =
                false
        }else{
            settingsFragment.findPreference<SwitchPreference>("action_goes_back")!!.isChecked =
                action.actionGoesBack
            settingsFragment.findPreference<SwitchPreference>("action_closes_app")!!.isChecked =
                action.actionClosesApp
        }
        settingsFragment.findPreference<SwitchPreference>("is_submenu")!!.isChecked =
            action.isSubmenu
        settingsFragment.findPreference<SwitchPreference>("action_closes_app_on_finish")!!.isChecked =
            action.closesAppOnFinish
        settingsFragment.findPreference<EditTextPreference>("data_sent_on_action")!!.text =
            action.dataSendOnAction ?: ""
        settingsFragment.findPreference<EditTextPreference>("message_displayed_on_action")!!.text =
            action.messageDisplayedOnAction ?: ""
    }

    @Throws(IllegalArgumentException::class)
    private fun saveAndExit(settingsFragment: SettingsFragment) {
        val label = settingsFragment.findPreference<EditTextPreference>("label")!!.text
        // if (label.isNullOrEmpty()) {
        //    throw IllegalArgumentException("label must not be empty")
        //}
        val action = settingsFragment.findPreference<ListPreference>("action")!!.value
        val isRoot = intent.getBooleanExtra("EXTRA_ACTION_IS_ROOT", false)
        if (!isRoot && action.isNullOrEmpty()) {
            throw IllegalArgumentException("an action must be selected")
        }
        val dataToSend =
            settingsFragment.findPreference<EditTextPreference>("data_sent_on_action")!!.text
        val messageToDisplay =
            settingsFragment.findPreference<EditTextPreference>("message_displayed_on_action")!!.text

        val resultAction = MenuAction(action, label)
        resultAction.isSubmenu =
            settingsFragment.findPreference<SwitchPreference>("is_submenu")!!.isChecked
        resultAction.actionGoesBack =
            settingsFragment.findPreference<SwitchPreference>("action_goes_back")!!.isChecked
        resultAction.actionClosesApp =
            settingsFragment.findPreference<SwitchPreference>("action_closes_app")!!.isChecked
        resultAction.closesAppOnFinish =
            settingsFragment.findPreference<SwitchPreference>("action_closes_app_on_finish")!!.isChecked
        if (!dataToSend.isNullOrEmpty()) {
            resultAction.dataSendOnAction = dataToSend
        }
        if (!messageToDisplay.isNullOrEmpty()) {
            resultAction.messageDisplayedOnAction = messageToDisplay
        }

        intent.putExtra("EXTRA_ACTION", resultAction)
        setResult(RESULT_CODE_OK, intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // NavUtils.navigateUpFromSameTask(this)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment(private val settingsActivity: ActionSettingsActivity) :
        PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            settingsActivity.initIntent(this)
        }
    }
}