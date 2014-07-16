import com.intellij.openapi.actionSystem.AnActionEvent

import javax.swing.AbstractAction
import javax.swing.JButton
import javax.swing.JPanel

import static liveplugin.PluginUtil.*

registerAction("helloPopupAction", "ctrl alt shift P") { AnActionEvent event ->

    def menuDescription = [
        "Hello LSCC": {
            show("Hello LSCC!")
        },
        "Hello": [
                "Sandro": { show("Hello Sandro!") },
                "Samir": { show("Where is Samir?") },
        ],
        "Create Toolwindow": {
            registerToolWindow("myToolWindowId") {
                createPanelWithRecursiveButton()
            }
        }
    ]

    showPopupMenu(menuDescription, popupTitle = "LSCC")
}

show("Loaded 'helloPopupAction'<br/>Use ctrl+alt+shift+P to run it")


static JPanel createPanelWithRecursiveButton(JPanel panel = new JPanel()) {
    def selfReproducingButton = new JButton("SoftwareCraftBeer")
    selfReproducingButton.addActionListener({ createPanelWithRecursiveButton(panel) } as AbstractAction)
    panel.add(selfReproducingButton)
    panel.revalidate()
    panel
}
