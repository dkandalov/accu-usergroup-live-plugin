import com.intellij.openapi.actionSystem.AnActionEvent

import static java.lang.Thread.currentThread
import static liveplugin.PluginUtil.*

registerAction("threadModel", "ctrl shift T") { AnActionEvent event ->

    show("Action thread: " + currentThread())

    doInBackground {
        show(currentEditorIn(project).selectionModel.selectedText)

        show("Background thread: " + currentThread())
        Thread.sleep(3000)
        show(execute("pwd").stdout)
    }
}

show("Loading plugin on: " + currentThread())