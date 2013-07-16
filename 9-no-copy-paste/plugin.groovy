import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Constraints
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.util.Pair

import static com.intellij.openapi.actionSystem.IdeActions.*

import java.nio.channels.IllegalSelectorException


import static liveplugin.PluginUtil.*

// This is a micro-plugin for IntelliJEval which disables copy/paste actions.
//
// This is an experimental plugin to try if actually typing all the code makes
// difference from psychological point of view and in the end affects software design.
//
// (Note that this code can only be executed within this plugin https://github.com/dkandalov/live-plugin)

def pluginState = new PluginState(pluginPath)
applyPluginState(pluginState.isOn)

registerAction("NoCopyPasting", "ctrl alt shift C", "ToolsMenu", "No Copy-Pasting") {
    def newState = !pluginState.isOn
    pluginState.isOn = newState
    applyPluginState(newState)
}
show("reloaded NoCopyPasting plugin")


def applyPluginState(boolean isOn) {
    def pasteActionIds = [ACTION_PASTE, ACTION_EDITOR_PASTE, "PasteMultiple", "EditorPasteSimple", "EditorPasteFromX11"]
    def copyActionIds = [
            ACTION_COPY, ACTION_EDITOR_COPY, ACTION_CUT, ACTION_EDITOR_CUT,
            "CopyPaths", ACTION_COPY_REFERENCE, "CopyElement",
            "XDebugger.CopyValue", "XDebugger.CopyName"
    ]
    def actionGroups = [GROUP_CUT_COPY_PASTE, GROUP_EDITOR_POPUP]
    (copyActionIds + pasteActionIds + ["EditorDuplicate"]).each { actionId ->
        if (isOn) {
            unwrapAction(actionId, actionGroups)
            wrapAction(actionId, actionGroups){ actionEvent, originalAction ->
                show("Hey! You promised not to copy-paste.")
            }
        } else {
            unwrapAction(actionId, actionGroups)
        }
    }

    show("No copy-pasting: ${isOn ? "ON" : "OFF"}")
}


class PluginState {
    private final pathToStateFile

    PluginState(String pluginPath) {
        pathToStateFile = pluginPath + "/state.txt"
    }

    def setIsOn(boolean isOn) {
        new File(pathToStateFile).write(isOn ? "ON" : "OFF")
    }

    def getIsOn() {
        def file = new File(pathToStateFile)
        if (!file.exists()) false
        else file.readLines().head() == "ON"
    }
}