import com.intellij.openapi.actionSystem.AnActionEvent

import static liveplugin.PluginUtil.*

registerAction("threadModel", "ctrl shift T") { AnActionEvent event ->

    show("Action thread: " + Thread.currentThread())

    doInBackground {
        show("Background thread: " + Thread.currentThread())
        show(execute("sleep", "3").stdout)
        show(execute("pwd").stdout)
    }
}

show("live-plugin: " + Thread.currentThread())