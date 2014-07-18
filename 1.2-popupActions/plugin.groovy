import com.intellij.openapi.actionSystem.AnActionEvent

import javax.swing.*

import static liveplugin.PluginUtil.*

registerAction("helloPopupAction", "ctrl alt shift P", "ToolsMenu", "craftBeer") { AnActionEvent event ->

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
                createPanelWithCraftBeerButton()
            }
        }
    ]

    showPopupMenu(menuDescription, popupTitle = "LSCC")
}

show("Reloaded 'helloPopupAction'<br/>Use ctrl+alt+shift+P to run it")


static JPanel createPanelWithCraftBeerButton(JPanel panel = new JPanel()) {
    def selfReproducingButton = new JButton("CraftBeer")
    selfReproducingButton.addActionListener({ createPanelWithCraftBeerButton(panel) } as AbstractAction)
    panel.add(selfReproducingButton)
    panel.revalidate()
    panel
}
