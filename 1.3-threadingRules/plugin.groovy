import com.intellij.openapi.actionSystem.AnActionEvent

import static java.lang.Thread.currentThread
import static liveplugin.PluginUtil.*

registerAction("threadingRules", "ctrl shift T") { AnActionEvent event ->

    show("Running action on: " + currentThread())

    doInBackground {
        show("Running background task on: " + currentThread())
        Thread.sleep(3000)
        show(execute("pwd").stdout)
    }
}

show("Loading plugin on: " + currentThread())