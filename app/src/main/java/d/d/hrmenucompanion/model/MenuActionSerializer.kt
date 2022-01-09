package d.d.hrmenucompanion.model

import com.google.gson.*
import java.lang.reflect.Type
import java.security.KeyStore

class MenuActionSerializer : JsonSerializer<MenuAction> {
    override fun serialize(
        src: MenuAction?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val node = JsonObject()

        if (src == null) {
            return node
        }
        if (context == null) {
            return node
        }

        node.addProperty("label", src.label)
        if(!src.action.isNullOrBlank()){
            node.addProperty("action", src.action)
        }
        if (src.isSubmenu) {
            node.addProperty("is_submenu", src.isSubmenu)
        }
        if (src.closesAppOnFinish) {
            node.addProperty("close_app_on_finish", src.closesAppOnFinish)
        }
        if (src.actionClosesApp) {
            node.addProperty("action_closes_app", src.actionClosesApp)
        }
        if (!src.dataSendOnAction.isNullOrBlank()) {
            node.addProperty("data_sent_on_action", src.dataSendOnAction)
        }
        if (!src.messageDisplayedOnAction.isNullOrBlank()) {
            node.addProperty("message_displayed_on_action", src.messageDisplayedOnAction)
        }
        if(!src.appToOpen.isNullOrBlank()){
            node.addProperty("app_to_open", src.appToOpen)
        }
        if (src.actionHandlers.size > 0) {
            val handlers = JsonArray(src.actionHandlers.size)
            for (action in src.actionHandlers) {
                handlers.add(context.serialize(action))
            }

            node.add("action_handlers", handlers)
        }

        return node
    }

}