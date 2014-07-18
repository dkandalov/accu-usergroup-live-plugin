import com.intellij.openapi.actionSystem.AnActionEvent

import static java.lang.Thread.currentThread
import static liveplugin.PluginUtil.*

registerAction("threadingRules", "ctrl shift T") { AnActionEvent event ->

    show("Running action on: " + currentThread())
    def name = event.project.name
    show(name)

    doInBackground {
        show(name)

        Thread.sleep(3000)
        show(execute("pwd").stdout)
    }
}

show("Loading plugin on: " + currentThread())