import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

import static liveplugin.PluginUtil.*

unwrapAction("EditorDelete")
wrapAction("EditorDelete") { AnActionEvent event, AnAction wrappedAction ->
    show("nooo... " + wrappedAction)
    wrappedAction.actionPerformed(event)
}
show("Reloaded delete wrapper")
