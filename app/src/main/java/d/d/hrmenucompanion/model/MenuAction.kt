package d.d.hrmenucompanion.model

import java.io.Serializable

open class MenuAction (var action: String?, var label: String) : Serializable{
    var isSubmenu: Boolean = false
    var actionGoesBack: Boolean = false
    var actionClosesApp: Boolean= false
    var closesAppOnFinish: Boolean = false
    var dataSendOnAction: String? = null
    var messageDisplayedOnAction: String? = null
    var actionHandlers: ArrayList<MenuAction> = ArrayList()
}