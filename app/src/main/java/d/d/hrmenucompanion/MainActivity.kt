package d.d.hrmenucompanion

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.gson.GsonBuilder
import com.unnamed.b.atv.model.TreeNode
import com.unnamed.b.atv.view.AndroidTreeView
import d.d.hrmenucompanion.databinding.ActivityMainBinding
import d.d.hrmenucompanion.model.MenuAction
import d.d.hrmenucompanion.model.MenuActionDeserializer
import d.d.hrmenucompanion.model.MenuActionSerializer
import java.io.File
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val treeView = AndroidTreeView(this)
    private lateinit var menuActionRoot: MenuAction
    private lateinit var sharedPrefs: SharedPreferences

    object PrefConstants {
        const val PREFS_KEY_MENU_STRUCTURE = "MENU_STRUCTURE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        /*
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
         */

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "sending to Gadgetbridge...", Snackbar.LENGTH_LONG).show()

            val intent = Intent("nodomain.freeyourgadget.gadgetbridge.Q_PUSH_CONFIG")

            val fullJson = """
                {
                    "push": {
                        "set": {
                            "customWatchFace._.config.menu_structure": %s
                        }
                    }
                }
            """.trimIndent().format(actionToJsonString(menuActionRoot))

            intent.putExtra("EXTRA_CONFIG_JSON", fullJson)
            sendBroadcast(intent)
        }


        sharedPrefs = getPreferences(MODE_PRIVATE)
        menuActionRoot = loadMenuFromStorage()

        initViews()
    }

    private var editActionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == ActionSettingsActivity.RESULT_CODE_OK){
            val resultIntent = it.data ?: return@registerForActivityResult

            val editedMenuAction = resultIntent.getSerializableExtra("EXTRA_ACTION") as MenuAction
            val originalHash = resultIntent.getIntExtra("EXTRA_ACTION_ORIGINAL_HASH", 0)

            class FoundMessage : Throwable()

            fun searchWithChildren(action: MenuAction){
                if(action.hashCode() == originalHash){
                    action.label = editedMenuAction.label
                    action.action = editedMenuAction.action
                    action.messageDisplayedOnAction = editedMenuAction.messageDisplayedOnAction
                    action.dataSendOnAction = editedMenuAction.dataSendOnAction
                    action.actionClosesAppOnFinish = editedMenuAction.actionClosesAppOnFinish
                    action.actionClosesApp = editedMenuAction.actionClosesApp
                    action.actionGoesBack = editedMenuAction.actionGoesBack
                    action.isSubmenu = editedMenuAction.isSubmenu
                    action.appToOpen = editedMenuAction.appToOpen
                    throw FoundMessage()
                }
                action.actionHandlers.forEach { menuAction ->
                    searchWithChildren(menuAction)
                }
            }
            try{
                searchWithChildren(menuActionRoot)
                return@registerForActivityResult
            }catch (foundMessage: FoundMessage){

            }
            refreshTree()
            persistMenu(menuActionRoot)
        }
    }

    private var createActionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == ActionSettingsActivity.RESULT_CODE_OK){
            val resultIntent = it.data ?: return@registerForActivityResult

            val editedMenuAction = resultIntent.getSerializableExtra("EXTRA_ACTION") as MenuAction
            val originalHash = resultIntent.getIntExtra("EXTRA_ACTION_PARENT_HASH", 0)

            class FoundMessage : Throwable()

            fun searchWithChildren(action: MenuAction){
                if(action.hashCode() == originalHash){
                    action.actionHandlers.add(editedMenuAction)
                    throw FoundMessage()
                }
                action.actionHandlers.forEach { menuAction ->
                    searchWithChildren(menuAction)
                }
            }
            try{
                searchWithChildren(menuActionRoot)
                return@registerForActivityResult
            }catch (foundMessage: FoundMessage){

            }
            refreshTree()
            persistMenu(menuActionRoot)
        }
    }

    private fun handleTreeClick(treeNode: TreeNode, menuAction: Any): Boolean{
        if(menuAction !is MenuAction){
            return false
        }
        val options = ArrayList<String>(3)
        options.add("add child")
        options.add("edit")
        options.add("delete")
        val dialog = AlertDialog.Builder(this)
            .setItems(options.toArray(arrayOf()), DialogInterface.OnClickListener { dialogInterface, i ->
                if(dialogInterface !is DialogInterface){
                    return@OnClickListener
                }
                when (i) {
                    0 -> {
                        if(!menuAction.isSubmenu){
                            Toast.makeText(this, "cannot create children for non sub-menu", Toast.LENGTH_LONG).show()
                            return@OnClickListener
                        }
                        val createIntent = Intent(this, ActionSettingsActivity::class.java)
                        createIntent.putExtra("EXTRA_ACTION_PARENT_HASH", menuAction.hashCode())
                        createActionLauncher.launch(createIntent)
                    }
                    1 -> {
                        val editIntent = Intent(this, ActionSettingsActivity::class.java)
                        editIntent.putExtra("EXTRA_ACTION", menuAction)
                        editIntent.putExtra("EXTRA_ACTION_ORIGINAL_HASH", menuAction.hashCode())
                        editIntent.putExtra("EXTRA_ACTION_IS_ROOT", treeNode.level == 1)
                        editActionLauncher.launch(editIntent)
                    }
                    2 -> {
                        if(treeNode.level == 1){
                            Toast.makeText(this, "cannot delete root node.", Toast.LENGTH_LONG).show()
                            return@OnClickListener
                        }
                        val parent = treeNode.parent.value
                        if(parent !is MenuAction){
                            return@OnClickListener
                        }
                        parent.actionHandlers.remove(menuAction)
                        persistMenu(menuActionRoot)
                        refreshTree()
                    }
                }
            })
        dialog.show()
        return true
    }

    private fun initViews(){
        val mainLayout = findViewById<LinearLayout>(R.id.mainLayout)

        treeView.setDefaultViewHolder(MenuActionHolder::class.java)
        treeView.setDefaultAnimation(true)
        treeView.setUseAutoToggle(false)
        treeView.setDefaultNodeLongClickListener(this::handleTreeClick)
        treeView.setDefaultNodeClickListener(this::handleTreeClick)

        val rootNode = TreeNode.root()
        buildMenuTree(menuActionRoot, rootNode)
        treeView.setRoot(rootNode)
        mainLayout.addView(treeView.view)
        treeView.expandAll()
    }

    private fun refreshTree(){
        val rootNode = TreeNode.root()
        buildMenuTree(menuActionRoot, rootNode)

        treeView.setRoot(rootNode)
        val mainLayout = findViewById<LinearLayout>(R.id.mainLayout)
        mainLayout.removeAllViews()
        mainLayout.addView(treeView.view)
        treeView.expandAll()
    }

    private fun actionToJsonString(rootAction: MenuAction, prettyPrint: Boolean = false): String? {
        val serializer = GsonBuilder()
            .registerTypeAdapter(MenuAction::class.java, MenuActionSerializer())

        if(prettyPrint){
            serializer.setPrettyPrinting()
        }

        return serializer.create().toJson(rootAction)
    }

    private fun persistMenu(rootAction: MenuAction){
        sharedPrefs.edit().putString(PrefConstants.PREFS_KEY_MENU_STRUCTURE, actionToJsonString(rootAction)).apply()
    }

    private fun loadMenuFromStorage(): MenuAction{
        val deserializer = GsonBuilder()
            .registerTypeAdapter(MenuAction::class.java, MenuActionDeserializer())
            .create()

        val structureJson = sharedPrefs.getString(PrefConstants.PREFS_KEY_MENU_STRUCTURE, null)
        if(structureJson.isNullOrBlank()){
            val inputStream = assets.open("default_menu_structure.json.txt")

            val structure = deserializer.fromJson(InputStreamReader(inputStream), MenuAction::class.java)
            inputStream.close()
            return structure
        }

        return deserializer.fromJson(structureJson, MenuAction::class.java)
    }

    private fun buildMenuTree(menuAction: MenuAction, treeNode: TreeNode) {
        val parent = TreeNode(menuAction)

        for (action in menuAction.actionHandlers) {
            buildMenuTree(action, parent)
        }
        treeNode.addChild(parent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun exportStructure(rootAction: MenuAction){
        val externalDir = getExternalFilesDir("export")
        val exportFile = File(externalDir, "%d.json.txt".format(System.currentTimeMillis()))
        val json = actionToJsonString(rootAction, true)
        if(json == null){
            Toast.makeText(this, "menu serialization failed", Toast.LENGTH_LONG).show()
            return
        }
        exportFile.writeText(json)
        Toast.makeText(this, "written to %s".format(exportFile), Toast.LENGTH_LONG).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_export -> {
                exportStructure(menuActionRoot)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /*
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
     */
}