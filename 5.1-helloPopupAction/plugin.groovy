import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.popup.JBPopupFactory

import static liveplugin.PluginUtil.*


registerAction("helloPopupAction", "ctrl alt shift P") { AnActionEvent event ->
	def popupMenuDescription = [
			"World 1": [
					"sub-world 11": { show "Hello sub-world 11!!" },
					"sub-world 12": { show "hello sub-world 12" },
			],
			"World 2": [
					"sub-world 21": { show "sub-world 21 hello" },
					"sub-world 22": { show "sub-world hello 22" },
			],
			"World 3": { show "Hey world 3!" }
	]
	def popupTitle = "Say hello to..."
    showPopupMenu(popupMenuDescription, popupTitle)
}
show("Loaded 'helloPopupAction'<br/>Use ctrl+alt+shift+P to run it")
