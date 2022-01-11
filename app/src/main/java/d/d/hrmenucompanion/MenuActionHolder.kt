package d.d.hrmenucompanion

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.unnamed.b.atv.model.TreeNode
import d.d.hrmenucompanion.model.MenuAction

class MenuActionHolder(context: Context?) : TreeNode.BaseNodeViewHolder<MenuAction>(context) {
    override fun createNodeView(node: TreeNode?, menuAction: MenuAction?): View {
        val actionView = LayoutInflater.from(context).inflate(R.layout.menu_action, null, false)
        if (menuAction == null) {
            return actionView
        }
        if (node == null) {
            return actionView
        }
        actionView.findViewById<TextView>(R.id.actionTextView).text =
            menuAction.action ?: "Watch face"
        actionView.findViewById<TextView>(R.id.labelTextView).text = menuAction.label

        if (menuAction.actionGoesBack) {
            actionView.findViewById<ImageView>(R.id.iamge_action_goes_back).visibility =
                View.VISIBLE
        }
        if (menuAction.actionClosesApp) {
            actionView.findViewById<ImageView>(R.id.image_action_closes).visibility =
                View.VISIBLE
        }
        if (menuAction.actionClosesAppOnFinish) {
            actionView.findViewById<ImageView>(R.id.image_action_closes).visibility =
                View.VISIBLE
        }
        if (menuAction.isSubmenu) {
            actionView.findViewById<ImageView>(R.id.image_action_is_submenu).visibility =
                View.VISIBLE
        }
        if (!menuAction.dataSendOnAction.isNullOrBlank()) {
            actionView.findViewById<ImageView>(R.id.image_action_has_data).visibility =
                View.VISIBLE
        }

        actionView.setPadding(node.level * 30, 0, 0, 0)

        return actionView
    }
}