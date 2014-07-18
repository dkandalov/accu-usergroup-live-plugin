import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

import static liveplugin.PluginUtil.*

unwrapAction("EditorTab")
wrapAction("EditorTab") { AnActionEvent event, AnAction wrappedAction ->
    def project = event.project
    def editor = currentEditorIn(project)
    def caretModel = editor.caretModel
    def document = editor.document

    def offset = caretModel.offset
    def lastFourChars = document.text.substring(offset - 4, offset)

    if (lastFourChars == "lscc-craftBeer") {
        runDocumentWriteAction(project, document) {
            document.insertString(offset, "-craftBeer")
            caretModel.moveToOffset(offset + "-craftBeer".length())
        }
    } else {
        wrappedAction.actionPerformed(event)
    }
}
show("Reloaded tab auto-complete")
