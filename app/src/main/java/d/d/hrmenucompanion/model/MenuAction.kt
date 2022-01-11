package d.d.hrmenucompanion.model

import java.io.Serializable

open class MenuAction (var action: String?, var label: String?) : Serializable{
    var isSubmenu: Boolean = false
    var appToOpen: String? = null
    var actionGoesBack: Boolean = false
    var actionClosesApp: Boolean= false
    var actionClosesAppOnFinish: Boolean = false
    var dataSendOnAction: String? = null
    var messageDisplayedOnAction: String? = null
    var actionHandlers: ArrayList<MenuAction> = ArrayList()
}