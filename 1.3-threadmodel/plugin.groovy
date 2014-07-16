import com.intellij.openapi.actionSystem.AnActionEvent

import static liveplugin.PluginUtil.*

registerAction("threadModel", "ctrl shift T") { AnActionEvent event ->

    show("Action thread: " + Thread.currentThread())

    doInBackground {
        show("Background thread: " + Thread.currentThread())
        execute("sleep", "3")
        show(execute("pwd").stdout)
    }
}

show("live-plugin: " + Thread.currentThread())