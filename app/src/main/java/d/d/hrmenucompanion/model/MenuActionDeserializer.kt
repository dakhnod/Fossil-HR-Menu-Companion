package d.d.hrmenucompanion.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class MenuActionDeserializer : JsonDeserializer<MenuAction> {
    override fun deserialize(
        jsonElement: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): MenuAction {
        val menuAction: MenuAction = MenuAction(null, "")
        if(jsonElement == null){
            return menuAction
        }
        if(context == null){
            return menuAction
        }
        val json = jsonElement.asJsonObject
        menuAction.label = json.get("label").asString
        menuAction.action = json.get("action")?.asString
        menuAction.isSubmenu = json.get("is_submenu")?.asBoolean ?: false
        menuAction.actionGoesBack = json.get("action_goes_back")?.asBoolean ?: false
        menuAction.actionClosesApp = json.get("action_closes_app")?.asBoolean ?: false
        menuAction.actionClosesAppOnFinish = json.get("action_closes_app_on_finish")?.asBoolean ?: false
        menuAction.dataSendOnAction = json.get("data_sent_on_action")?.asString
        menuAction.messageDisplayedOnAction = json.get("message_displayed_on_action")?.asString
        menuAction.appToOpen = json.get("app_to_open")?.asString

        val handlers = json.get("action_handlers")?.asJsonArray ?: listOf()
        handlers.forEach{
            menuAction.actionHandlers.add(context.deserialize(it, MenuAction::class.java))
        }

        return menuAction
    }
}